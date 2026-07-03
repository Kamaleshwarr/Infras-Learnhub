package com.company.learninghub.learn.controller;

import com.company.learninghub.learn.dto.CreateResourceOverrideRequest;
import com.company.learninghub.learn.dto.ResourceOverrideResponse;
import com.company.learninghub.learn.dto.StageResourceAdminResponse;
import com.company.learninghub.learn.dto.UpdateResourceOverrideRequest;
import com.company.learninghub.learn.service.LearnResourceOverrideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learn/manage/technologies/{technologyId}/roadmap")
@Tag(name = "Learn Resource Overrides", description = "Admin management for organization learning resource overrides")
@SecurityRequirement(name = "bearerAuth")
public class LearnResourceOverrideManageController {

    private final LearnResourceOverrideService overrideService;

    public LearnResourceOverrideManageController(LearnResourceOverrideService overrideService) {
        this.overrideService = overrideService;
    }

    @GetMapping("/stages/{stageSlug}/resources")
    @Operation(summary = "List stage resources for admin management", description = "Returns catalog, effective, and override status for each resource.")
    public ResponseEntity<StageResourceAdminResponse> getStageResources(
            @PathVariable UUID technologyId,
            @PathVariable String stageSlug
    ) {
        return ResponseEntity.ok(overrideService.getStageResources(technologyId, stageSlug));
    }

    @PostMapping("/resources/overrides")
    @Operation(summary = "Create a resource override", description = "Replace URL, disable a catalog resource, or add an organization-only resource.")
    public ResponseEntity<ResourceOverrideResponse> createOverride(
            @PathVariable UUID technologyId,
            @Valid @RequestBody CreateResourceOverrideRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(overrideService.createOverride(technologyId, request));
    }

    @PutMapping("/resources/overrides/{overrideId}")
    @Operation(summary = "Update a resource override")
    public ResponseEntity<ResourceOverrideResponse> updateOverride(
            @PathVariable UUID technologyId,
            @PathVariable UUID overrideId,
            @Valid @RequestBody UpdateResourceOverrideRequest request
    ) {
        return ResponseEntity.ok(overrideService.updateOverride(technologyId, overrideId, request));
    }

    @DeleteMapping("/resources/overrides/{overrideId}")
    @Operation(summary = "Delete a resource override", description = "Restores the catalog default for catalog resources.")
    public ResponseEntity<Void> deleteOverride(
            @PathVariable UUID technologyId,
            @PathVariable UUID overrideId
    ) {
        overrideService.deleteOverride(technologyId, overrideId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stages/{stageSlug}/resources/{resourceSlug}/restore")
    @Operation(summary = "Restore catalog default", description = "Deletes the override for a catalog resource slug.")
    public ResponseEntity<Void> restoreDefault(
            @PathVariable UUID technologyId,
            @PathVariable String stageSlug,
            @PathVariable String resourceSlug
    ) {
        overrideService.restoreDefault(technologyId, stageSlug, resourceSlug);
        return ResponseEntity.noContent().build();
    }
}
