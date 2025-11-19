package jobkorea.crawler.service;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlerExtractor {

    private final String JOB_KOREA_URL = "https://www.jobkorea.co.kr";
    private final GoogleOcrService googleOcrService;
    private final NaverOcrService naverOcrService;

    public String extractText(WebDriverWait wait) throws IOException {

        StringBuilder htmlContent = new StringBuilder();

        // 1. 공고 식별 번호 및 링크
        String jobId = "";
        String currentUrl = wait.until(driver -> driver.getCurrentUrl());
        Pattern pattern = Pattern.compile("/Recruit/GI_Read/(\\d+)");
        Matcher matcher = pattern.matcher(currentUrl);
        if (matcher.find()) {
            jobId = matcher.group(1);
        }
        htmlContent.append("\n--- 1. 공고 ID 및 링크 ---\n").append(jobId).append(", ").append(currentUrl);
        System.out.println("\n--- 1. 공고 ID 및 링크 ---\n" + jobId + ", " + currentUrl);

        // 2. 채용 공고 제목
        String title = extractTitle(wait);
        htmlContent.append("\n--- 2. 채용 공고 제목 ---\n").append(title);
        System.out.println("\n--- 2. 채용 공고 제목 ---\n" + title);

        // 3. 기업명
        String companyName = extractCompanyName(wait);
        htmlContent.append("\n--- 3. 기업명 ---\n").append(companyName);
        System.out.println("\n--- 3. 기업명 ---\n" + companyName);

        // 4. 상세 모집 요강
        String recruitmentDetail = extractRecruitmentDetail(wait);
        htmlContent.append("\n--- 4. 상세 모집 요강 ---\n").append(recruitmentDetail);
        System.out.println("\n--- 4. 상세 모집 요강 ---\n" + recruitmentDetail);

        // 5. 지원 자격
        String qualification = extractQualification(wait);
        htmlContent.append("\n--- 5. 지원 자격 ---\n").append(qualification);
        System.out.println("\n--- 5. 지원 자격 ---\n" + qualification);

        // 6. 기업 정보
        String corpInfo = extractCorpInfo(wait);
        htmlContent.append("\n--- 6. 기업 정보 ---\n").append(corpInfo);
        System.out.println("\n--- 6. 기업 정보 ---\n" + corpInfo);

        // 7. 시작일 및 마감일
        String timeInfo = extractTimeInfo(wait);
        htmlContent.append("\n--- 7. 시작일 및 마감일 ---\n").append(timeInfo);
        System.out.println("\n--- 7. 시작일 및 마감일 ---\n" + timeInfo);

        // 8. 모집 요강
        String recruitmentOutline = extractRecruitmentOutline(wait);
        htmlContent.append("\n--- 8. 모집 요강 ---\n").append(recruitmentOutline);
        System.out.println("\n--- 8. 모집 요강 ---\n" + recruitmentOutline);

        return htmlContent.toString();
    }

    public String extractTitle(WebDriverWait wait) {
        WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1[data-sentry-element='Typography']")
        ));
        return titleElement.getText();
    }

    public String extractCompanyName(WebDriverWait wait) {
        WebElement companyNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h2[data-sentry-element='Typography']")
        ));
        return companyNameElement.getText();
    }

    // 예외 확인하고, 실패 시 skip
    public String extractRecruitmentDetail(WebDriverWait wait) throws IOException {
        WebElement detailIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#details-section iframe")
        ));
        String articleUrl = detailIframe.getDomAttribute("src");
        return extractDetail(articleUrl);
    }

    private String extractDetail(String url) throws IOException {
        Document doc = Jsoup.connect(JOB_KOREA_URL + url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")

                .get();

        Element introduce = doc.selectFirst("div.artTopDesc");
        Element recruitmentTable = doc.selectFirst("td.detailTable");
        Elements images = doc.select("td.detailTable img");

        // 이미지 중복 제거
        Set<String> imageSet = new LinkedHashSet<>();
        for (Element img : images) {
            imageSet.add(img.attr("src"));
        }

        StringBuilder result = new StringBuilder();

        result.append("\n<회사 소개>\n");
        if (introduce != null) {
            result.append(introduce.text());
        }
        result.append("\n</회사 소개>\n");

        result.append("\n<모집 부문>\n");
        if (recruitmentTable != null) {
            Safelist tableSafeList = Safelist.none()
                    .addTags("table", "thead", "tbody", "tfoot")
                    .addTags("tr", "th", "td")
                    .addTags("caption", "colgroup", "col");
            String detailHtml = Jsoup.clean(recruitmentTable.html(), tableSafeList);
            result.append(detailHtml);
        }
        result.append("\n</모집 부문>\n");

        result.append("\n<채용 공고 이미지 OCR 내용>\n");
        for (String imgUrl : imageSet) {
            String imageText = naverOcrService.extractTextFromImageUrl(imgUrl);
            result.append(imageText).append("\n\n");
        }
        result.append("\n</채용 공고 이미지 OCR 내용>\n");

        return result.toString();
    }

    public String extractQualification(WebDriverWait wait) {
        WebElement qualificationSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-sentry-component='Qualification']")
        ));
        String rawHtml = qualificationSection.getAttribute("innerHTML");
        return extractQualification(rawHtml);
    }

    private String extractQualification(String rawHtml) {
        Document doc = Jsoup.parseBodyFragment(rawHtml);
        Elements items = doc.select("div[data-sentry-component='QualificationItem']");

        StringBuilder cleanHtml = new StringBuilder();
        for (Element item : items) {
            Element keyElement = item.selectFirst("span[style*='min-width:80px']");
            String key = keyElement.text();
            String value = item.text().replace(key, "").trim();
            cleanHtml.append(key).append(": ").append(value).append("\n");
        }
        return cleanHtml.toString();
    }

    public String extractCorpInfo(WebDriverWait wait) {
        WebElement corpInfoSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-sentry-component='CorpInformation']")
        ));
        String rawHtml = corpInfoSection.getAttribute("innerHTML");
        return extractCorpInfo(rawHtml);
    }

    private String extractCorpInfo(String rawHtml) {
        Document doc = Jsoup.parseBodyFragment(rawHtml);
        Elements items = doc.select("div[data-sentry-component='CorpInformationBox']");

        StringBuilder cleanHtml = new StringBuilder();
        for (Element item : items) {
            Element keyElement = item.selectFirst("span[class*='Typography_variant_size13']");
            Element valueElement = item.selectFirst("div[class*='Typography_variant_size14']");
            String key = keyElement.text();
            String value = valueElement.text();
            if (!key.isEmpty() && !value.isEmpty()) {
                cleanHtml.append(key).append(": ").append(value).append("\n");
            }
        }
        return cleanHtml.toString();
    }

    public String extractTimeInfo(WebDriverWait wait) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-sentry-component='SimpleTable']")
        ));
        return element.getText();
    }

    public String extractRecruitmentOutline(WebDriverWait wait) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(
                        "div[data-sentry-element='Flex'][data-sentry-source-file='index.tsx'].Flex_display_flex__i0l0hl2.Flex_gap_space28__i0l0hl2a.Flex_direction_column__i0l0hl4"
                )
        ));
        return element.getText();
    }
}
