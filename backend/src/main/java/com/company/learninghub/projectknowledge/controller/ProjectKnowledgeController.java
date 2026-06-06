package com.company.learninghub.projectknowledge.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.dto.CreateProjectLinkRequest;
import com.company.learninghub.projectknowledge.dto.CreateProjectRequest;
import com.company.learninghub.projectknowledge.dto.ProjectFolderRequest;
import com.company.learninghub.projectknowledge.dto.ProjectFolderResponse;
import com.company.learninghub.projectknowledge.dto.ProjectKnowledgeItemResponse;
import com.company.learninghub.projectknowledge.dto.ProjectLinkAccessResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberRequest;
import com.company.learninghub.projectknowledge.dto.ProjectMemberResponse;
import com.company.learninghub.projectknowledge.dto.ProjectResponse;
import com.company.learninghub.projectknowledge.dto.UpdateProjectItemRequest;
import com.company.learninghub.projectknowledge.dto.UpdateProjectRequest;
import com.company.learninghub.projectknowledge.service.ProjectKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Project Knowledge", description = "Project knowledge repository, folders, members, documents, links, and access tracking")
@SecurityRequirement(name = "bearerAuth")
public class ProjectKnowledgeController {

    private final ProjectKnowledgeService projectKnowledgeService;

    public ProjectKnowledgeController(ProjectKnowledgeService projectKnowledgeService) {
        this.projectKnowledgeService = projectKnowledgeService;
    }

    @PostMapping
    @Operation(summary = "Create a project. The creator becomes OWNER.")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectResponse response = projectKnowledgeService.createProject(request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{projectId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update a project", description = "Requires OWNER project role.")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.updateProject(projectId, request, authenticatedUser));
    }

    @PostMapping("/{projectId}/archive")
    @Operation(summary = "Archive a project", description = "Requires OWNER project role.")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.archiveProject(projectId, authenticatedUser));
    }

    @GetMapping
    @Operation(summary = "Browse and search accessible projects")
    public ResponseEntity<PageResponse<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProjectAccessType accessType,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @Parameter(description = "Supported sort fields: name, accessType, archived, createdAtUtc, updatedAtUtc.")
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(PageResponse.from(
                projectKnowledgeService.searchProjects(search, accessType, includeArchived, pageable, authenticatedUser)
        ));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "View project details")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.getProject(projectId, authenticatedUser));
    }

    @PostMapping("/{projectId}/members")
    @Operation(summary = "Add or update project member", description = "Requires OWNER project role.")
    public ResponseEntity<ProjectMemberResponse> addOrUpdateMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectMemberRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.addOrUpdateMember(projectId, request, authenticatedUser));
    }

    @GetMapping("/{projectId}/members")
    @Operation(summary = "List project members")
    public ResponseEntity<List<ProjectMemberResponse>> listMembers(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.listMembers(projectId, authenticatedUser));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(summary = "Remove project member", description = "Requires OWNER project role.")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        projectKnowledgeService.removeMember(projectId, userId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/folders")
    @Operation(summary = "Create project folder", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectFolderResponse> createFolder(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectFolderRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectFolderResponse response = projectKnowledgeService.createFolder(projectId, request, authenticatedUser);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{folderId}").buildAndExpand(response.id()).toUri())
                .body(response);
    }

    @PutMapping("/{projectId}/folders/{folderId}")
    @Operation(summary = "Update project folder", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectFolderResponse> updateFolder(
            @PathVariable UUID projectId,
            @PathVariable UUID folderId,
            @Valid @RequestBody ProjectFolderRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.updateFolder(projectId, folderId, request, authenticatedUser));
    }

    @DeleteMapping("/{projectId}/folders/{folderId}")
    @Operation(summary = "Delete empty project folder", description = "Requires OWNER project role.")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable UUID projectId,
            @PathVariable UUID folderId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        projectKnowledgeService.deleteFolder(projectId, folderId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/folders")
    @Operation(summary = "Browse project folder hierarchy")
    public ResponseEntity<PageResponse<ProjectFolderResponse>> listFolders(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID parentId,
            @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(PageResponse.from(projectKnowledgeService.listFolders(projectId, parentId, pageable, authenticatedUser)));
    }

    @PostMapping(value = "/{projectId}/items/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload project knowledge file", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectKnowledgeItemResponse> uploadFile(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID folderId,
            @RequestParam @Size(max = 250) String title,
            @RequestParam(required = false) @Size(max = 5000) String description,
            @RequestParam KnowledgeCategory category,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectKnowledgeItemResponse response = projectKnowledgeService.uploadFile(projectId, folderId, title, description, category, file, authenticatedUser);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/projects/{projectId}/items/{itemId}").buildAndExpand(projectId, response.id()).toUri())
                .body(response);
    }

    @PostMapping("/{projectId}/items/links")
    @Operation(summary = "Create project knowledge link", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectKnowledgeItemResponse> createLink(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateProjectLinkRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectKnowledgeItemResponse response = projectKnowledgeService.createLink(projectId, request, authenticatedUser);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/projects/{projectId}/items/{itemId}").buildAndExpand(projectId, response.id()).toUri())
                .body(response);
    }

    @PutMapping("/{projectId}/items/{itemId}")
    @Operation(summary = "Update project knowledge item", description = "Requires OWNER or CONTRIBUTOR project role.")
    public ResponseEntity<ProjectKnowledgeItemResponse> updateItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateProjectItemRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.updateItem(projectId, itemId, request, authenticatedUser));
    }

    @DeleteMapping("/{projectId}/items/{itemId}")
    @Operation(summary = "Delete project knowledge item", description = "Requires OWNER project role.")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        projectKnowledgeService.deleteItem(projectId, itemId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/items")
    @Operation(summary = "Search project knowledge items")
    public ResponseEntity<PageResponse<ProjectKnowledgeItemResponse>> searchItems(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) KnowledgeCategory category,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAtUtc", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(PageResponse.from(projectKnowledgeService.searchItems(projectId, folderId, category, search, pageable, authenticatedUser)));
    }

    @GetMapping("/{projectId}/items/{itemId}")
    @Operation(summary = "View project knowledge item")
    public ResponseEntity<ProjectKnowledgeItemResponse> getItem(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.getItem(projectId, itemId, authenticatedUser));
    }

    @GetMapping("/{projectId}/items/{itemId}/download")
    @Operation(summary = "Download project knowledge file and track access")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectKnowledgeItemResponse item = projectKnowledgeService.getItem(projectId, itemId, authenticatedUser);
        Resource resource = projectKnowledgeService.downloadFile(projectId, itemId, authenticatedUser);
        String filename = item.originalFilename() == null ? "project-knowledge" : item.originalFilename();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(item.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : item.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename.replace("\"", "") + "\"")
                .body(resource);
    }

    @GetMapping("/{projectId}/items/{itemId}/link")
    @Operation(summary = "Access project knowledge link and track access")
    public ResponseEntity<ProjectLinkAccessResponse> accessLink(
            @PathVariable UUID projectId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(projectKnowledgeService.accessLink(projectId, itemId, authenticatedUser));
    }
}

