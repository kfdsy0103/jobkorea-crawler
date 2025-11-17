package jobkorea.crawler.dto;

import java.time.LocalDate;
import java.util.List;
import jobkorea.crawler.enums.CompanySize;
import jobkorea.crawler.enums.Education;
import jobkorea.crawler.enums.EmploymentType;
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
    private List<Job> jobs;
}