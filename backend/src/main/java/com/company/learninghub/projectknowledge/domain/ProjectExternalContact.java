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
@Table(name = "project_external_contacts")
public class ProjectExternalContact extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 40)
    private ExternalContactType contactType;

    @Column(name = "role_title", length = 200)
    private String roleTitle;

    @Column(name = "organization", length = 200)
    private String organization;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "contact_url")
    private String contactUrl;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_primary_contact", nullable = false)
    private boolean primaryContact;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    protected ProjectExternalContact() {
    }

    public ProjectExternalContact(
            Project project,
            String name,
            ExternalContactType contactType,
            String roleTitle,
            String organization,
            String email,
            String phone,
            String contactUrl,
            String notes,
            boolean primaryContact,
            int displayOrder,
            User createdBy
    ) {
        this.project = project;
        this.name = name;
        this.contactType = contactType;
        this.roleTitle = roleTitle;
        this.organization = organization;
        this.email = email;
        this.phone = phone;
        this.contactUrl = contactUrl;
        this.notes = notes;
        this.primaryContact = primaryContact;
        this.displayOrder = displayOrder;
        this.active = true;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public ExternalContactType getContactType() {
        return contactType;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public String getOrganization() {
        return organization;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void updateDetails(
            String name,
            ExternalContactType contactType,
            String roleTitle,
            String organization,
            String email,
            String phone,
            String contactUrl,
            String notes,
            boolean primaryContact,
            int displayOrder,
            boolean active
    ) {
        this.name = name;
        this.contactType = contactType;
        this.roleTitle = roleTitle;
        this.organization = organization;
        this.email = email;
        this.phone = phone;
        this.contactUrl = contactUrl;
        this.notes = notes;
        this.primaryContact = primaryContact;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectExternalContact that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
