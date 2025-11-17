package jobkorea.crawler.service;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class CrawlerFilter {

    public void selectFilter(WebDriverWait wait) throws InterruptedException {

        // "AI/개발/데이터" 필터 클릭
        WebElement devDataFilter = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("label[for='duty_step1_10031']")));
        devDataFilter.click();

        // 상세 분류에서 요소 체크 (우선 백엔드만)
        List<String> jobValues = List.of("1000229");

        for (String jobValue : jobValues) {
            // Java에서는 문자열 포맷팅 대신 + 연산자로 CSS 선택자를 만듭니다.
            String selector = "label[for='duty_step2_" + jobValue + "']";

            WebElement jobCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
            wait.until(ExpectedConditions.elementToBeClickable(jobCheckbox)).click();
        }

        // "검색" 버튼 클릭
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("dev-btn-search")));
        searchButton.click();
        Thread.sleep(5000); // 한 창에 모든 요소가 존재하여 대기

        // 최신 업데이트로 정렬
        WebElement optionToClick = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#orderTab option[value='3']")
        ));
        optionToClick.click();
    }
}
