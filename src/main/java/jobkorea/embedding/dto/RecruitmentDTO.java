package jobkorea.embedding.dto;

import java.time.LocalDate;
import java.util.List;
import jobkorea.embedding.enums.CompanySize;
import jobkorea.embedding.enums.Education;
import jobkorea.embedding.enums.EmploymentType;
import lombok.Data;

@Data
public class RecruitmentDTO {
    private Long postId;
    private String companyName;
    private CompanySize companySize;
    private String title;
    private String description;
    private EmploymentType employmentType;
    private Education education;
    private String siteLink;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<JobDTO> jobs;

    @Data
    public static class JobDTO {
        private String name;
        private List<String> content;
    }
}
