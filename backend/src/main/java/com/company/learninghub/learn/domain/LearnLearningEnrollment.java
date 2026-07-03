package com.company.learninghub.learn.domain;

import com.company.learninghub.common.domain.AuditableEntity;
import com.company.learninghub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "learn_learning_enrollments")
public class LearnLearningEnrollment extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technology_slug", referencedColumnName = "slug", nullable = false, updatable = false)
    private LearnTechnology technology;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LearningEnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id")
    private LearnRoadmapStage currentStage;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected LearnLearningEnrollment() {
    }

    public LearnLearningEnrollment(User user, LearnTechnology technology, Instant enrolledAt) {
        this.user = user;
        this.technology = technology;
        this.status = LearningEnrollmentStatus.NOT_STARTED;
        this.enrolledAt = enrolledAt;
        this.lastActivityAt = enrolledAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LearnTechnology getTechnology() {
        return technology;
    }

    public String getTechnologySlug() {
        return technology == null ? null : technology.getSlug();
    }

    public LearningEnrollmentStatus getStatus() {
        return status;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public LearnRoadmapStage getCurrentStage() {
        return currentStage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void startLearning(LearnRoadmapStage firstStage, Instant activityAt) {
        this.status = LearningEnrollmentStatus.IN_PROGRESS;
        this.startedAt = activityAt;
        this.lastActivityAt = activityAt;
        this.currentStage = firstStage;
    }

    public void markActivity(Instant activityAt) {
        this.lastActivityAt = activityAt;
    }

    public void advanceToStage(LearnRoadmapStage nextStage, Instant activityAt) {
        this.currentStage = nextStage;
        this.lastActivityAt = activityAt;
    }

    public void complete(Instant activityAt) {
        this.status = LearningEnrollmentStatus.COMPLETED;
        this.completedAt = activityAt;
        this.lastActivityAt = activityAt;
        this.currentStage = null;
    }

    public void leave(Instant activityAt) {
        this.status = LearningEnrollmentStatus.LEFT;
        this.lastActivityAt = activityAt;
        this.currentStage = null;
    }

    public boolean isActive() {
        return status == LearningEnrollmentStatus.NOT_STARTED
                || status == LearningEnrollmentStatus.IN_PROGRESS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnLearningEnrollment that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
