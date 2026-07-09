package com.company.learninghub.projectknowledge.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.projectknowledge.domain.RepositoryProvider;
import com.company.learninghub.projectknowledge.domain.RepositoryType;
import com.company.learninghub.projectknowledge.dto.ProjectLinkedRepositoryRequest;
import com.company.learninghub.projectknowledge.dto.ProjectLinkedRepositoryResponse;
import com.company.learninghub.projectknowledge.service.ProjectLinkedRepositoryService;
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
@RequestMapping("/api/v1/projects/{projectId}/repositories")
@Tag(name = "Project Repositories", description = "Linked source repositories for a project")
@SecurityRequirement(name = "bearerAuth")
public class ProjectLinkedRepositoryController {

    private final ProjectLinkedRepositoryService repositoryService;

    public ProjectLinkedRepositoryController(ProjectLinkedRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    @Operation(summary = "List project repositories")
    public ResponseEntity<List<ProjectLinkedRepositoryResponse>> listRepositories(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RepositoryType repositoryType,
            @RequestParam(required = false) RepositoryProvider provider,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(repositoryService.listRepositories(
                projectId,
                search,
                repositoryType,
                provider,
                includeInactive,
                authenticatedUser
        ));
    }

    @GetMapping("/{repositoryId}")
    @Operation(summary = "View project repository")
    public ResponseEntity<ProjectLinkedRepositoryResponse> getRepository(
            @PathVariable UUID projectId,
            @PathVariable UUID repositoryId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(repositoryService.getRepository(projectId, repositoryId, authenticatedUser));
    }

    @PostMapping
    @Operation(summary = "Create project repository link", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectLinkedRepositoryResponse> createRepository(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectLinkedRepositoryRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectLinkedRepositoryResponse response = repositoryService.createRepository(projectId, request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{repositoryId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{repositoryId}")
    @Operation(summary = "Update project repository link", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectLinkedRepositoryResponse> updateRepository(
            @PathVariable UUID projectId,
            @PathVariable UUID repositoryId,
            @Valid @RequestBody ProjectLinkedRepositoryRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(repositoryService.updateRepository(projectId, repositoryId, request, authenticatedUser));
    }

    @DeleteMapping("/{repositoryId}")
    @Operation(summary = "Delete project repository link", description = "Requires OWNER project role.")
    public ResponseEntity<Void> deleteRepository(
            @PathVariable UUID projectId,
            @PathVariable UUID repositoryId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        repositoryService.deleteRepository(projectId, repositoryId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
