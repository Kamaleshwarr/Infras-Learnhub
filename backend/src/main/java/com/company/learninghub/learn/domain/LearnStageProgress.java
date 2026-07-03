package com.company.learninghub.learn.domain;

import com.company.learninghub.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learn_stage_progress")
public class LearnStageProgress extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private LearnLearningEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false, updatable = false)
    private LearnRoadmapStage stage;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected LearnStageProgress() {
    }

    public LearnStageProgress(LearnLearningEnrollment enrollment, LearnRoadmapStage stage, Instant completedAt) {
        this.enrollment = enrollment;
        this.stage = stage;
        this.completedAt = completedAt;
    }

    public UUID getId() {
        return id;
    }

    public LearnLearningEnrollment getEnrollment() {
        return enrollment;
    }

    public LearnRoadmapStage getStage() {
        return stage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnStageProgress that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
