package jobkorea.crawler.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

@Component
public class WebDriverFactory {

    public WebDriver createChromeDriver() {

        // 1. WebDriverManager를 사용하여 ChromeDriver 자동 설정
        WebDriverManager.chromedriver().setup();

        // 2. 요청한 ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized"); // 브라우저 전체 화면 표시
        options.addArguments("--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");  // 웹소켓 차단 방지
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox");  // 리소스 제한 해결
        options.addArguments("--disable-blink-features=AutomationControlled");  // 봇 탐지 우회
        options.addArguments("--disable-features=NetworkService");  // 웹소켓 연결 문제 해결
        options.addArguments("--remote-debugging-port=9222");  // 웹소켓 디버깅 활성화
        options.addArguments("--disable-popup-blocking"); // 팝업 미사용
        // 멀티스레딩 시 포트 충돌 처리 필요
        
        // (선택사항) Headless 모드 (UI 없이 백그라운드에서 실행)
        // options.addArguments("--headless");

        // 3. 옵션을 적용하여 ChromeDriver 인스턴스 생성 및 반환
        return new ChromeDriver(options);
    }
}
