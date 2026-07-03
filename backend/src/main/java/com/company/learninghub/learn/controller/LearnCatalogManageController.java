package com.company.learninghub.learn.controller;

import com.company.learninghub.learn.dto.CatalogImportStatusResponse;
import com.company.learninghub.learn.service.LearnCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/learn/manage/catalog")
@Tag(name = "Learn Catalog Management", description = "Admin catalog import status")
@SecurityRequirement(name = "bearerAuth")
public class LearnCatalogManageController {

    private final LearnCatalogService catalogService;

    public LearnCatalogManageController(LearnCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get catalog import status", description = "Admin only.")
    public ResponseEntity<Map<String, Object>> status() {
        CatalogImportStatusResponse technologyImport = catalogService.getCatalogStatus();
        CatalogImportStatusResponse roadmapImport = catalogService.getRoadmapImportStatus();
        return ResponseEntity.ok(Map.of(
                "catalogVersion", technologyImport.catalogVersion() == null ? "" : technologyImport.catalogVersion(),
                "importedAt", technologyImport.importedAt() == null ? "" : technologyImport.importedAt(),
                "packageType", technologyImport.packageType(),
                "recordsUpserted", technologyImport.recordsUpserted(),
                "status", technologyImport.status(),
                "technologyCount", catalogService.countCatalogTechnologies(),
                "roadmapImportStatus", roadmapImport.status(),
                "roadmapRecordsUpserted", roadmapImport.recordsUpserted(),
                "roadmapCount", catalogService.countCatalogRoadmaps()
        ));
    }
}
