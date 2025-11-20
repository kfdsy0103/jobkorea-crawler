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

    public String getSummationText(String text) {
        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                         주어진 채용 공고를 분석하여 직무별로 요구되는 '직무 핵심 역량(KPI)'을 중점적으로 요약본을 작성하세요.
                         '직무 핵심 역량(KPI)'이란, '실제 담당 업무', '요구 사항', '우대 사항' 등에서 '역량', '능력', '경험', '이해도'와 같은 맥락을 의미합니다.
                         예시) 벡엔드 : "RESTful API 개발 및 설계 능력", "비동기 메시지 큐 개발 경험", "MSA 아키텍처에 대한 이해도"
                        
                         채용 공고 이미지 OCR 내용은 구조화되어있지 않습니다. 따라서 최대한 구조화하여 직무별 KPI를 이해하세요.
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

        String summation = chatClent.prompt()
                .messages(systemMessage, userMessage)
                .call()
                .content();

        return summation;
    }

    public Recruitment getExplanationText(String text) {

        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                        너는 채용 공고 텍스트를 분석하여 지정된 데이터 스키마(JSON)로 변환하는 전문 파서(Parser)야.
                        아래의 예시를 참고하여 데이터를 추출해줘.
                        회사명 앞의 (주)는 제거해줘.
                        
                        예시)
                        결과는 오직 아래의 JSON 형식으로만 출력해야 해.
                        KPI은 2어절 이상의 명사구 형식이어야 해
                        {
                          "id": "12345678",
                          "company": "회사명",
                          "companySize": "대기업",
                          "title": "공고 제목",
                          "description": "공고 핵심 요약",
                          "employmentType": "정규직",
                          "education": "학사",
                          "siteLink": "채용 링크",
                          "startDate": "2025-11-18",
                          "endDate": "2025-12-18",
                          "jobs": [
                            {
                              "jobType": "백엔드",
                              "kpi": ["RESTful API 개발 및 설계 경험", "MSA 아키텍처에 대한 이해도", "비동기 메시지 큐 사용 경험", ...]
                            }
                          ]
                        }
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
