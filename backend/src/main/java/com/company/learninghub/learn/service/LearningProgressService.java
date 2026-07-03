package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.domain.LearnLearningEnrollment;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnStageProgress;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearningEnrollmentStatus;
import com.company.learninghub.learn.dto.CompleteStageRequest;
import com.company.learninghub.learn.dto.ContinueLearningResponse;
import com.company.learninghub.learn.dto.CreateEnrollmentRequest;
import com.company.learninghub.learn.dto.EnrollmentResponse;
import com.company.learninghub.learn.dto.JourneyResponse;
import com.company.learninghub.learn.dto.TechnologyProgressResponse;
import com.company.learninghub.learn.mapper.LearnProgressMapper;
import com.company.learninghub.learn.repository.LearnLearningEnrollmentRepository;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnStageProgressRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LearningProgressService {

    private static final Set<LearningEnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES = EnumSet.of(
            LearningEnrollmentStatus.NOT_STARTED,
            LearningEnrollmentStatus.IN_PROGRESS,
            LearningEnrollmentStatus.COMPLETED
    );

    private static final Set<LearningEnrollmentStatus> CONTINUE_LEARNING_STATUSES = EnumSet.of(
            LearningEnrollmentStatus.NOT_STARTED,
            LearningEnrollmentStatus.IN_PROGRESS
    );

    private final UserRepository userRepository;
    private final LearnTechnologyRepository technologyRepository;
    private final LearnRoadmapRepository roadmapRepository;
    private final LearnRoadmapStageRepository stageRepository;
    private final LearnLearningEnrollmentRepository enrollmentRepository;
    private final LearnStageProgressRepository stageProgressRepository;
    private final LearnProgressMapper progressMapper;

    public LearningProgressService(
            UserRepository userRepository,
            LearnTechnologyRepository technologyRepository,
            LearnRoadmapRepository roadmapRepository,
            LearnRoadmapStageRepository stageRepository,
            LearnLearningEnrollmentRepository enrollmentRepository,
            LearnStageProgressRepository stageProgressRepository,
            LearnProgressMapper progressMapper
    ) {
        this.userRepository = userRepository;
        this.technologyRepository = technologyRepository;
        this.roadmapRepository = roadmapRepository;
        this.stageRepository = stageRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.stageProgressRepository = stageProgressRepository;
        this.progressMapper = progressMapper;
    }

    @Transactional
    public EnrollmentResponse enroll(CreateEnrollmentRequest request, AuthenticatedUser authenticatedUser) {
        Instant now = Instant.now();
        LearnTechnology technology = findVisibleTechnology(request.technologyId(), authenticatedUser);
        assertRoadmapAvailable(technology);

        if (enrollmentRepository.existsByUserIdAndTechnology_SlugAndStatusIn(
                authenticatedUser.getId(),
                technology.getSlug(),
                ACTIVE_ENROLLMENT_STATUSES
        )) {
            throw new BusinessConflictException("You already have an active learning journey for this technology.");
        }

        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));

        LearnRoadmap roadmap = roadmapRepository.findByTechnologySlug(technology.getSlug())
                .filter(LearnRoadmap::isCatalogPresent)
                .orElseThrow(() -> new ResourceNotFoundException("No roadmap is available for this technology."));

        List<LearnRoadmapStage> stages = stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId());
        if (stages.isEmpty()) {
            throw new ResourceNotFoundException("No roadmap is available for this technology.");
        }

        LearnLearningEnrollment enrollment = new LearnLearningEnrollment(user, technology, now);
        enrollment.startLearning(stages.getFirst(), now);
        enrollmentRepository.save(enrollment);

        return progressMapper.toEnrollmentResponse(enrollment, stages, List.of());
    }

    @Transactional
    public EnrollmentResponse startLearning(UUID enrollmentId, AuthenticatedUser authenticatedUser) {
        Instant now = Instant.now();
        LearnLearningEnrollment enrollment = findOwnedEnrollment(enrollmentId, authenticatedUser.getId());

        if (enrollment.getStatus() == LearningEnrollmentStatus.IN_PROGRESS) {
            return toEnrollmentResponse(enrollment);
        }
        if (enrollment.getStatus() != LearningEnrollmentStatus.NOT_STARTED) {
            throw new BusinessConflictException("This enrollment cannot be started.");
        }

        List<LearnRoadmapStage> stages = loadStagesForTechnology(enrollment.getTechnology());
        enrollment.startLearning(stages.getFirst(), now);
        enrollmentRepository.save(enrollment);

        return progressMapper.toEnrollmentResponse(enrollment, stages, List.of());
    }

    @Transactional
    public EnrollmentResponse completeStage(
            UUID enrollmentId,
            CompleteStageRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        Instant now = Instant.now();
        LearnLearningEnrollment enrollment = findOwnedEnrollment(enrollmentId, authenticatedUser.getId());

        if (enrollment.getStatus() == LearningEnrollmentStatus.COMPLETED) {
            throw new BusinessConflictException("This roadmap is already complete.");
        }
        if (enrollment.getStatus() == LearningEnrollmentStatus.LEFT) {
            throw new BusinessConflictException("This enrollment is no longer active.");
        }

        List<LearnRoadmapStage> stages = loadStagesForTechnology(enrollment.getTechnology());
        List<LearnStageProgress> completedProgress = stageProgressRepository
                .findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId());

        LearnRoadmapStage nextStage = findNextIncompleteStage(stages, completedProgress);
        if (!nextStage.getId().equals(request.stageId())) {
            throw new IllegalArgumentException("Stages must be completed in order.");
        }

        if (stageProgressRepository.existsByEnrollmentIdAndStageId(enrollment.getId(), request.stageId())) {
            throw new BusinessConflictException("This stage is already complete.");
        }

        if (enrollment.getStatus() == LearningEnrollmentStatus.NOT_STARTED) {
            enrollment.startLearning(stages.getFirst(), now);
        }

        LearnStageProgress stageProgress = new LearnStageProgress(enrollment, nextStage, now);
        stageProgressRepository.save(stageProgress);
        completedProgress = stageProgressRepository.findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId());

        if (completedProgress.size() >= stages.size()) {
            enrollment.complete(now);
        } else {
            LearnRoadmapStage followingStage = findNextIncompleteStage(stages, completedProgress);
            enrollment.advanceToStage(followingStage, now);
        }

        enrollmentRepository.save(enrollment);
        return progressMapper.toEnrollmentResponse(enrollment, stages, completedProgress);
    }

    @Transactional
    public void leaveEnrollment(UUID enrollmentId, AuthenticatedUser authenticatedUser) {
        LearnLearningEnrollment enrollment = findOwnedEnrollment(enrollmentId, authenticatedUser.getId());

        if (!enrollment.isActive()) {
            throw new BusinessConflictException("This enrollment is not active.");
        }

        enrollment.leave(Instant.now());
        enrollmentRepository.save(enrollment);
    }

    @Transactional(readOnly = true)
    public JourneyResponse getJourney(AuthenticatedUser authenticatedUser) {
        List<LearnLearningEnrollment> active = enrollmentRepository.findByUserIdAndStatusInOrderByLastActivityDesc(
                authenticatedUser.getId(),
                EnumSet.of(LearningEnrollmentStatus.NOT_STARTED, LearningEnrollmentStatus.IN_PROGRESS)
        );
        List<LearnLearningEnrollment> completed = enrollmentRepository.findByUserIdAndStatusInOrderByLastActivityDesc(
                authenticatedUser.getId(),
                EnumSet.of(LearningEnrollmentStatus.COMPLETED)
        );
        List<LearnLearningEnrollment> left = enrollmentRepository.findByUserIdAndStatusInOrderByLastActivityDesc(
                authenticatedUser.getId(),
                EnumSet.of(LearningEnrollmentStatus.LEFT)
        );

        ContinueLearningResponse continueLearning = active.stream()
                .map(this::toContinueLearningResponse)
                .filter(response -> response != null)
                .findFirst()
                .orElse(null);

        return new JourneyResponse(
                continueLearning,
                active.stream().map(this::toEnrollmentResponse).toList(),
                completed.stream().map(this::toEnrollmentResponse).toList(),
                left.stream().map(this::toEnrollmentResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public TechnologyProgressResponse getTechnologyProgress(UUID technologyId, AuthenticatedUser authenticatedUser) {
        LearnTechnology technology = findVisibleTechnology(technologyId, authenticatedUser);

        LearnLearningEnrollment enrollment = enrollmentRepository
                .findByUserIdAndTechnology_SlugAndStatusIn(
                        authenticatedUser.getId(),
                        technology.getSlug(),
                        CONTINUE_LEARNING_STATUSES
                )
                .or(() -> enrollmentRepository.findByUserIdAndTechnology_SlugAndStatusIn(
                        authenticatedUser.getId(),
                        technology.getSlug(),
                        EnumSet.of(LearningEnrollmentStatus.COMPLETED)
                ))
                .orElseThrow(() -> new ResourceNotFoundException("No learning progress found for this technology."));

        List<LearnRoadmapStage> stages = loadStagesForTechnology(technology);
        List<LearnStageProgress> completedProgress = stageProgressRepository
                .findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId());

        return progressMapper.toTechnologyProgressResponse(enrollment, stages, completedProgress);
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getActiveEnrollment(UUID technologyId, AuthenticatedUser authenticatedUser) {
        LearnTechnology technology = findVisibleTechnology(technologyId, authenticatedUser);

        LearnLearningEnrollment enrollment = enrollmentRepository
                .findByUserIdAndTechnology_SlugAndStatusIn(
                        authenticatedUser.getId(),
                        technology.getSlug(),
                        ACTIVE_ENROLLMENT_STATUSES
                )
                .orElseThrow(() -> new ResourceNotFoundException("No active enrollment found for this technology."));

        return toEnrollmentResponse(enrollment);
    }

    private EnrollmentResponse toEnrollmentResponse(LearnLearningEnrollment enrollment) {
        List<LearnRoadmapStage> stages = loadStagesForTechnology(enrollment.getTechnology());
        List<LearnStageProgress> completedProgress = stageProgressRepository
                .findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId());
        return progressMapper.toEnrollmentResponse(enrollment, stages, completedProgress);
    }

    private ContinueLearningResponse toContinueLearningResponse(LearnLearningEnrollment enrollment) {
        List<LearnRoadmapStage> stages = loadStagesForTechnology(enrollment.getTechnology());
        List<LearnStageProgress> completedProgress = stageProgressRepository
                .findByEnrollmentIdOrderByStageStageOrderAsc(enrollment.getId());
        return progressMapper.toContinueLearningResponse(enrollment, stages, completedProgress);
    }

    private LearnLearningEnrollment findOwnedEnrollment(UUID enrollmentId, UUID userId) {
        return enrollmentRepository.findByIdAndUserId(enrollmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment was not found."));
    }

    private LearnTechnology findVisibleTechnology(UUID technologyId, AuthenticatedUser authenticatedUser) {
        LearnTechnology technology = technologyRepository.findById(technologyId)
                .orElseThrow(() -> new ResourceNotFoundException("Technology was not found."));

        if (authenticatedUser != null && authenticatedUser.getRoleNames().contains(RoleName.ADMIN)) {
            return technology;
        }
        if (!technology.isVisibleToEmployees()) {
            throw new ResourceNotFoundException("Technology was not found.");
        }
        return technology;
    }

    private void assertRoadmapAvailable(LearnTechnology technology) {
        if (!technology.isCatalogPresent()) {
            throw new ResourceNotFoundException("Technology was not found.");
        }
        roadmapRepository.findByTechnologySlug(technology.getSlug())
                .filter(LearnRoadmap::isCatalogPresent)
                .orElseThrow(() -> new ResourceNotFoundException("No roadmap is available for this technology."));
    }

    private List<LearnRoadmapStage> loadStagesForTechnology(LearnTechnology technology) {
        LearnRoadmap roadmap = roadmapRepository.findByTechnologySlug(technology.getSlug())
                .filter(LearnRoadmap::isCatalogPresent)
                .orElseThrow(() -> new ResourceNotFoundException("No roadmap is available for this technology."));
        return stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId());
    }

    private LearnRoadmapStage findNextIncompleteStage(
            List<LearnRoadmapStage> stages,
            List<LearnStageProgress> completedProgress
    ) {
        Set<UUID> completedStageIds = completedProgress.stream()
                .map(progress -> progress.getStage().getId())
                .collect(java.util.stream.Collectors.toSet());

        return stages.stream()
                .sorted(java.util.Comparator.comparingInt(LearnRoadmapStage::getStageOrder))
                .filter(stage -> !completedStageIds.contains(stage.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessConflictException("All stages are already complete."));
    }
}
