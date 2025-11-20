package jobkorea.crawler.controller;

import java.util.List;
import jobkorea.crawler.service.CrawlerService;
import jobkorea.crawler.service.NaverOcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final NaverOcrService naverOcrService;

    @GetMapping("/request-crawling")
    public void requestCrawling() {
        crawlerService.crawl(List.of(1));
    }

    @GetMapping("/long-image")
    public String longImage() {
        try {
            return naverOcrService.extractTextFromImageUrl(
                    "https://file2.jobkorea.co.kr/Net/Mng/DownImage/CorpEditor?file_No=1503549");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }
}
