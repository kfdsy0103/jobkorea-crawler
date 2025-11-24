package jobkorea.crawler.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ChatClient chatClient;

    public AIService(ChatClient.Builder clientBuilder) {
        this.chatClient = clientBuilder.build();
    }

    public String getSummationText(String text) {
        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                         주어진 채용 공고를 분석하여 직무별로 요구되는 '직무 핵심 역량(KPI)'을 중점적으로 요약본을 작성하세요.
                         '직무 핵심 역량(KPI)'이란, '실제 담당 업무', '요구 사항', '우대 사항' 등에서 '역량', '능력', '경험', '이해도'와 같은 맥락을 의미합니다.
                         예시) 벡엔드 : "RESTful API 개발 및 설계 능력", "비동기 메시지 큐 개발 경험", "MSA 아키텍처에 대한 이해도"
                        
                         채용 공고 이미지 OCR 내용은 구조화되어있지 않습니다. 따라서 최대한 구조화하여 직무별 KPI를 이해하세요.
                         출력 시 특수문자는 '-'만 허용합니다.
                         KPI를 최대한 많이 파악하세요.
                         아래 내용을 반드시 포함하세요.
                         오래 생각하세요.
                        
                         - 공고 id
                         - 공고 제목
                         - 회사명 (㈜는 제외)
                         - 회사 규모 (대기업 등, 사원 수 제외)
                         - 고용 형태 (정규직 등)
                         - 학력 (학사 등)
                         - 채용 공고 링크 (오직 링크만 출력)
                         - 시작일
                         - 마감일
                         - 채용 직무와 직무 별 KPI
                         - 기타 내용 (인재상이 있다면 이곳에 포함)
                        """)
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text(text)
                .build();

        String summation = chatClient.prompt()
                .messages(systemMessage, userMessage)
                .call()
                .content();

        return summation;
    }
}
