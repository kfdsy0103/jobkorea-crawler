package jobkorea.crawler.controller;

import java.util.List;
import jobkorea.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    @GetMapping("/request-crawling")
    public void requestCrawling() {
        crawlerService.multiThreadCrawl(List.of(1));
    }
}
