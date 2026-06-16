package com.company.learninghub.initiative.domain;

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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learning_initiatives")
public class LearningInitiative extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reward_description", columnDefinition = "TEXT")
    private String rewardDescription;

    @Column(name = "start_date_utc", nullable = false)
    private Instant startDateUtc;

    @Column(name = "expiry_date_utc", nullable = false)
    private Instant expiryDateUtc;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InitiativeStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    protected LearningInitiative() {
    }

    public LearningInitiative(
            String title,
            String description,
            String rewardDescription,
            Instant startDateUtc,
            Instant expiryDateUtc,
            InitiativeStatus status,
            User createdBy
    ) {
        updateDetails(title, description, rewardDescription, startDateUtc, expiryDateUtc, status);
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public Instant getStartDateUtc() {
        return startDateUtc;
    }

    public Instant getExpiryDateUtc() {
        return expiryDateUtc;
    }

    public InitiativeStatus getStatus() {
        return status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void updateDetails(
            String title,
            String description,
            String rewardDescription,
            Instant startDateUtc,
            Instant expiryDateUtc,
            InitiativeStatus status
    ) {
        this.title = title;
        this.description = description;
        this.rewardDescription = rewardDescription;
        this.startDateUtc = startDateUtc;
        this.expiryDateUtc = expiryDateUtc;
        this.status = status;
    }

    public boolean isVisibleToEmployeesAt(Instant now) {
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDate today = LocalDate.ofInstant(now, utc);
        LocalDate startDate = LocalDate.ofInstant(startDateUtc, utc);
        LocalDate expiryDate = LocalDate.ofInstant(expiryDateUtc, utc);
        return InitiativeStatus.ACTIVE.equals(status)
                && !startDate.isAfter(today)
                && !expiryDate.isBefore(today);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearningInitiative that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

