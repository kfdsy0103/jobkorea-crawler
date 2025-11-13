package jobkorea.crawler.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlerExtractor {

    private final TessaractService tesseractService;
    private final String JOB_KOREA_URL = "https://www.jobkorea.co.kr";

    public String extractText(WebDriverWait wait) {

        StringBuilder htmlContent = new StringBuilder();

        // 1. 채용 공고 제목
        String title = "";
        try {
            WebElement companyNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1[data-sentry-element='Typography']")
            ));
            title = companyNameElement.getText();
        } catch (Exception e) {
            System.err.println("'제목'을 찾을 수 없습니다: " + e.getMessage());
        }
        htmlContent.append("1. 채용 공고 제목 : ").append(title).append("\n");
        System.out.println("\n--- 1. 채용 공고 제목 ---\n" + title);

        // 2. 기업명
        String companyName = "";
        try {
            WebElement companyNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h2[data-sentry-element='Typography']")
            ));
            companyName = companyNameElement.getText();
        } catch (Exception e) {
            System.err.println("'기업명'을 찾을 수 없습니다: " + e.getMessage());
        }
        htmlContent.append("2. 기업명 : ").append(companyName).append("\n");
        System.out.println("\n--- 2. 기업명 ---\n" + companyName);

        // 3. 상세 모집 요강
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
        htmlContent.append("3. 상세 모집 요강\n").append(detail).append("\n");
        System.out.println("--- 3. 상세 모집 요강 ---\n" + detail);

        // 4. 지원 자격
        String qualification = "";
        try {
            WebElement qualificationSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-sentry-component='Qualification']")
            ));
            String rawHTML = qualificationSection.getAttribute("innerHTML");
            qualification =
        } catch (Exception e) {
            System.err.println("'Qualification' 섹션을 찾을 수 없습니다");
        }
        htmlContent.append("4. 지원 자격\n").append(qualification).append("\n");
        System.out.println("--- 4. 지원 자격 ---\n" + qualification);

        // 5. 기업 정보
        String corpInfo = "";
        try {
            WebElement corpInfoSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-sentry-component='CorpInformation']")
            ));
            String rawHTML = corpInfoSection.getAttribute("innerHTML");
            corpInfo =
        } catch (Exception e) {
            System.err.println("'CorpInformation' 섹션을 찾을 수 없습니다");
        }
        htmlContent.append("5. 기업 정보\n").append(corpInfoHtml).append("\n");
        System.out.println("--- 5. 기업 정보 ---\n" + corpInfoHtml);

        return htmlContent.toString();
    }

    public String extractTitle(WebDriverWait wait) {
        WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1[data-sentry-element='Typography']")
        ));
        return titleElement.getText();
    }

    private String extractDetail(String url) throws Exception {
        if (url == null || url.isBlank()) return "";

        Document doc = Jsoup.connect(JOB_KOREA_URL + url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .get();

        Element detailElement = doc.selectFirst("#detail-content");
        if (detailElement != null) {
            Elements imgElements = detailElement.select("img");
            StringBuilder imageText = new StringBuilder();
            for (Element imgElement : imgElements) {
                // (안정성 개선) .attr("src") 대신 .absUrl("src")를 사용
                imageText.append(tesseractService.extractTextFromImageUrl(imgElement.absUrl("src")));
                imageText.append("\n");
            }
            return detailElement.outerHtml() + "\n<채용공고 이미지 내용>\n" + imageText + "\n</채용공고 이미지 내용>";
        } else {
            return "";
        }
    }
}
