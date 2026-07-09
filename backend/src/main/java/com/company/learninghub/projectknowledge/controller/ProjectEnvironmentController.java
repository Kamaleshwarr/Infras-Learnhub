package com.company.learninghub.projectknowledge.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentReferenceRequest;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentReferenceResponse;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentRequest;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentResponse;
import com.company.learninghub.projectknowledge.service.ProjectEnvironmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/projects/{projectId}/environments")
@Tag(name = "Project Environments", description = "Project environment directory and navigation references")
@SecurityRequirement(name = "bearerAuth")
public class ProjectEnvironmentController {

    private final ProjectEnvironmentService environmentService;

    public ProjectEnvironmentController(ProjectEnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    @Operation(summary = "List project environments")
    public ResponseEntity<List<ProjectEnvironmentResponse>> listEnvironments(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(environmentService.listEnvironments(projectId, search, includeInactive, authenticatedUser));
    }

    @GetMapping("/{environmentId}")
    @Operation(summary = "View project environment")
    public ResponseEntity<ProjectEnvironmentResponse> getEnvironment(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(environmentService.getEnvironment(projectId, environmentId, authenticatedUser));
    }

    @PostMapping
    @Operation(summary = "Create project environment", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectEnvironmentResponse> createEnvironment(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectEnvironmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectEnvironmentResponse response = environmentService.createEnvironment(projectId, request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{environmentId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{environmentId}")
    @Operation(summary = "Update project environment", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectEnvironmentResponse> updateEnvironment(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @Valid @RequestBody ProjectEnvironmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(environmentService.updateEnvironment(projectId, environmentId, request, authenticatedUser));
    }

    @DeleteMapping("/{environmentId}")
    @Operation(summary = "Delete empty project environment", description = "Requires OWNER project role.")
    public ResponseEntity<Void> deleteEnvironment(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        environmentService.deleteEnvironment(projectId, environmentId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{environmentId}/references")
    @Operation(summary = "Add environment reference", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectEnvironmentReferenceResponse> createReference(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @Valid @RequestBody ProjectEnvironmentReferenceRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectEnvironmentReferenceResponse response = environmentService.createReference(
                projectId,
                environmentId,
                request,
                authenticatedUser
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{referenceId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{environmentId}/references/{referenceId}")
    @Operation(summary = "Update environment reference", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectEnvironmentReferenceResponse> updateReference(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @PathVariable UUID referenceId,
            @Valid @RequestBody ProjectEnvironmentReferenceRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(environmentService.updateReference(projectId, environmentId, referenceId, request, authenticatedUser));
    }

    @DeleteMapping("/{environmentId}/references/{referenceId}")
    @Operation(summary = "Delete environment reference", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<Void> deleteReference(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @PathVariable UUID referenceId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        environmentService.deleteReference(projectId, environmentId, referenceId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
