package jobkorea.embedding.service;

import jobkorea.embedding.dto.RecruitmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;

    public RecruitmentDTO getRecruitment(String recruitmentText) {

        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                        너는 채용 공고 텍스트를 분석하여 지정된 데이터 스키마(JSON)로 변환하는 전문 파서(Parser)야.
                        
                        회사 규모는 '대기업', '중견기업', '중소기업', '기타' 중 한가지로 분류해.
                        고용 형태는 '아르바이트', '인턴', '프리랜서', '계약직', '정규직' 중 한가지로 분류해.
                        학력은 '무관', '고졸', '전문학사', '학사', '석사', '박사' 중 한가지로 분류해.
                        description은 어떤 KPI가 중점인지 요약해.
                        Job의 name은 채용 공고 제목을 참고하여 '백엔드, 프론트엔드, PM, 디자이너' 중 한가지로 분류해.
                        직무를 4가지 중 하나로 분류하지 못한다면 해당 Job은 제외하세요.
                        """)
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text(recruitmentText)
                .build();

        RecruitmentDTO recruitmentDTO = chatClient.prompt()
                .messages(systemMessage, userMessage)
                .call()
                .entity(RecruitmentDTO.class);

        System.out.println("recruitmentDTO: " + recruitmentDTO);

        return recruitmentDTO;
    }
}
