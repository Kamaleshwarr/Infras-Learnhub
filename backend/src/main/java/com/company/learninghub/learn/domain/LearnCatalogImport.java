package com.company.learninghub.learn.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learn_catalog_imports")
public class LearnCatalogImport {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "catalog_version", nullable = false, length = 20)
    private String catalogVersion;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    @Column(name = "package_type", nullable = false, length = 50)
    private String packageType;

    @Column(name = "records_upserted", nullable = false)
    private int recordsUpserted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CatalogImportStatus status;

    protected LearnCatalogImport() {
    }

    public LearnCatalogImport(
            String catalogVersion,
            Instant importedAt,
            String packageType,
            int recordsUpserted,
            CatalogImportStatus status
    ) {
        this.catalogVersion = catalogVersion;
        this.importedAt = importedAt;
        this.packageType = packageType;
        this.recordsUpserted = recordsUpserted;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public String getPackageType() {
        return packageType;
    }

    public int getRecordsUpserted() {
        return recordsUpserted;
    }

    public CatalogImportStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnCatalogImport that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
