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
@Table(name = "project_knowledge_items")
public class ProjectKnowledgeItem extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private ProjectKnowledgeFolder folder;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private KnowledgeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private KnowledgeSourceType sourceType;

    @Column(name = "storage_provider", length = 50)
    private String storageProvider;

    @Column(name = "storage_key", unique = true, length = 500)
    private String storageKey;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "external_url", columnDefinition = "TEXT")
    private String externalUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false, updatable = false)
    private User uploadedBy;

    @Column(name = "access_count", nullable = false)
    private long accessCount;

    protected ProjectKnowledgeItem() {
    }

    private ProjectKnowledgeItem(
            Project project,
            ProjectKnowledgeFolder folder,
            String title,
            String description,
            KnowledgeCategory category,
            KnowledgeSourceType sourceType,
            String storageProvider,
            String storageKey,
            String originalFilename,
            String contentType,
            Long fileSizeBytes,
            String externalUrl,
            User uploadedBy
    ) {
        this.project = project;
        this.folder = folder;
        this.title = title;
        this.description = description;
        this.category = category;
        this.sourceType = sourceType;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.externalUrl = externalUrl;
        this.uploadedBy = uploadedBy;
    }

    public static ProjectKnowledgeItem fileItem(
            Project project,
            ProjectKnowledgeFolder folder,
            String title,
            String description,
            KnowledgeCategory category,
            String storageProvider,
            String storageKey,
            String originalFilename,
            String contentType,
            long fileSizeBytes,
            User uploadedBy
    ) {
        return new ProjectKnowledgeItem(project, folder, title, description, category, KnowledgeSourceType.FILE,
                storageProvider, storageKey, originalFilename, contentType, fileSizeBytes, null, uploadedBy);
    }

    public static ProjectKnowledgeItem linkItem(
            Project project,
            ProjectKnowledgeFolder folder,
            String title,
            String description,
            KnowledgeCategory category,
            String externalUrl,
            User uploadedBy
    ) {
        return new ProjectKnowledgeItem(project, folder, title, description, category, KnowledgeSourceType.LINK,
                null, null, null, null, null, externalUrl, uploadedBy);
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public ProjectKnowledgeFolder getFolder() {
        return folder;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public KnowledgeCategory getCategory() {
        return category;
    }

    public KnowledgeSourceType getSourceType() {
        return sourceType;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public void updateMetadata(String title, String description, ProjectKnowledgeFolder folder, KnowledgeCategory category) {
        this.title = title;
        this.description = description;
        this.folder = folder;
        this.category = category;
    }

    public void updateLink(String title, String description, ProjectKnowledgeFolder folder, KnowledgeCategory category, String externalUrl) {
        updateMetadata(title, description, folder, category);
        this.externalUrl = externalUrl;
    }

    public void incrementAccessCount() {
        this.accessCount++;
    }

    public boolean isFile() {
        return KnowledgeSourceType.FILE.equals(sourceType);
    }

    public boolean isLink() {
        return KnowledgeSourceType.LINK.equals(sourceType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectKnowledgeItem that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

