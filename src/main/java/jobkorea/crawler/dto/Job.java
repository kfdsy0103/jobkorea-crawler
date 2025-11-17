package jobkorea.crawler.dto;

import java.util.List;
import jobkorea.crawler.enums.JobType;
import lombok.Data;

@Data
public class Job {
    private JobType jobType;
    private List<String> KPI;
}