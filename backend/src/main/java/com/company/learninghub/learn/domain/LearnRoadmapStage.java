package com.company.learninghub.learn.domain;

import com.company.learninghub.common.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "learn_roadmap_stages")
public class LearnRoadmapStage extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private LearnRoadmap roadmap;

    @Column(name = "stage_order", nullable = false)
    private int stageOrder;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_effort", nullable = false, length = 50)
    private String estimatedEffort;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LearnRoadmapStageResource> resources = new ArrayList<>();

    protected LearnRoadmapStage() {
    }

    public static LearnRoadmapStage create() {
        return new LearnRoadmapStage();
    }

    public UUID getId() {
        return id;
    }

    public LearnRoadmap getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(LearnRoadmap roadmap) {
        this.roadmap = roadmap;
    }

    public int getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(int stageOrder) {
        this.stageOrder = stageOrder;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEstimatedEffort() {
        return estimatedEffort;
    }

    public void setEstimatedEffort(String estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<LearnRoadmapStageResource> getResources() {
        return resources;
    }

    public void replaceResources(List<LearnRoadmapStageResource> newResources) {
        resources.clear();
        for (LearnRoadmapStageResource resource : newResources) {
            resource.setStage(this);
            resources.add(resource);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnRoadmapStage that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
