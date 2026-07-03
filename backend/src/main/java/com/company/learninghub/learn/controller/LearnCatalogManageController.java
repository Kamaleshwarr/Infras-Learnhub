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
        CatalogImportStatusResponse importStatus = catalogService.getCatalogStatus();
        return ResponseEntity.ok(Map.of(
                "catalogVersion", importStatus.catalogVersion() == null ? "" : importStatus.catalogVersion(),
                "importedAt", importStatus.importedAt() == null ? "" : importStatus.importedAt(),
                "packageType", importStatus.packageType(),
                "recordsUpserted", importStatus.recordsUpserted(),
                "status", importStatus.status(),
                "technologyCount", catalogService.countCatalogTechnologies()
        ));
    }
}
