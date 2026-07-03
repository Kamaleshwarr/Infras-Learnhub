package com.company.learninghub.learn.integration;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.learn.domain.LearnLearningEnrollment;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearningEnrollmentStatus;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.CompleteStageRequest;
import com.company.learninghub.learn.dto.CreateEnrollmentRequest;
import com.company.learninghub.learn.dto.EnrollmentResponse;
import com.company.learninghub.learn.dto.JourneyResponse;
import com.company.learninghub.learn.repository.LearnLearningEnrollmentRepository;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnStageProgressRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.learn.service.LearningProgressService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class LearningProgressIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_progress")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.catalog.import.enabled", () -> "false");
    }

    @Autowired
    private LearningProgressService progressService;

    @Autowired
    private LearnTechnologyRepository technologyRepository;

    @Autowired
    private LearnRoadmapRepository roadmapRepository;

    @Autowired
    private LearnRoadmapStageRepository stageRepository;

    @Autowired
    private LearnLearningEnrollmentRepository enrollmentRepository;

    @Autowired
    private LearnStageProgressRepository stageProgressRepository;

    @Autowired
    private UserRepository userRepository;

    private AuthenticatedUser employeePrincipal;
    private LearnTechnology technology;
    private LearnRoadmapStage stageOne;
    private LearnRoadmapStage stageTwo;

    @BeforeEach
    void setUp() {
        stageProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        stageRepository.deleteAll();
        roadmapRepository.deleteAll();
        technologyRepository.deleteAll();

        User employee = userRepository.findByEmailIgnoreCase("employee@learninghub.local").orElseThrow();
        employeePrincipal = AuthenticatedUser.from(employee);

        technology = new LearnTechnology(
                "progress-java",
                "Progress Java",
                "Java",
                "Progress test technology",
                TechnologyCategory.BACKEND,
                TechnologyDifficulty.BEGINNER,
                TechnologyStatus.PUBLISHED,
                true,
                employee
        );
        technologyRepository.save(technology);

        LearnRoadmap roadmap = new LearnRoadmap(technology);
        roadmap.applyCatalogData("1.0.0", "Test roadmap", "test", null, null);
        roadmapRepository.save(roadmap);

        stageOne = createStage(roadmap, 1, "stage-one");
        stageTwo = createStage(roadmap, 2, "stage-two");
        stageRepository.saveAll(List.of(stageOne, stageTwo));
    }

    @Test
    void enrollCompleteStagesAndResumeJourney() {
        EnrollmentResponse enrollment = progressService.enroll(
                new CreateEnrollmentRequest(technology.getId()),
                employeePrincipal
        );

        assertThat(enrollment.status()).isEqualTo("IN_PROGRESS");
        assertThat(enrollment.currentStageOrder()).isEqualTo(1);

        assertThatThrownBy(() -> progressService.completeStage(
                UUID.fromString(enrollment.id()),
                new CompleteStageRequest(stageTwo.getId()),
                employeePrincipal
        )).isInstanceOf(IllegalArgumentException.class);

        EnrollmentResponse afterStageOne = progressService.completeStage(
                UUID.fromString(enrollment.id()),
                new CompleteStageRequest(stageOne.getId()),
                employeePrincipal
        );
        assertThat(afterStageOne.progressPercent()).isEqualTo(50);
        assertThat(afterStageOne.currentStageOrder()).isEqualTo(2);

        EnrollmentResponse completed = progressService.completeStage(
                UUID.fromString(enrollment.id()),
                new CompleteStageRequest(stageTwo.getId()),
                employeePrincipal
        );
        assertThat(completed.status()).isEqualTo("COMPLETED");
        assertThat(completed.progressPercent()).isEqualTo(100);

        JourneyResponse journey = progressService.getJourney(employeePrincipal);
        assertThat(journey.continueLearning()).isNull();
        assertThat(journey.completed()).hasSize(1);

        LearnLearningEnrollment persisted = enrollmentRepository.findById(UUID.fromString(enrollment.id())).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(LearningEnrollmentStatus.COMPLETED);
        assertThat(stageProgressRepository.countByEnrollmentId(persisted.getId())).isEqualTo(2);
    }

    private LearnRoadmapStage createStage(LearnRoadmap roadmap, int order, String slug) {
        LearnRoadmapStage stage = LearnRoadmapStage.create();
        stage.setRoadmap(roadmap);
        stage.setStageOrder(order);
        stage.setSlug(slug);
        stage.setTitle(slug);
        stage.setDescription("Description");
        stage.setEstimatedEffort("1 week");
        return stage;
    }
}
