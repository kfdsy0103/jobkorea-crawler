package jobkorea.crawler.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TessaractService {

    private static final String TESS_DATA_PATH = "C:\\OCR\\tessdata";

    public String extractTextFromImageUrl(String url) throws Exception {
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
