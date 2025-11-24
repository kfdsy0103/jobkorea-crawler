package jobkorea.embedding.controller;

import jobkorea.embedding.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @PostMapping("/save-recruitment")
    public void requestCrawling(@RequestParam String text) {
        recruitmentService.saveRecruitment(text);
    }
}
