package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnCatalogImport;
import com.company.learninghub.learn.domain.CatalogImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnCatalogImportRepository extends JpaRepository<LearnCatalogImport, UUID> {

    boolean existsByCatalogVersionAndPackageTypeAndStatus(
            String catalogVersion,
            String packageType,
            CatalogImportStatus status
    );

    Optional<LearnCatalogImport> findTopByPackageTypeAndStatusOrderByImportedAtDesc(
            String packageType,
            CatalogImportStatus status
    );

    List<LearnCatalogImport> findByCatalogVersionOrderByImportedAtDesc(String catalogVersion);
}
