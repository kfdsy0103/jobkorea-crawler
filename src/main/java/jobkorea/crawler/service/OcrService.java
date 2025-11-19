package jobkorea.crawler.service;

public interface OcrService {
    String extractTextFromImageUrl(String imageUrl) throws Exception;
}
