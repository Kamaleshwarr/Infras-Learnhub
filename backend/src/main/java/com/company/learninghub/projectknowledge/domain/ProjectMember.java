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
@Table(name = "project_members")
public class ProjectMember extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false, length = 30)
    private ProjectRole projectRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "functional_role", nullable = false, length = 40)
    private ProjectFunctionalRole functionalRole;

    @Column(name = "responsibility")
    private String responsibility;

    @Column(name = "is_primary_contact", nullable = false)
    private boolean primaryContact;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProjectMember() {
    }

    public ProjectMember(Project project, User user, ProjectRole projectRole) {
        this.project = project;
        this.user = user;
        this.projectRole = projectRole;
        this.functionalRole = ProjectFunctionalRole.OTHER;
        this.primaryContact = false;
        this.displayOrder = 0;
    }

    public ProjectMember(
            Project project,
            User user,
            ProjectRole projectRole,
            ProjectFunctionalRole functionalRole,
            String responsibility,
            boolean primaryContact,
            int displayOrder
    ) {
        this.project = project;
        this.user = user;
        this.projectRole = projectRole;
        this.functionalRole = functionalRole;
        this.responsibility = responsibility;
        this.primaryContact = primaryContact;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public User getUser() {
        return user;
    }

    public ProjectRole getProjectRole() {
        return projectRole;
    }

    public void updateRole(ProjectRole projectRole) {
        this.projectRole = projectRole;
    }

    public ProjectFunctionalRole getFunctionalRole() {
        return functionalRole;
    }

    public String getResponsibility() {
        return responsibility;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void updateAssignment(
            ProjectRole projectRole,
            ProjectFunctionalRole functionalRole,
            String responsibility,
            boolean primaryContact,
            int displayOrder
    ) {
        this.projectRole = projectRole;
        this.functionalRole = functionalRole;
        this.responsibility = responsibility;
        this.primaryContact = primaryContact;
        this.displayOrder = displayOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectMember that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

