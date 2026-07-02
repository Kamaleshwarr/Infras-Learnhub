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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learn_technologies")
public class LearnTechnology extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "short_name", nullable = false, length = 30)
    private String shortName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private TechnologyCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private TechnologyDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TechnologyStatus status;

    @Column(name = "featured", nullable = false)
    private boolean featured;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @OneToMany(mappedBy = "technology", fetch = FetchType.LAZY)
    private List<LearnTechnologyProjectLink> projectLinks = new ArrayList<>();

    protected LearnTechnology() {
    }

    public LearnTechnology(
            String name,
            String shortName,
            String description,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            TechnologyStatus status,
            boolean featured,
            User createdBy
    ) {
        updateDetails(name, shortName, description, category, difficulty, featured);
        this.status = status;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDescription() {
        return description;
    }

    public TechnologyCategory getCategory() {
        return category;
    }

    public TechnologyDifficulty getDifficulty() {
        return difficulty;
    }

    public TechnologyStatus getStatus() {
        return status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public List<LearnTechnologyProjectLink> getProjectLinks() {
        return projectLinks;
    }

    public void updateDetails(
            String name,
            String shortName,
            String description,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            boolean featured
    ) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.featured = featured;
    }

    public void setStatus(TechnologyStatus status) {
        this.status = status;
    }

    public boolean isVisibleToEmployees() {
        return TechnologyStatus.PUBLISHED.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnTechnology that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
