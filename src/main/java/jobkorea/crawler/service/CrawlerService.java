package jobkorea.crawler.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import jobkorea.crawler.config.WebDriverFactory;
import jobkorea.crawler.dto.RecruitmentPost;
import jobkorea.crawler.enums.JobCode;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final WebDriverFactory webDriverFactory;
    private final CrawlerFilter crawlerFilter;
    private final CrawlerExtractor crawlerExtractor;
    private final AIService aiService;
    private final Duration CLICK_WAIT_TIME = Duration.ofSeconds(5);
    private final String JOB_KOREA_URL = "https://www.jobkorea.co.kr";
    private final String baseUrl = JOB_KOREA_URL + "/recruit/joblist?menucode=duty";

    public void crawl(List<Integer> pagesToCrawl) {
        WebDriver driver = webDriverFactory.createChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, CLICK_WAIT_TIME);
        try {
            for (JobCode jobCode : JobCode.values()) {
                System.out.println("=== [" + jobCode.name() + "] 직무 크롤링 시작 === ");
                driver.get(baseUrl);
                crawlerFilter.selectFilter(wait, jobCode);
                crawlByJob(driver, wait, pagesToCrawl);
            }
        } catch (Exception exception) {
            System.err.println("치명적 에러 발생: " + exception.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void crawlByJob(WebDriver driver, WebDriverWait wait, List<Integer> pagesToCrawl) {
        String originalHandle = driver.getWindowHandle();
        String currentBaseUrl = driver.getCurrentUrl();

        for (Integer pageNumber : pagesToCrawl) {
            try {
                String newUrl = currentBaseUrl.replaceAll("#anchorGICnt_\\d+", "#anchorGICnt_" + pageNumber);
                driver.get(newUrl);

                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("tr[data-index='0']")
                ));

                // 공고 개수 확인
                int linkCount = driver.findElements(By.cssSelector("tr[data-index]")).size();
                System.out.println(pageNumber + "페이지에서 " + linkCount + "개의 항목 발견");

                // 페이지 당 크롤링
                crawlPageItems(driver, wait, originalHandle, linkCount);
            } catch (Exception exception) {
                System.out.println(pageNumber + "페이지 처리 중 오류: " + exception.getMessage());
            }
        }
    }

    private void crawlPageItems(WebDriver driver, WebDriverWait wait, String originalHandle, int linkCount) {

        Path outputDir = Paths.get("recruitment_summation");
        try {
            Files.createDirectories(outputDir);
        } catch (Exception exception) {
            System.err.println("디렉토리 생성 실패: \n" + exception.getMessage());
        }

        for (int i = 0; i < linkCount; i++) {
            String newWindowHandle = ""; // 새 창의 핸들을 저장할 변수

            try {
                // 1. 인덱스에 해당하는 채용 공고
                String selector = "tr[data-index='" + i + "'] strong > a";
                WebElement element = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(selector))
                );

                // 2. 클릭해서 새 창(탭) 열기
                element.click();

                // 3. 새 창이 열릴 때까지 대기
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));

                // 4. 새로 열린 창의 핸들 찾기
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalHandle)) {
                        newWindowHandle = handle;
                        break;
                    }
                }

                // 5. 새 창으로 초점 전환
                driver.switchTo().window(newWindowHandle);

                // 6. 새 창의 wait를 전달하여 텍스트 추출
                WebDriverWait newWindowWait = new WebDriverWait(driver, CLICK_WAIT_TIME);

                // 7. 채용 공고 DTO 생성
                String title = crawlerExtractor.extractTitle(newWindowWait);
                if (title.contains("교육") || title.contains("국비") || title.contains("캠프") || title.contains("취업")) {
                    System.out.println("Index " + i + " 스킵됨 : " + title);
                    continue;
                }
                RecruitmentPost post = new RecruitmentPost();
                post.setTitle(title);
                String currentUrl = crawlerExtractor.extractCurrentUrl(newWindowWait);
                post.setUrl(currentUrl);
                post.setJobId(crawlerExtractor.extractJobId(currentUrl));
                post.setCompanyName(crawlerExtractor.extractCompanyName(newWindowWait));
                post.setRecruitmentDetail(crawlerExtractor.extractRecruitmentDetail(newWindowWait));
                post.setQualification(crawlerExtractor.extractQualification(newWindowWait));
                post.setCorpInfo(crawlerExtractor.extractCorpInfo(newWindowWait));
                post.setTimeInfo(crawlerExtractor.extractTimeInfo(newWindowWait));
                post.setRecruitmentOutline(crawlerExtractor.extractRecruitmentOutline(newWindowWait));

                // 8. LLM 요약본 생성
                String resultHTML = post.toFormattedString();
                String summation = aiService.getSummationText(resultHTML);
                System.out.println(summation);

                // 9. 요약본 text 파일로 저장
                saveRecruitmentPost(outputDir, post, summation);

            } catch (Exception e) {
                System.err.println("Index " + i + " 크롤링 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (!newWindowHandle.isEmpty() && driver.getWindowHandles().size() > 1) {
                    driver.close();
                }
                driver.switchTo().window(originalHandle);
            }
        }
    }

    private void saveRecruitmentPost(Path outputDir, RecruitmentPost post, String summation) {
        try {
            String companyName = sanitizeFileName(post.getCompanyName());
            String title = sanitizeFileName(post.getTitle());
            String fileName = post.getJobId() + "_" + companyName + "_" + title + ".text";

            Path filePath = outputDir.resolve(fileName);
            Files.writeString(filePath, summation, StandardCharsets.UTF_8);
            System.out.println("파일 저장 완료: " + fileName);
        } catch (Exception e) {
            System.err.println("파일 저장 중 오류 발생 (" + post.getJobId() + "): " + e.getMessage());
        }
    }

    private String sanitizeFileName(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t.]", "_").trim();
    }
}
