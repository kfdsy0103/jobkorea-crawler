package jobkorea.crawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClent;

    public String getExplanationText(String text) {

        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                        제공되는 내용을 보고 설명문으로 작성하세요.
                        """)
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text(text)
                .build();

        String answer = chatClent.prompt()
                .messages(systemMessage, userMessage)
                .call()
                .content();

        return answer;
    }
}
