package com.company.learninghub.studymaterial.domain;

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
@Table(name = "study_materials")
public class StudyMaterial extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private StudyMaterialFolder folder;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 30)
    private MaterialType materialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private MaterialSourceType sourceType;

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

    @Column(name = "download_count", nullable = false)
    private long downloadCount;

    protected StudyMaterial() {
    }

    private StudyMaterial(
            StudyMaterialFolder folder,
            String title,
            String description,
            MaterialType materialType,
            MaterialSourceType sourceType,
            String storageProvider,
            String storageKey,
            String originalFilename,
            String contentType,
            Long fileSizeBytes,
            String externalUrl,
            User uploadedBy
    ) {
        this.folder = folder;
        this.title = title;
        this.description = description;
        this.materialType = materialType;
        this.sourceType = sourceType;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.externalUrl = externalUrl;
        this.uploadedBy = uploadedBy;
    }

    public static StudyMaterial fileMaterial(
            StudyMaterialFolder folder,
            String title,
            String description,
            MaterialType materialType,
            String storageProvider,
            String storageKey,
            String originalFilename,
            String contentType,
            long fileSizeBytes,
            User uploadedBy
    ) {
        return new StudyMaterial(
                folder,
                title,
                description,
                materialType,
                MaterialSourceType.FILE,
                storageProvider,
                storageKey,
                originalFilename,
                contentType,
                fileSizeBytes,
                null,
                uploadedBy
        );
    }

    public static StudyMaterial linkMaterial(
            StudyMaterialFolder folder,
            String title,
            String description,
            MaterialType materialType,
            String externalUrl,
            User uploadedBy
    ) {
        return new StudyMaterial(
                folder,
                title,
                description,
                materialType,
                MaterialSourceType.LINK,
                null,
                null,
                null,
                null,
                null,
                externalUrl,
                uploadedBy
        );
    }

    public UUID getId() {
        return id;
    }

    public StudyMaterialFolder getFolder() {
        return folder;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public MaterialSourceType getSourceType() {
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

    public long getDownloadCount() {
        return downloadCount;
    }

    public void updateMetadata(String title, String description, StudyMaterialFolder folder) {
        this.title = title;
        this.description = description;
        this.folder = folder;
    }

    public void updateLink(String title, String description, StudyMaterialFolder folder, String externalUrl) {
        updateMetadata(title, description, folder);
        this.externalUrl = externalUrl;
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public boolean isFile() {
        return MaterialSourceType.FILE.equals(sourceType);
    }

    public boolean isLink() {
        return MaterialSourceType.LINK.equals(sourceType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudyMaterial that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

