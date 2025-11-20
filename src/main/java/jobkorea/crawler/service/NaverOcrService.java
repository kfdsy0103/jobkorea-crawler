package jobkorea.crawler.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class NaverOcrService implements OcrService {

    private final String apiUrl;
    private final String secretKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private static final int MAX_HEIGHT = 1960;
    private static final List<String> SUPPORTED_FORMATS = List.of("jpg", "jpeg", "png", "pdf", "tif", "tiff");

    public NaverOcrService(
            @Value("${naver.service.url}") String apiUrl,
            @Value("${naver.service.secretKey}") String secretKey,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {
        this.apiUrl = apiUrl;
        this.secretKey = secretKey;
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.objectMapper = objectMapper;
    }

    @Data
    @Builder
    public static class OcrRequest {
        @Builder.Default
        private String version = "V2";
        private String requestId;
        private long timestamp;
        @Builder.Default
        private String lang = "ko";
        private List<ImagePayload> images;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImagePayload {
        private String format;
        private String name;
        private String url; // 1960 이하
        private String data; // 1960 이상
    }

    @Override
    public String extractTextFromImageUrl(String imageUrl) throws Exception {

        if (imageUrl.startsWith("//")) {
            imageUrl = "https:" + imageUrl;
        }

        String extension = getExtensionFromUrl(imageUrl);
        if (!SUPPORTED_FORMATS.contains(extension)) {
            System.err.println("OCR 미지원 이미지입니다: " + imageUrl);
            return "";
        }

        // 이미지 확장자 정의
        String format = extension;

        // 1. 이미지 다운로드 및 크기 확인
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        int totalHeight = originalImage.getHeight();
        int width = originalImage.getWidth();

        // 높이 체크 및 분기 처리
        if (totalHeight <= MAX_HEIGHT) {
            return sendOcrRequest(imageUrl, null, format);
        }

        // 이미지 분할 시 포맷을 png로 고정
        format = "png";
        StringBuilder combinedText = new StringBuilder();
        for (int y = 0; y < totalHeight; y += MAX_HEIGHT) {
            // 자를 높이 계산 (마지막 조각은 남은 만큼만)
            int currentHeight = Math.min(MAX_HEIGHT, totalHeight - y);

            // 이미지 자르기 (x, y, w, h)
            BufferedImage subImage = originalImage.getSubimage(0, y, width, currentHeight);

            // 자른 이미지를 Base64 문자열로 변환
            String base64Data = convertImageToBase64(subImage, format);

            // OCR 요청 (URL 대신 Data 전송)
            String chunkResult = sendOcrRequest(null, base64Data, format);
            combinedText.append(chunkResult).append("\n");
        }

        return combinedText.toString().trim();

    }

    private String sendOcrRequest(String url, String base64Data, String format) {
        try {
            // ImagePayload 생성 (URL 혹은 Data 둘 중 하나만 설정)
            ImagePayload.ImagePayloadBuilder payloadBuilder = ImagePayload.builder()
                    .format(format)
                    .name("ocr-image");

            if (base64Data != null) {
                payloadBuilder.data(base64Data); // 이미지 직접 전송
            } else {
                payloadBuilder.url(url); // URL 전송
            }

            OcrRequest requestBody = OcrRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .timestamp(System.currentTimeMillis())
                    .images(List.of(payloadBuilder.build()))
                    .build();

            // WebClient 호출
            String jsonResponse = webClient.post()
                    .header("Content-Type", "application/json")
                    .header("X-OCR-SECRET", secretKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 응답 파싱
            StringBuilder resultText = new StringBuilder();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode firstImage = rootNode.path("images").get(0);

            if ("SUCCESS".equals(firstImage.path("inferResult").asText())) {
                JsonNode fieldsNode = firstImage.path("fields");
                if (fieldsNode.isArray()) {
                    for (JsonNode field : fieldsNode) {
                        resultText.append(field.path("inferText").asText()).append(" ");
                    }
                }
            } else {
                System.err.println("OCR inference failed: " + firstImage.path("message").asText());
            }

            return resultText.toString().trim();

        } catch (WebClientResponseException e) {
            System.err.println("========== OCR API 오류 발생 (400) ==========");
            System.err.println("상태 코드: " + e.getStatusCode());
            System.err.println("응답 본문: " + e.getResponseBodyAsString()); // 이 로그를 확인해야 함
            System.err.println("==========================================");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String convertImageToBase64(BufferedImage image, String format) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Image conversion failed", e);
        }
    }

    private String getExtensionFromUrl(String imageUrl) {
        int lastDotIndex = imageUrl.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return imageUrl.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}