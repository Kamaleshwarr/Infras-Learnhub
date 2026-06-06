package com.company.learninghub.studymaterial.domain;

import com.company.learninghub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_material_download_events")
public class StudyMaterialDownloadEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private StudyMaterial material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "downloaded_by", nullable = false)
    private User downloadedBy;

    @Column(name = "downloaded_at_utc", nullable = false, updatable = false)
    private Instant downloadedAtUtc;

    protected StudyMaterialDownloadEvent() {
    }

    public StudyMaterialDownloadEvent(StudyMaterial material, User downloadedBy) {
        this.material = material;
        this.downloadedBy = downloadedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.downloadedAtUtc = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public StudyMaterial getMaterial() {
        return material;
    }

    public User getDownloadedBy() {
        return downloadedBy;
    }

    public Instant getDownloadedAtUtc() {
        return downloadedAtUtc;
    }
}

