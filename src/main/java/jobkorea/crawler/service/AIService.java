package jobkorea.crawler.service;

import jobkorea.crawler.dto.Recruitment;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ChatClient chatClent;

    public AIService(ChatClient.Builder clientBuilder) {
        this.chatClent = clientBuilder.build();
    }

    public Recruitment getExplanationText(String text) {

        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                        채용 공고에 관한 크롤링 자료입니다.
                        직무별로 요구되는 핵심 역량 (KPI)를 중점적으로 분석하세요.
                        직무(JobType)별 핵심 역량(KPI)을 키워드 중심으로 정리하세요.
                        회사명 앞의 (주)는 제거해주세요.
                        """)
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text(text)
                .build();

        Recruitment dto = chatClent.prompt()
                .messages(systemMessage, userMessage)
                .call()
                .entity(Recruitment.class);

        return dto;
    }
}
