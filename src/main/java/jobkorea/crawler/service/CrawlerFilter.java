package jobkorea.crawler.service;

import jobkorea.crawler.enums.JobCode;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class CrawlerFilter {

    public void selectFilter(WebDriverWait wait, JobCode jobCode) throws Exception {
        String step1 = jobCode.getJobCode();
        String step2 = jobCode.getDetailCode();

        WebElement devDataFilter = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("label[for='duty_step1_" + step1 + "']")));
        devDataFilter.click();
        String selector = "label[for='duty_step2_" + step2 + "']";

        WebElement jobCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
        wait.until(ExpectedConditions.elementToBeClickable(jobCheckbox)).click();

        searchAndSort(wait);
    }

    private void searchAndSort(WebDriverWait wait) throws InterruptedException {
        // "검색" 버튼 클릭
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("dev-btn-search")));
        searchButton.click();
        Thread.sleep(5000); // 한 창에 모든 요소가 존재하여 대기

        // 최신 업데이트로 정렬
        WebElement optionToClick = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#orderTab option[value='3']")
        ));
        optionToClick.click();

        // 50개씩 보기
        WebElement elementsPerPage = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#pstab option[value='50']")
        ));
        elementsPerPage.click();
    }
}
