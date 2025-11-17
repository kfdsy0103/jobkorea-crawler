package jobkorea.crawler.service;

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

    public String extractText(WebDriverWait wait) {

        StringBuilder htmlContent = new StringBuilder();

        // 1. 공고 식별 번호 및 링크
        String jobId = "";
        String currentUrl = wait.until(driver -> driver.getCurrentUrl());
        Pattern pattern = Pattern.compile("/Recruit/GI_Read/(\\d+)");
        Matcher matcher = pattern.matcher(currentUrl);
        if (matcher.find()) {
            jobId = matcher.group(1);
        }
        htmlContent.append("1. 공고 ID 및 링크 : ").append(jobId).append(currentUrl).append("\n");
        htmlContent.append("림크 : ").append(currentUrl).append("\n");
        System.out.println("\n--- 1. 공고 ID 및 링크 ---\n" + jobId + ", " + currentUrl);

        // 2. 채용 공고 제목
        String title = "";
        try {
            WebElement companyNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1[data-sentry-element='Typography']")
            ));
            title = companyNameElement.getText();
        } catch (Exception e) {
            System.err.println("'제목'을 찾을 수 없습니다: " + e.getMessage());
        }
        htmlContent.append("2. 채용 공고 제목 : ").append(title).append("\n");
        System.out.println("--- 2. 채용 공고 제목 ---\n" + title);

        // 3. 기업명
        String companyName = "";
        try {
            WebElement companyNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h2[data-sentry-element='Typography']")
            ));
            companyName = companyNameElement.getText();
        } catch (Exception e) {
            System.err.println("'기업명'을 찾을 수 없습니다: " + e.getMessage());
        }
        htmlContent.append("3. 기업명 : ").append(companyName).append("\n");
        System.out.println("--- 3. 기업명 ---\n" + companyName);

        // 4. 상세 모집 요강
        String detail = "";
        try {
            WebElement detailIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#details-section iframe")
            ));
            String articleUrl = detailIframe.getDomAttribute("src");
            detail = extractDetail(articleUrl);
        } catch (Exception e) {
            System.err.println("상세 모집 iframe을 찾을 수 없습니다: " + e.getMessage());
        }
        htmlContent.append("4. 상세 모집 요강\n").append(detail).append("\n");
        System.out.println("--- 4. 상세 모집 요강 ---\n" + detail);

        // 5. 지원 자격
        String qualification = "";
        try {
            WebElement qualificationSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-sentry-component='Qualification']")
            ));
            String rawHtml = qualificationSection.getAttribute("innerHTML");
            qualification = extractQualification(rawHtml);
        } catch (Exception e) {
            System.err.println("'Qualification' 섹션을 찾을 수 없습니다");
        }
        htmlContent.append("5. 지원 자격\n").append(qualification).append("\n");
        System.out.println("--- 5. 지원 자격 ---\n" + qualification);

        // 6. 기업 정보
        String corpInfo = "";
        try {
            WebElement corpInfoSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-sentry-component='CorpInformation']")
            ));
            String rawHtml = corpInfoSection.getAttribute("innerHTML");
            corpInfo = extractCorpInfo(rawHtml);
        } catch (Exception e) {
            System.err.println("'CorpInformation' 섹션을 찾을 수 없습니다");
        }
        htmlContent.append("6. 기업 정보\n").append(corpInfo).append("\n");
        System.out.println("--- 6. 기업 정보 ---\n" + corpInfo);

        // 7. 시작일 및 마감일
        String timeInfo = "";
        try {
            WebElement table = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("div[data-sentry-component='SimpleTable']")
                    )
            );
            timeInfo = table.getText();
        } catch (Exception e) {
            System.err.println("'시작일 및 마감일' 섹션을 찾을 수 없습니다.");
        }
        htmlContent.append("7. 시작일 및 마감일\n").append(timeInfo).append("\n");
        System.out.println("--- 7. 시작일 및 마감일 ---\n" + timeInfo);

        // 8. 모집 요강
        String dti = "";
        try {
            WebElement flexDiv = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(
                            "div[data-sentry-element='Flex'][data-sentry-source-file='index.tsx'].Flex_display_flex__i0l0hl2.Flex_gap_space28__i0l0hl2a.Flex_direction_column__i0l0hl4"
                    )
            ));
            dti = flexDiv.getText();
        } catch (Exception e) {
            System.err.println("'모집 요강' 섹션을 찾을 수 없습니다.");
        }
        htmlContent.append("8. 모집 요강\n").append(dti).append("\n");
        System.out.println("--- 8. 모집 요강 ---\n" + dti);

        return htmlContent.toString();
    }

    public String extractTitle(WebDriverWait wait) {
        WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1[data-sentry-element='Typography']")
        ));
        return titleElement.getText();
    }

    private String extractDetail(String url) throws Exception {
        if (url == null || url.isBlank()) {
            return "";
        }

        Document doc = Jsoup.connect(JOB_KOREA_URL + url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .get();

        Element detailElement = doc.selectFirst("#detail-content");
        if (detailElement != null) {
            Set<String> imgUrlSet = new LinkedHashSet<>(); // 이미지 경로 중복 제거를 위함
            Elements imgElements = detailElement.select("img");
            for (Element imgElement : imgElements) {
                String imgUrl = imgElement.absUrl("src");
                imgUrlSet.add(imgUrl);
            }
            StringBuilder imageText = new StringBuilder();
            imgUrlSet.forEach(urlStr -> imageText.append(urlStr).append("\n"));
            String detailDescription = Jsoup.clean(detailElement.outerHtml(), Safelist.none());
            return detailDescription
                    + "\n<imgUrl>\n"
                    + imageText
                    + "</imgUrl>";
        } else {
            return "";
        }
    }

    private String extractQualification(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return "";
        }
        Document doc = Jsoup.parseBodyFragment(rawHtml);

        Elements items = doc.select("div[data-sentry-component='QualificationItem']");
        if (items.isEmpty()) {
            return "";
        }

        StringBuilder cleanHtml = new StringBuilder();
        for (Element item : items) {
            // 경력, 학력, 스킬, 우대조건
            Element keyElement = item.selectFirst("span[style*='min-width:80px']");
            String key = (keyElement != null) ? keyElement.text() : "";
            String value = item.text().replaceFirst(key, "").trim();

            cleanHtml.append(key)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
        return cleanHtml.toString();
    }

    private String extractCorpInfo(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return "";
        }
        Document doc = Jsoup.parseBodyFragment(rawHtml);

        Elements items = doc.select("div[data-sentry-component='CorpInformationBox']");
        if (items.isEmpty()) {
            return "";
        }

        StringBuilder cleanHtml = new StringBuilder();
        for (Element item : items) {
            // 사원수, 기업 구분, 산업(업종), 지도보기
            Element keyElement = item.selectFirst("span[class*='Typography_variant_size13']");
            Element valueElement = item.selectFirst("div[class*='Typography_variant_size14']");

            String key = (keyElement != null) ? keyElement.text() : "";
            String value = (valueElement != null) ? valueElement.text() : "";

            if (!key.isEmpty() && !value.isEmpty()) {
                cleanHtml.append(key)
                        .append(": ")
                        .append(value)
                        .append("\n");
            }
        }
        return cleanHtml.toString();
    }
}
