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
                System.out.println("\n=== [" + jobCode.name() + "] 직무 크롤링 시작 === ");
                driver.get(baseUrl);
                crawlerFilter.selectFilter(wait, jobCode);
                crawlByJob(driver, wait, jobCode, pagesToCrawl);
            }
        } catch (Exception exception) {
            System.err.println("치명적 에러 발생: " + exception.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void crawlByJob(WebDriver driver, WebDriverWait wait, JobCode jobCode, List<Integer> pagesToCrawl) {
        String originalHandle = driver.getWindowHandle();
        String currentBaseUrl = driver.getCurrentUrl();

        Path outputDir = Paths.get("recruitment_summation_" + jobCode.name().toLowerCase());
        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            System.err.println("디렉토리 생성 실패 (" + outputDir + "): " + e.getMessage());
            return;
        }

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
                crawlPageItems(driver, wait, outputDir, originalHandle, linkCount);
            } catch (Exception exception) {
                System.out.println(pageNumber + "페이지 처리 중 오류: " + exception.getMessage());
            }
        }
    }

    private void crawlPageItems(WebDriver driver, WebDriverWait wait, Path outputDir, String originalHandle,
                                int linkCount) {

        for (int i = 0; i < linkCount; i++) {
            String newWindowHandle = ""; // 새 창의 핸들을 저장할 변수

            try {
                // 1. 인덱스에 해당하는 채용 공고 클릭
                String selector = "tr[data-index='" + i + "'] strong > a";
                WebElement element = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(selector))
                );
                element.click();

                // 2. 채용 공고 창이 열릴 때까지 대기
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));

                // 3. 새 창의 핸들 찾기
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalHandle)) {
                        newWindowHandle = handle;
                        break;
                    }
                }
                driver.switchTo().window(newWindowHandle);

                // 4. 새 창의 wait 생성
                WebDriverWait newWindowWait = new WebDriverWait(driver, CLICK_WAIT_TIME);

                // 5. 채용 공고 정보 추출
                String title = crawlerExtractor.extractTitle(newWindowWait);
                if (isSkipTitle(title)) {
                    System.out.println("Index " + i + " 스킵됨, 제목 : " + title);
                    continue;
                }

                // 6. LLM 요약 문서 생성
                RecruitmentPost recruitmentPost = getRecruitmentPost(newWindowWait);
                String summation = aiService.getSummationText(recruitmentPost.toFormattedString());
                System.out.println(summation);

                // 7. 요약본 저장
                saveRecruitmentPost(outputDir, recruitmentPost, summation);

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

    private RecruitmentPost getRecruitmentPost(WebDriverWait wait) throws Exception {
        RecruitmentPost post = new RecruitmentPost();

        post.setTitle(crawlerExtractor.extractTitle(wait));
        String currentUrl = crawlerExtractor.extractCurrentUrl(wait);
        post.setUrl(currentUrl);
        post.setPostId(crawlerExtractor.extractPostId(currentUrl));
        post.setCompanyName(crawlerExtractor.extractCompanyName(wait));
        post.setRecruitmentDetail(crawlerExtractor.extractRecruitmentDetail(wait));
        post.setQualification(crawlerExtractor.extractQualification(wait));
        post.setCorpInfo(crawlerExtractor.extractCorpInfo(wait));
        post.setTimeInfo(crawlerExtractor.extractTimeInfo(wait));
        post.setRecruitmentOutline(crawlerExtractor.extractRecruitmentOutline(wait));

        return post;
    }

    private void saveRecruitmentPost(Path outputDir, RecruitmentPost post, String summation) {
        try {
            String companyName = sanitizeFileName(post.getCompanyName());
            String title = sanitizeFileName(post.getTitle());
            String fileName = post.getPostId() + "_" + companyName + "_" + title + ".text";

            Path filePath = outputDir.resolve(fileName);
            Files.writeString(filePath, summation, StandardCharsets.UTF_8);
            System.out.println("파일 저장 완료: " + fileName);
        } catch (Exception e) {
            System.err.println("파일 저장 중 오류 발생 (" + post.getPostId() + "): " + e.getMessage());
        }
    }

    private boolean isSkipTitle(String title) {
        return title.contains("교육") || title.contains("국비") || title.contains("캠프") || title.contains("취업");
    }

    private String sanitizeFileName(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t.]", "_").trim();
    }
}
