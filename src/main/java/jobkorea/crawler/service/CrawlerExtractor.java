package jobkorea.crawler.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
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
public class CrawlerExtractor {

    private final String JOB_KOREA_URL = "https://www.jobkorea.co.kr";
    private final String TESS_DATA_PATH = "C:\\OCR\\tessdata";

    public String extractText(WebDriverWait wait) {
        // 추출한 HTML 조각들을 합칠 StringBuilder 생성
        StringBuilder htmlContent = new StringBuilder();

        // 보기 좋은 HTML 파일로 만들기 위해 기본 템플릿 추가
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html lang=\"ko\">\n");
        htmlContent.append("<head>\n");
        htmlContent.append("  <meta charset=\"UTF-8\">\n");
        htmlContent.append("  <title>채용 공고</title>\n");
        htmlContent.append("  <style>\n");
        htmlContent.append("    body { font-family: 'Malgun Gothic', sans-serif; margin: 20px; line-height: 1.6; }\n");
        htmlContent.append("    .section { border: 1px solid #ddd; border-radius: 8px; margin-bottom: 25px; overflow: hidden; }\n");
        htmlContent.append("    .section h2 { background-color: #f5f5f5; padding: 15px 20px; margin: 0; border-bottom: 1px solid #ddd; }\n");
        htmlContent.append("    .content { padding: 20px; }\n");
        htmlContent.append("    h1 { color: #333; }\n");
        htmlContent.append("  </style>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");

        // 1. 제목
        String title = "";
        try {
            WebElement companyNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1[data-sentry-element='Typography']")
            ));
            title = companyNameElement.getText();
        } catch (Exception e) {
            System.err.println("'제목'을 찾을 수 없습니다: " + e.getMessage());
        }
        htmlContent.append("<h1>").append(title).append("</h1>\n");
        System.out.println("\n--- 1. 제목 ---\n" + title);

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
        htmlContent.append("<h1>").append(companyName).append("</h1>\n");
        System.out.println("\n--- 2. 기업명 ---\n" + companyName);

        // 3. 상세 모집 요강
        String detailHtml = "";
        try {
            WebElement detailIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#details-section iframe")
            ));
            String articleUrl = detailIframe.getDomAttribute("src");
            detailHtml = extractArticle(articleUrl);
        } catch (Exception e) {
            System.err.println("상세 모집 iframe을 찾을 수 없습니다: " + e.getMessage());
        }
        // 4. (NEW) div 섹션으로 StringBuilder에 추가
        htmlContent.append("<div class=\"section\">\n");
        htmlContent.append("  <h2>3. 상세 모집 요강</h2>\n");
        htmlContent.append("  <div class=\"content\">\n").append(detailHtml).append("</div>\n");
        htmlContent.append("</div>\n");
        System.out.println("--- 3. 상세 모집 요강 ---\n" + detailHtml);

        // 4. 지원 자격
        String qualificationHtml = "";
        try {
            WebElement qualificationSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-sentry-component='Qualification']")
            ));
            qualificationHtml = qualificationSection.getAttribute("innerHTML");
        } catch (Exception e) {
            System.err.println("'Qualification' 섹션을 찾을 수 없습니다");
        }
        htmlContent.append("<div class=\"section\">\n");
        htmlContent.append("  <h2>4. 지원 자격</h2>\n");
        htmlContent.append("  <div class=\"content\">\n").append(qualificationHtml).append("</div>\n");
        htmlContent.append("</div>\n");
        System.out.println("--- 4. 지원 자격 ---\n" + qualificationHtml);

        // 5. 기업 정보
        String corpInfoHtml = "";
        try {
            WebElement corpInfoSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-sentry-component='CorpInformation']")
            ));
            corpInfoHtml = corpInfoSection.getAttribute("innerHTML");
        } catch (Exception e) {
            System.err.println("'CorpInformation' 섹션을 찾을 수 없습니다");
        }
        htmlContent.append("<div class=\"section\">\n");
        htmlContent.append("  <h2>5. 기업 정보</h2>\n");
        htmlContent.append("  <div class=\"content\">\n").append(corpInfoHtml).append("</div>\n");
        htmlContent.append("</div>\n");
        htmlContent.append("</body>\n</html>\n");
        System.out.println("--- 5. 기업 정보 ---\n" + corpInfoHtml);

        return htmlContent.toString();
    }

    public String extractTitle(WebDriverWait wait) {
        WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1[data-sentry-element='Typography']")
        ));
        return titleElement.getText();
    }

    private String extractArticle(String url) throws Exception {
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
                imageText.append(extractTextFromImageUrl(imgElement.absUrl("src")));
                imageText.append("\n");
            }
            return detailElement.outerHtml() + "\n<채용공고 이미지 내용>\n" + imageText + "\n</채용공고 이미지 내용>";
        } else {
            return "";
        }
    }

    // 이미지 URL을 Tesseract로 OCR하는 헬퍼 메소드
    private String extractTextFromImageUrl(String url) throws Exception {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESS_DATA_PATH); // 상수 사용
        tesseract.setLanguage("kor+eng");
        tesseract.setPageSegMode(6);

        InputStream inputStream = null;
        try {
            URL imageUrl = new URL(url);
            inputStream = imageUrl.openStream();

            BufferedImage image = ImageIO.read(inputStream);
            if(image == null) {
                throw new IOException("URL에서 이미지를 읽어올 수 없습니다.");
            }

            return tesseract.doOCR(image).trim();
        } catch (Exception exception) {
            System.out.println("extractTextFromImageUrl 실패\n" + exception.getMessage());
            return "";
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
