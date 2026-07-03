package com.company.learninghub.learn.domain;

import com.company.learninghub.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learn_stage_resource_overrides")
public class LearnStageResourceOverride extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "technology_slug", nullable = false, length = 100)
    private String technologySlug;

    @Column(name = "stage_slug", nullable = false, length = 100)
    private String stageSlug;

    @Column(name = "resource_slug", nullable = false, length = 100)
    private String resourceSlug;

    @Column(name = "catalog_resource_slug", length = 100)
    private String catalogResourceSlug;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_kind", nullable = false, length = 20)
    private RoadmapResourceKind resourceKind;

    @Column(name = "disabled", nullable = false)
    private boolean disabled;

    @Column(name = "override_url", length = 2048)
    private String overrideUrl;

    @Column(name = "preferred", nullable = false)
    private boolean preferred;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "reason")
    private String reason;

    @Column(name = "title", length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 50)
    private RoadmapResourceType resourceType;

    @Column(name = "provider", length = 100)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "free_paid", length = 20)
    private RoadmapResourceCost freePaid;

    @Column(name = "resource_order", nullable = false)
    private int resourceOrder;

    protected LearnStageResourceOverride() {
    }

    public static LearnStageResourceOverride create() {
        return new LearnStageResourceOverride();
    }

    public boolean isOrganizationResource() {
        return catalogResourceSlug == null;
    }

    public UUID getId() {
        return id;
    }

    public String getTechnologySlug() {
        return technologySlug;
    }

    public void setTechnologySlug(String technologySlug) {
        this.technologySlug = technologySlug;
    }

    public String getStageSlug() {
        return stageSlug;
    }

    public void setStageSlug(String stageSlug) {
        this.stageSlug = stageSlug;
    }

    public String getResourceSlug() {
        return resourceSlug;
    }

    public void setResourceSlug(String resourceSlug) {
        this.resourceSlug = resourceSlug;
    }

    public String getCatalogResourceSlug() {
        return catalogResourceSlug;
    }

    public void setCatalogResourceSlug(String catalogResourceSlug) {
        this.catalogResourceSlug = catalogResourceSlug;
    }

    public RoadmapResourceKind getResourceKind() {
        return resourceKind;
    }

    public void setResourceKind(RoadmapResourceKind resourceKind) {
        this.resourceKind = resourceKind;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getOverrideUrl() {
        return overrideUrl;
    }

    public void setOverrideUrl(String overrideUrl) {
        this.overrideUrl = overrideUrl;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getResourceOrder() {
        return resourceOrder;
    }

    public void setResourceOrder(int resourceOrder) {
        this.resourceOrder = resourceOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnStageResourceOverride that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
