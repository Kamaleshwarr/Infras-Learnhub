package com.company.learninghub.learn.mapper;

import com.company.learninghub.learn.domain.LearnLearningEnrollment;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnStageProgress;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearningEnrollmentStatus;
import com.company.learninghub.learn.dto.ContinueLearningResponse;
import com.company.learninghub.learn.dto.EnrollmentResponse;
import com.company.learninghub.learn.dto.TechnologyProgressResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LearnProgressMapper {

    public EnrollmentResponse toEnrollmentResponse(
            LearnLearningEnrollment enrollment,
            List<LearnRoadmapStage> stages,
            List<LearnStageProgress> completedProgress
    ) {
        ProgressSnapshot snapshot = buildSnapshot(enrollment, stages, completedProgress);
        LearnTechnology technology = enrollment.getTechnology();

        return new EnrollmentResponse(
                enrollment.getId().toString(),
                technology.getId().toString(),
                technology.getSlug(),
                technology.getName(),
                enrollment.getStatus().name(),
                formatInstant(enrollment.getEnrolledAt()),
                formatInstant(enrollment.getStartedAt()),
                formatInstant(enrollment.getLastActivityAt()),
                formatInstant(enrollment.getCompletedAt()),
                snapshot.progressPercent(),
                snapshot.currentStage() == null ? null : snapshot.currentStage().getId().toString(),
                snapshot.currentStage() == null ? null : snapshot.currentStage().getStageOrder(),
                snapshot.currentStage() == null ? null : snapshot.currentStage().getTitle(),
                snapshot.nextStage() == null ? null : snapshot.nextStage().getId().toString(),
                snapshot.nextStage() == null ? null : snapshot.nextStage().getStageOrder(),
                snapshot.nextStage() == null ? null : snapshot.nextStage().getTitle()
        );
    }

    public ContinueLearningResponse toContinueLearningResponse(
            LearnLearningEnrollment enrollment,
            List<LearnRoadmapStage> stages,
            List<LearnStageProgress> completedProgress
    ) {
        ProgressSnapshot snapshot = buildSnapshot(enrollment, stages, completedProgress);
        LearnTechnology technology = enrollment.getTechnology();
        LearnRoadmapStage currentStage = snapshot.currentStage();
        if (currentStage == null && enrollment.getStatus() == LearningEnrollmentStatus.COMPLETED) {
            return null;
        }
        LearnRoadmapStage displayStage = currentStage != null
                ? currentStage
                : stages.stream().min(Comparator.comparingInt(LearnRoadmapStage::getStageOrder)).orElseThrow();

        return new ContinueLearningResponse(
                enrollment.getId().toString(),
                technology.getId().toString(),
                technology.getSlug(),
                technology.getName(),
                displayStage.getId().toString(),
                displayStage.getStageOrder(),
                displayStage.getTitle(),
                snapshot.progressPercent()
        );
    }

    public TechnologyProgressResponse toTechnologyProgressResponse(
            LearnLearningEnrollment enrollment,
            List<LearnRoadmapStage> stages,
            List<LearnStageProgress> completedProgress
    ) {
        ProgressSnapshot snapshot = buildSnapshot(enrollment, stages, completedProgress);
        LearnTechnology technology = enrollment.getTechnology();
        Set<UUID> completedStageIds = completedProgress.stream()
                .map(progress -> progress.getStage().getId())
                .collect(Collectors.toSet());

        List<LearnRoadmapStage> remainingStages = stages.stream()
                .filter(stage -> !completedStageIds.contains(stage.getId()))
                .sorted(Comparator.comparingInt(LearnRoadmapStage::getStageOrder))
                .toList();

        return new TechnologyProgressResponse(
                enrollment.getId().toString(),
                technology.getId().toString(),
                technology.getSlug(),
                technology.getName(),
                enrollment.getStatus().name(),
                formatInstant(enrollment.getEnrolledAt()),
                formatInstant(enrollment.getStartedAt()),
                formatInstant(enrollment.getLastActivityAt()),
                formatInstant(enrollment.getCompletedAt()),
                snapshot.progressPercent(),
                stages.size(),
                completedProgress.size(),
                snapshot.currentStage() == null ? null : snapshot.currentStage().getId().toString(),
                snapshot.currentStage() == null ? null : snapshot.currentStage().getStageOrder(),
                snapshot.currentStage() == null ? null : snapshot.currentStage().getTitle(),
                snapshot.nextStage() == null ? null : snapshot.nextStage().getId().toString(),
                snapshot.nextStage() == null ? null : snapshot.nextStage().getStageOrder(),
                snapshot.nextStage() == null ? null : snapshot.nextStage().getTitle(),
                summarizeEffort(remainingStages),
                completedProgress.stream()
                        .sorted(Comparator.comparingInt(progress -> progress.getStage().getStageOrder()))
                        .map(progress -> progress.getStage().getId().toString())
                        .toList(),
                completedProgress.stream()
                        .sorted(Comparator.comparingInt(progress -> progress.getStage().getStageOrder()))
                        .map(progress -> progress.getStage().getStageOrder())
                        .toList()
        );
    }

    private ProgressSnapshot buildSnapshot(
            LearnLearningEnrollment enrollment,
            List<LearnRoadmapStage> stages,
            List<LearnStageProgress> completedProgress
    ) {
        List<LearnRoadmapStage> orderedStages = stages.stream()
                .sorted(Comparator.comparingInt(LearnRoadmapStage::getStageOrder))
                .toList();
        Set<UUID> completedStageIds = completedProgress.stream()
                .map(progress -> progress.getStage().getId())
                .collect(Collectors.toSet());

        int progressPercent = orderedStages.isEmpty()
                ? 0
                : (int) Math.round((completedProgress.size() * 100.0) / orderedStages.size());

        if (enrollment.getStatus() == LearningEnrollmentStatus.COMPLETED) {
            return new ProgressSnapshot(progressPercent, null, null);
        }

        LearnRoadmapStage currentStage = orderedStages.stream()
                .filter(stage -> !completedStageIds.contains(stage.getId()))
                .findFirst()
                .orElse(null);

        LearnRoadmapStage nextStage = currentStage == null
                ? null
                : orderedStages.stream()
                        .filter(stage -> stage.getStageOrder() > currentStage.getStageOrder())
                        .findFirst()
                        .orElse(null);

        return new ProgressSnapshot(progressPercent, currentStage, nextStage);
    }

    private String summarizeEffort(List<LearnRoadmapStage> stages) {
        if (stages.isEmpty()) {
            return "";
        }
        List<String> efforts = new ArrayList<>();
        for (LearnRoadmapStage stage : stages) {
            efforts.add(stage.getEstimatedEffort());
        }
        return String.join(" + ", efforts);
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private record ProgressSnapshot(
            int progressPercent,
            LearnRoadmapStage currentStage,
            LearnRoadmapStage nextStage
    ) {
    }
}
