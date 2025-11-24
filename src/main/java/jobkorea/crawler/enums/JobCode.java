package jobkorea.crawler.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobCode {
    웹개발("10031", "1000231"),
    PM("10026", "1000185"),
    디자인("10032", "1000248");

    private final String jobCode;
    private final String detailCode;
}
