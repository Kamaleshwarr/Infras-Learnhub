package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnLearningEnrollment;
import com.company.learninghub.learn.domain.LearningEnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnLearningEnrollmentRepository extends JpaRepository<LearnLearningEnrollment, UUID> {

    @EntityGraph(attributePaths = {"technology", "currentStage"})
    Optional<LearnLearningEnrollment> findByUserIdAndTechnology_SlugAndStatusIn(
            UUID userId,
            String technologySlug,
            Collection<LearningEnrollmentStatus> statuses
    );

    @EntityGraph(attributePaths = {"technology", "currentStage"})
    Optional<LearnLearningEnrollment> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"technology", "currentStage"})
    @Query("""
            SELECT e FROM LearnLearningEnrollment e
            WHERE e.user.id = :userId
              AND e.status IN :statuses
            ORDER BY e.lastActivityAt DESC NULLS LAST, e.enrolledAt DESC
            """)
    List<LearnLearningEnrollment> findByUserIdAndStatusInOrderByLastActivityDesc(
            @Param("userId") UUID userId,
            @Param("statuses") Collection<LearningEnrollmentStatus> statuses
    );

    boolean existsByUserIdAndTechnology_SlugAndStatusIn(
            UUID userId,
            String technologySlug,
            Collection<LearningEnrollmentStatus> statuses
    );
}
