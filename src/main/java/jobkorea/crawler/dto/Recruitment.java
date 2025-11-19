package jobkorea.crawler.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class Recruitment {
    private String id; // 식별 번호
    private String company; // 회사명 (주 제거, 영어면 영어 그대로)
    private CompanySize companySize; // 기업 규모 (대기업, 중견기업, 중소기업, 기타)
    private String title; // 제목
    private String description; // 채용 공고 요약 (짧게 요약)
    private EmploymentType employmentType; // 고용형태 (인턴, 정규직)
    private Education education; // 학력 (무관, 고졸, 전문학사, 학사, 석사, 박사)
    private String siteLink; // 채용공고 링크
    private LocalDate startDate; // 시작일
    private LocalDate endDate; // 마감일
    private List<Job> jobs; // 직무 분야

    @Data
    public static class Job {
        private JobType jobType;
        private List<String> kpi; // 직무 별 KPI 키워드
    }

    public enum JobType {
        백엔드, 프론트엔드, 앱, 시스템, 네트워크, DBA, 보안, 게임, AI, 클라우드, 데이터, 하드웨어, 기타
    }

    public enum EmploymentType {
        아르바이트, 인턴, 프리랜서, 계약직, 정규직
    }

    public enum Education {
        무관, 고졸, 전문학사, 학사, 석사, 박사
    }

    public enum CompanySize {
        대기업, 중견기업, 중소기업, 기타
    }
}