package jobkorea.crawler.service;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
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
    private final OcrService ocrService;
    private static final String REGEXP_JOB_ID_PATTERN = "/Recruit/GI_Read/(\\d+)";

    public String extractCurrentUrl(WebDriverWait wait) {
        return wait.until(driver -> driver.getCurrentUrl());
    }

    public String extractJobId(String url) {
        Matcher matcher = Pattern.compile(REGEXP_JOB_ID_PATTERN).matcher(url);
        if (matcher.find()) {
            return matcher.group(1); // 찾았을 때만 그룹을 반환
        }
        throw new NoSuchElementException("JobId를 찾을 수 없습니다.");
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

    public String extractRecruitmentDetail(WebDriverWait wait) throws Exception {
        WebElement detailIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#details-section iframe")
        ));
        String articleUrl = detailIframe.getDomAttribute("src");
        return extractDetail(articleUrl);
    }

    private String extractDetail(String url) throws Exception {
        Document doc = Jsoup.connect(JOB_KOREA_URL + url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")

                .get();

        StringBuilder result = new StringBuilder();
        result.append(extractCompanyIntroduction(doc));
        result.append(extractRecruitmentTable(doc));
        result.append(extractOcrImageText(doc));

        return result.toString();
    }

    private String extractCompanyIntroduction(Document doc) {
        Element introduce = doc.selectFirst("div.artTopDesc");
        StringBuilder sb = new StringBuilder();

        sb.append("\n<회사 소개>\n");
        if (introduce != null) {
            sb.append(introduce.text());
        }
        sb.append("\n</회사 소개>\n");

        return sb.toString();
    }

    private String extractRecruitmentTable(Document doc) {
        Element recruitmentTable = doc.selectFirst("td.detailTable");

        StringBuilder sb = new StringBuilder();
        sb.append("\n<모집 부문>\n");
        if (recruitmentTable != null) {
            Safelist tableSafeList = Safelist.none()
                    .addTags("table", "thead", "tbody", "tfoot")
                    .addTags("tr", "th", "td")
                    .addTags("caption", "colgroup", "col")
                    .addAttributes("td", "rowspan", "colspan");
            String detailHtml = Jsoup.clean(recruitmentTable.html(), tableSafeList);
            sb.append(detailHtml);
        }
        sb.append("\n</모집 부문>\n");

        return sb.toString();
    }

    private String extractOcrImageText(Document doc) throws Exception {
        Elements images = doc.select("td.detailTable img");
        LinkedHashSet<String> imageSet = new LinkedHashSet<>();
        for (Element img : images) {
            imageSet.add(img.attr("src"));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n<채용 공고 이미지 OCR 내용>\n");
        for (String imgUrl : imageSet) {
            String imageText = ocrService.extractTextFromImageUrl(imgUrl);
            if (!imageText.isBlank()) {
                sb.append(imageText).append("\n\n");
            }
        }
        sb.append("\n</채용 공고 이미지 OCR 내용>\n");

        System.out.println(sb.toString());

        return sb.toString();
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
