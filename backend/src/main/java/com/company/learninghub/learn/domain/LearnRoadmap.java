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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learn_roadmaps")
public class LearnRoadmap extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technology_slug", referencedColumnName = "slug", nullable = false, unique = true)
    private LearnTechnology technology;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "catalog_updated_at")
    private Instant catalogUpdatedAt;

    @Column(name = "catalog_present", nullable = false)
    private boolean catalogPresent = true;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LearnRoadmapStage> stages = new ArrayList<>();

    protected LearnRoadmap() {
    }

    public LearnRoadmap(LearnTechnology technology) {
        this.technology = technology;
    }

    public UUID getId() {
        return id;
    }

    public LearnTechnology getTechnology() {
        return technology;
    }

    public String getTechnologySlug() {
        return technology == null ? null : technology.getSlug();
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public Instant getCatalogUpdatedAt() {
        return catalogUpdatedAt;
    }

    public boolean isCatalogPresent() {
        return catalogPresent;
    }

    public List<LearnRoadmapStage> getStages() {
        return stages;
    }

    public void applyCatalogData(
            String version,
            String description,
            String source,
            String sourceUrl,
            Instant catalogUpdatedAt
    ) {
        this.version = version;
        this.description = description;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.catalogUpdatedAt = catalogUpdatedAt;
        this.catalogPresent = true;
    }

    public void replaceStages(List<LearnRoadmapStage> newStages) {
        stages.clear();
        for (LearnRoadmapStage stage : newStages) {
            stage.setRoadmap(this);
            stages.add(stage);
        }
    }

    public void markCatalogAbsent() {
        this.catalogPresent = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnRoadmap that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
