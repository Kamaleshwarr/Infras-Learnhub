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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
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

    @Column(name = "slug", nullable = false, length = 100, updatable = false)
    private String slug;

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

    @Column(name = "catalog_featured", nullable = false)
    private boolean catalogFeatured;

    @Column(name = "featured_override")
    private Boolean featuredOverride;

    @Column(name = "estimated_duration", length = 50)
    private String estimatedDuration;

    @Column(name = "official_website", length = 2048)
    private String officialWebsite;

    @Column(name = "official_documentation", length = 2048)
    private String officialDocumentation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false, columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @Column(name = "org_notes", columnDefinition = "TEXT")
    private String orgNotes;

    @Column(name = "catalog_version", length = 20)
    private String catalogVersion;

    @Column(name = "catalog_source", length = 100)
    private String catalogSource;

    @Column(name = "catalog_source_url", length = 2048)
    private String catalogSourceUrl;

    @Column(name = "catalog_updated_at")
    private Instant catalogUpdatedAt;

    @Column(name = "catalog_present", nullable = false)
    private boolean catalogPresent = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @OneToMany(mappedBy = "technology", fetch = FetchType.LAZY)
    private List<LearnTechnologyProjectLink> projectLinks = new ArrayList<>();

    protected LearnTechnology() {
    }

    public LearnTechnology(
            String slug,
            String name,
            String shortName,
            String description,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            TechnologyStatus status,
            boolean catalogFeatured,
            User createdBy
    ) {
        this.slug = slug;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.status = status;
        this.catalogFeatured = catalogFeatured;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
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

    public boolean isCatalogFeatured() {
        return catalogFeatured;
    }

    public Boolean getFeaturedOverride() {
        return featuredOverride;
    }

    public boolean isFeatured() {
        return featuredOverride != null ? featuredOverride : catalogFeatured;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public String getOfficialWebsite() {
        return officialWebsite;
    }

    public String getOfficialDocumentation() {
        return officialDocumentation;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getOrgNotes() {
        return orgNotes;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public String getCatalogSource() {
        return catalogSource;
    }

    public String getCatalogSourceUrl() {
        return catalogSourceUrl;
    }

    public Instant getCatalogUpdatedAt() {
        return catalogUpdatedAt;
    }

    public boolean isCatalogPresent() {
        return catalogPresent;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public List<LearnTechnologyProjectLink> getProjectLinks() {
        return projectLinks;
    }

    public void applyCatalogData(
            String name,
            String shortName,
            String description,
            TechnologyCategory category,
            TechnologyDifficulty difficulty,
            boolean catalogFeatured,
            String estimatedDuration,
            String officialWebsite,
            String officialDocumentation,
            List<String> tags,
            String catalogVersion,
            String catalogSource,
            String catalogSourceUrl,
            Instant catalogUpdatedAt
    ) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        if (featuredOverride == null) {
            this.catalogFeatured = catalogFeatured;
        }
        this.estimatedDuration = estimatedDuration;
        this.officialWebsite = officialWebsite;
        this.officialDocumentation = officialDocumentation;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.catalogVersion = catalogVersion;
        this.catalogSource = catalogSource;
        this.catalogSourceUrl = catalogSourceUrl;
        this.catalogUpdatedAt = catalogUpdatedAt;
        this.catalogPresent = true;
    }

    public void setFeaturedOverride(Boolean featuredOverride) {
        this.featuredOverride = featuredOverride;
    }

    public void setOrgNotes(String orgNotes) {
        this.orgNotes = orgNotes;
    }

    public void setStatus(TechnologyStatus status) {
        this.status = status;
    }

    public void markCatalogAbsent() {
        this.catalogPresent = false;
    }

    public boolean isVisibleToEmployees() {
        return TechnologyStatus.PUBLISHED.equals(status) && catalogPresent;
    }

    public boolean hasOrganizationCuration() {
        return featuredOverride != null
                || orgNotes != null
                || !TechnologyStatus.HIDDEN.equals(status)
                || (projectLinks != null && !projectLinks.isEmpty());
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
