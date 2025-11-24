package jobkorea.embedding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import jobkorea.embedding.enums.CompanySize;
import jobkorea.embedding.enums.Education;
import jobkorea.embedding.enums.EmploymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "recruitment")
public class Recruitment {

    @Id
    @Column(name = "recruitment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanySize companySize;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    private Education education;

    @Column(nullable = false)
    private String siteLink;

    private LocalDate startDate;
    private LocalDate endDate;
}
