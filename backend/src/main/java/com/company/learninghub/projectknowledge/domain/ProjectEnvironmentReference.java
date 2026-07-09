package com.company.learninghub.projectknowledge.domain;

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

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "project_environment_references")
public class ProjectEnvironmentReference extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false, updatable = false)
    private ProjectEnvironment environment;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 40)
    private EnvironmentReferenceType referenceType;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected ProjectEnvironmentReference() {
    }

    public ProjectEnvironmentReference(
            ProjectEnvironment environment,
            String name,
            EnvironmentReferenceType referenceType,
            String url,
            String description,
            int displayOrder
    ) {
        this.environment = environment;
        updateDetails(name, referenceType, url, description, displayOrder, true);
    }

    public UUID getId() {
        return id;
    }

    public ProjectEnvironment getEnvironment() {
        return environment;
    }

    public String getName() {
        return name;
    }

    public EnvironmentReferenceType getReferenceType() {
        return referenceType;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void updateDetails(
            String name,
            EnvironmentReferenceType referenceType,
            String url,
            String description,
            int displayOrder,
            boolean active
    ) {
        this.name = name;
        this.referenceType = referenceType;
        this.url = url;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectEnvironmentReference that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
