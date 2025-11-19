package jobkorea.crawler.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecruitmentPost {
    private String url;
    private String jobId;
    private String title;
    private String companyName;
    private String recruitmentDetail;
    private String qualification;
    private String corpInfo;
    private String recruitmentOutline;
    private String timeInfo;

    public String toFormattedString() {
        return "\n--- 1. 공고 URL 및 공고 ID ---\n" + url + ", " + jobId
                + "\n--- 2. 채용 공고 제목 ---\n" + title
                + "\n--- 3. 기업명 ---\n" + companyName
                + "\n--- 4. 상세 모집 요강 ---\n" + recruitmentDetail
                + "\n--- 5. 지원 자격 ---\n" + qualification
                + "\n--- 6. 기업 정보 ---\n" + corpInfo
                + "\n--- 7. 모집 요강 ---\n" + recruitmentOutline
                + "\n--- 8. 시작일 및 마감일 ---\n" + timeInfo;
    }
}
