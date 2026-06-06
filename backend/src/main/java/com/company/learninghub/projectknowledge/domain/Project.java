package com.company.learninghub.projectknowledge.domain;

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

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 30)
    private ProjectAccessType accessType;

    @Column(name = "archived", nullable = false)
    private boolean archived;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    protected Project() {
    }

    public Project(String name, String description, ProjectAccessType accessType, User createdBy) {
        updateDetails(name, description, accessType);
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProjectAccessType getAccessType() {
        return accessType;
    }

    public boolean isArchived() {
        return archived;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void updateDetails(String name, String description, ProjectAccessType accessType) {
        this.name = name;
        this.description = description;
        this.accessType = accessType;
    }

    public void archive() {
        this.archived = true;
    }

    public boolean isPublic() {
        return ProjectAccessType.PUBLIC.equals(accessType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project project)) {
            return false;
        }
        return id != null && Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

