package jobkorea.crawler.service;

import jobkorea.crawler.enums.JobCode;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class CrawlerFilter {

    public void selectWebDeveloperFilter(WebDriverWait wait) throws Exception {

        JobCode jobCode = JobCode.웹개발;
        String step1 = jobCode.getJobCode();
        String step2 = jobCode.getDetailCode();

        // "AI/개발/데이터" -> "웹개발"
        WebElement devDataFilter = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("label[for='duty_step1_" + step1 + "']")));
        devDataFilter.click();
        String selector = "label[for='duty_step2_" + step2 + "']";

        WebElement jobCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
        wait.until(ExpectedConditions.elementToBeClickable(jobCheckbox)).click();

        searchAndSort(wait);
    }

    public void selectPMFilter(WebDriverWait wait) throws Exception {

        JobCode jobCode = JobCode.PM;
        String step1 = jobCode.getJobCode();
        String step2 = jobCode.getDetailCode();

        // "기획·전략" -> "경영·비즈니스"
        WebElement PMFilter = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("label[for='duty_step1_" + step1 + "']")));
        PMFilter.click();
        String selector = "label[for='duty_step2_" + step2 + "']";

        WebElement jobCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
        wait.until(ExpectedConditions.elementToBeClickable(jobCheckbox)).click();

        searchAndSort(wait);
    }

    public void selectDesignerFilter(WebDriverWait wait) throws Exception {

        JobCode jobCode = JobCode.디자인;
        String step1 = jobCode.getJobCode();
        String step2 = jobCode.getDetailCode();

        // "디자인" -> "그래픽디자이너"
        WebElement PMFilter = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("label[for='duty_step1_" + step1 + "']")));
        PMFilter.click();
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
