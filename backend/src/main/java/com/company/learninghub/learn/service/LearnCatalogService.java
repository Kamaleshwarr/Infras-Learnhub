package com.company.learninghub.learn.service;

import com.company.learninghub.learn.domain.CatalogImportStatus;
import com.company.learninghub.learn.dto.CatalogImportStatusResponse;
import com.company.learninghub.learn.repository.LearnCatalogImportRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearnCatalogService {

    private static final String TECHNOLOGIES_PACKAGE_TYPE = "technologies";

    private final LearnCatalogImportRepository catalogImportRepository;
    private final LearnTechnologyRepository technologyRepository;

    public LearnCatalogService(
            LearnCatalogImportRepository catalogImportRepository,
            LearnTechnologyRepository technologyRepository
    ) {
        this.catalogImportRepository = catalogImportRepository;
        this.technologyRepository = technologyRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public CatalogImportStatusResponse getCatalogStatus() {
        return catalogImportRepository
                .findTopByPackageTypeAndStatusOrderByImportedAtDesc(
                        TECHNOLOGIES_PACKAGE_TYPE,
                        CatalogImportStatus.SUCCESS
                )
                .map(importRecord -> new CatalogImportStatusResponse(
                        importRecord.getCatalogVersion(),
                        importRecord.getImportedAt(),
                        importRecord.getPackageType(),
                        importRecord.getRecordsUpserted(),
                        importRecord.getStatus().name()
                ))
                .orElse(new CatalogImportStatusResponse(null, null, TECHNOLOGIES_PACKAGE_TYPE, 0, "NOT_IMPORTED"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public long countCatalogTechnologies() {
        return technologyRepository.findByCatalogPresentTrue().size();
    }
}
