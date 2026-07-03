package com.company.learninghub.learn.domain;

import com.company.learninghub.common.domain.AuditableEntity;
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
@Table(name = "learn_roadmap_stage_resources")
public class LearnRoadmapStageResource extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private LearnRoadmapStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_kind", nullable = false, length = 20)
    private RoadmapResourceKind resourceKind;

    @Column(name = "resource_order", nullable = false)
    private int resourceOrder;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private RoadmapResourceType resourceType;

    @Column(name = "provider", length = 100)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "free_paid", length = 20)
    private RoadmapResourceCost freePaid;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected LearnRoadmapStageResource() {
    }

    public static LearnRoadmapStageResource create() {
        return new LearnRoadmapStageResource();
    }

    public UUID getId() {
        return id;
    }

    public LearnRoadmapStage getStage() {
        return stage;
    }

    public void setStage(LearnRoadmapStage stage) {
        this.stage = stage;
    }

    public RoadmapResourceKind getResourceKind() {
        return resourceKind;
    }

    public void setResourceKind(RoadmapResourceKind resourceKind) {
        this.resourceKind = resourceKind;
    }

    public int getResourceOrder() {
        return resourceOrder;
    }

    public void setResourceOrder(int resourceOrder) {
        this.resourceOrder = resourceOrder;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RoadmapResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(RoadmapResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public RoadmapResourceCost getFreePaid() {
        return freePaid;
    }

    public void setFreePaid(RoadmapResourceCost freePaid) {
        this.freePaid = freePaid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnRoadmapStageResource that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
