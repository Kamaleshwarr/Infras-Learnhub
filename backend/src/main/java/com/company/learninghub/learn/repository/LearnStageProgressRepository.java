package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnStageProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LearnStageProgressRepository extends JpaRepository<LearnStageProgress, UUID> {

    @EntityGraph(attributePaths = "stage")
    List<LearnStageProgress> findByEnrollmentIdOrderByStageStageOrderAsc(UUID enrollmentId);

    int countByEnrollmentId(UUID enrollmentId);

    boolean existsByEnrollmentIdAndStageId(UUID enrollmentId, UUID stageId);
}
