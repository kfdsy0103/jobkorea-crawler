package jobkorea.crawler.dto;

import java.util.List;
import jobkorea.crawler.enums.JobType;
import lombok.Data;

@Data
public class Job {
    private JobType jobType;
    private List<String> kpi; // 직무 별 KPI 키워드
}