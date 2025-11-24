package jobkorea.embedding.service;

import jobkorea.embedding.dto.RecruitmentDTO;
import jobkorea.embedding.entity.Recruitment;
import jobkorea.embedding.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final LLMService llmService;
    private final EmbeddingService embeddingService;
    private final RecruitmentRepository recruitmentRepository;

    @Transactional
    public void saveRecruitment(String recruitmentText) {

        // 1. 요약본으로부터 DTO 추출
        RecruitmentDTO recruitmentDTO = llmService.getRecruitment(recruitmentText);

        // 2. 엔티티 컨버팅
        Recruitment recruitment = Recruitment.builder()
                .postId(recruitmentDTO.getPostId())
                .companyName(recruitmentDTO.getCompanyName())
                .companySize(recruitmentDTO.getCompanySize())
                .title(recruitmentDTO.getTitle())
                .description(recruitmentDTO.getDescription())
                .employmentType(recruitmentDTO.getEmploymentType())
                .education(recruitmentDTO.getEducation())
                .siteLink(recruitmentDTO.getSiteLink())
                .startDate(recruitmentDTO.getStartDate())
                .endDate(recruitmentDTO.getEndDate())
                .build();

        // 3. 엔티티 RDB 저장
        recruitmentRepository.save(recruitment);

        // 4. 요약본 벡터DB 저장
        embeddingService.embedRecruitmentPost(recruitmentText, recruitment);
    }
}
