package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
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
import com.company.learninghub.learn.mapper.LearnProgressMapper;
import com.company.learninghub.learn.repository.LearnLearningEnrollmentRepository;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnStageProgressRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningProgressServiceTest {

    @Mock
    private LearnTechnologyRepository technologyRepository;

    @Mock
    private LearnRoadmapRepository roadmapRepository;

    @Mock
    private LearnRoadmapStageRepository stageRepository;

    @Mock
    private LearnLearningEnrollmentRepository enrollmentRepository;

    @Mock
    private LearnStageProgressRepository stageProgressRepository;

    @Mock
    private com.company.learninghub.user.repository.UserRepository userRepository;

    private LearningProgressService progressService;

    private User employeeUser;
    private AuthenticatedUser employeePrincipal;
    private LearnTechnology technology;
    private LearnRoadmap roadmap;
    private LearnRoadmapStage stageOne;
    private LearnRoadmapStage stageTwo;

    @BeforeEach
    void setUp() {
        progressService = new LearningProgressService(
                userRepository,
                technologyRepository,
                roadmapRepository,
                stageRepository,
                enrollmentRepository,
                stageProgressRepository,
                new LearnProgressMapper()
        );

        employeeUser = new User("EMP001", "employee@learninghub.local", "Employee", "$2a$12$hash");
        ReflectionTestUtils.setField(employeeUser, "id", UUID.randomUUID());
        employeeUser.assignRole(new Role(RoleName.EMPLOYEE));
        employeePrincipal = AuthenticatedUser.from(employeeUser);

        technology = new LearnTechnology(
                "java",
                "Java",
                "Java",
                "Java language",
                TechnologyCategory.BACKEND,
                TechnologyDifficulty.INTERMEDIATE,
                TechnologyStatus.PUBLISHED,
                true,
                employeeUser
        );
        ReflectionTestUtils.setField(technology, "id", UUID.randomUUID());

        roadmap = new LearnRoadmap(technology);
        ReflectionTestUtils.setField(roadmap, "id", UUID.randomUUID());

        stageOne = stage(roadmap, 1, "introduction");
        stageTwo = stage(roadmap, 2, "core-concepts");
    }

    @Test
    void enrollCreatesInProgressEnrollmentAtFirstStage() {
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.of(technology));
        when(enrollmentRepository.existsByUserIdAndTechnology_SlugAndStatusIn(
                employeePrincipal.getId(),
                technology.getSlug(),
                EnumSet.of(
                        LearningEnrollmentStatus.NOT_STARTED,
                        LearningEnrollmentStatus.IN_PROGRESS,
                        LearningEnrollmentStatus.COMPLETED
                )
        )).thenReturn(false);
        when(userRepository.findById(employeePrincipal.getId())).thenReturn(Optional.of(employeeUser));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stageOne, stageTwo));
        when(enrollmentRepository.save(any(LearnLearningEnrollment.class))).thenAnswer(invocation -> {
            LearnLearningEnrollment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        EnrollmentResponse response = progressService.enroll(
                new CreateEnrollmentRequest(technology.getId()),
                employeePrincipal
        );

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(response.currentStageOrder()).isEqualTo(1);
        assertThat(response.progressPercent()).isZero();

        ArgumentCaptor<LearnLearningEnrollment> captor = ArgumentCaptor.forClass(LearnLearningEnrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(LearningEnrollmentStatus.IN_PROGRESS);
        assertThat(captor.getValue().getCurrentStage().getSlug()).isEqualTo("introduction");
    }

    @Test
    void enrollRejectsDuplicateActiveEnrollment() {
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(enrollmentRepository.existsByUserIdAndTechnology_SlugAndStatusIn(
                employeePrincipal.getId(),
                technology.getSlug(),
                EnumSet.of(
                        LearningEnrollmentStatus.NOT_STARTED,
                        LearningEnrollmentStatus.IN_PROGRESS,
                        LearningEnrollmentStatus.COMPLETED
                )
        )).thenReturn(true);

        assertThatThrownBy(() -> progressService.enroll(
                new CreateEnrollmentRequest(technology.getId()),
                employeePrincipal
        ))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("already have an active learning journey");
    }

    @Test
    void completeStageRequiresSequentialOrder() {
        LearnLearningEnrollment enrollment = activeEnrollment();
        when(enrollmentRepository.findByIdAndUserId(enrollment.getId(), employeePrincipal.getId()))
                .thenReturn(Optional.of(enrollment));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stageOne, stageTwo));
        when(stageProgressRepository.findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> progressService.completeStage(
                enrollment.getId(),
                new CompleteStageRequest(stageTwo.getId()),
                employeePrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("in order");
    }

    @Test
    void completeStageAdvancesToNextStage() {
        LearnLearningEnrollment enrollment = activeEnrollment();
        when(enrollmentRepository.findByIdAndUserId(enrollment.getId(), employeePrincipal.getId()))
                .thenReturn(Optional.of(enrollment));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stageOne, stageTwo));
        when(stageProgressRepository.findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId()))
                .thenReturn(List.of())
                .thenReturn(List.of(new com.company.learninghub.learn.domain.LearnStageProgress(
                        enrollment,
                        stageOne,
                        Instant.now()
                )));
        when(stageProgressRepository.existsByEnrollmentIdAndStageId(enrollment.getId(), stageOne.getId())).thenReturn(false);
        when(stageProgressRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentRepository.save(any(LearnLearningEnrollment.class))).thenAnswer(invocation -> {
            LearnLearningEnrollment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        EnrollmentResponse response = progressService.completeStage(
                enrollment.getId(),
                new CompleteStageRequest(stageOne.getId()),
                employeePrincipal
        );

        assertThat(response.progressPercent()).isEqualTo(50);
        assertThat(response.currentStageOrder()).isEqualTo(2);
        verify(stageProgressRepository).save(any());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void completeFinalStageMarksEnrollmentCompleted() {
        LearnLearningEnrollment enrollment = activeEnrollment();
        enrollment.advanceToStage(stageTwo, Instant.now());

        when(enrollmentRepository.findByIdAndUserId(enrollment.getId(), employeePrincipal.getId()))
                .thenReturn(Optional.of(enrollment));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stageOne, stageTwo));
        com.company.learninghub.learn.domain.LearnStageProgress firstStageProgress =
                new com.company.learninghub.learn.domain.LearnStageProgress(enrollment, stageOne, Instant.now());
        com.company.learninghub.learn.domain.LearnStageProgress secondStageProgress =
                new com.company.learninghub.learn.domain.LearnStageProgress(enrollment, stageTwo, Instant.now());

        when(stageProgressRepository.findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId()))
                .thenReturn(List.of(firstStageProgress))
                .thenReturn(List.of(firstStageProgress, secondStageProgress));
        when(stageProgressRepository.existsByEnrollmentIdAndStageId(enrollment.getId(), stageTwo.getId())).thenReturn(false);
        when(stageProgressRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentRepository.save(any(LearnLearningEnrollment.class))).thenAnswer(invocation -> {
            LearnLearningEnrollment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        EnrollmentResponse response = progressService.completeStage(
                enrollment.getId(),
                new CompleteStageRequest(stageTwo.getId()),
                employeePrincipal
        );

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.progressPercent()).isEqualTo(100);
        assertThat(enrollment.getStatus()).isEqualTo(LearningEnrollmentStatus.COMPLETED);
    }

    @Test
    void getTechnologyProgressRequiresEnrollment() {
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.of(technology));
        when(enrollmentRepository.findByUserIdAndTechnology_SlugAndStatusIn(
                employeePrincipal.getId(),
                technology.getSlug(),
                EnumSet.of(LearningEnrollmentStatus.NOT_STARTED, LearningEnrollmentStatus.IN_PROGRESS)
        )).thenReturn(Optional.empty());
        when(enrollmentRepository.findByUserIdAndTechnology_SlugAndStatusIn(
                employeePrincipal.getId(),
                technology.getSlug(),
                EnumSet.of(LearningEnrollmentStatus.COMPLETED)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.getTechnologyProgress(technology.getId(), employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private LearnLearningEnrollment activeEnrollment() {
        LearnLearningEnrollment enrollment = new LearnLearningEnrollment(employeeUser, technology, Instant.now());
        ReflectionTestUtils.setField(enrollment, "id", UUID.randomUUID());
        enrollment.startLearning(stageOne, Instant.now());
        return enrollment;
    }

    private LearnRoadmapStage stage(LearnRoadmap roadmap, int order, String slug) {
        LearnRoadmapStage stage = LearnRoadmapStage.create();
        stage.setRoadmap(roadmap);
        stage.setStageOrder(order);
        stage.setSlug(slug);
        stage.setTitle(slug);
        stage.setDescription("Description");
        stage.setEstimatedEffort("1 week");
        ReflectionTestUtils.setField(stage, "id", UUID.randomUUID());
        return stage;
    }
}
