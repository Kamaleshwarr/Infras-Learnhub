package com.company.learninghub.studymaterial.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.studymaterial.domain.MaterialType;
import com.company.learninghub.studymaterial.dto.CreateFolderRequest;
import com.company.learninghub.studymaterial.dto.CreateLinkMaterialRequest;
import com.company.learninghub.studymaterial.dto.LinkDownloadResponse;
import com.company.learninghub.studymaterial.dto.StudyMaterialFolderResponse;
import com.company.learninghub.studymaterial.dto.StudyMaterialResponse;
import com.company.learninghub.studymaterial.dto.UpdateFolderRequest;
import com.company.learninghub.studymaterial.dto.UpdateMaterialRequest;
import com.company.learninghub.studymaterial.service.StudyMaterialService;
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
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/study-materials")
@Tag(name = "Study Materials", description = "Folder hierarchy, study material files, links, search, and downloads")
@SecurityRequirement(name = "bearerAuth")
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;

    public StudyMaterialController(StudyMaterialService studyMaterialService) {
        this.studyMaterialService = studyMaterialService;
    }

    @PostMapping("/folders")
    @Operation(summary = "Create a study material folder", description = "Admin only.")
    public ResponseEntity<StudyMaterialFolderResponse> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        StudyMaterialFolderResponse response = studyMaterialService.createFolder(request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{folderId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/folders/{folderId}")
    @Operation(summary = "Update a study material folder", description = "Admin only.")
    public ResponseEntity<StudyMaterialFolderResponse> updateFolder(
            @PathVariable UUID folderId,
            @Valid @RequestBody UpdateFolderRequest request
    ) {
        return ResponseEntity.ok(studyMaterialService.updateFolder(folderId, request));
    }

    @DeleteMapping("/folders/{folderId}")
    @Operation(summary = "Delete an empty study material folder", description = "Admin only.")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID folderId) {
        studyMaterialService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/folders")
    @Operation(summary = "Browse study material folders")
    public ResponseEntity<PageResponse<StudyMaterialFolderResponse>> listFolders(
            @RequestParam(required = false) UUID parentId,
            @Parameter(description = "Supported sort fields: name, createdAtUtc, updatedAtUtc.")
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(studyMaterialService.listFolders(parentId, pageable)));
    }

    @PostMapping(value = "/materials/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a study material file", description = "Admin only. Supports PDF, PPT/PPTX, and DOCX.")
    public ResponseEntity<StudyMaterialResponse> uploadFileMaterial(
            @RequestParam(required = false) UUID folderId,
            @RequestParam @Size(max = 250) String title,
            @RequestParam(required = false) @Size(max = 5000) String description,
            @RequestParam MaterialType materialType,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        StudyMaterialResponse response = studyMaterialService.uploadFileMaterial(
                folderId,
                title,
                description,
                materialType,
                file,
                authenticatedUser
        );
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/study-materials/materials/{materialId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/materials/links")
    @Operation(summary = "Create a study material link", description = "Admin only. Supports video and external links.")
    public ResponseEntity<StudyMaterialResponse> createLinkMaterial(
            @Valid @RequestBody CreateLinkMaterialRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        StudyMaterialResponse response = studyMaterialService.createLinkMaterial(request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/study-materials/materials/{materialId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/materials/{materialId}")
    @Operation(summary = "Update study material metadata", description = "Admin only. Link materials may also update externalUrl.")
    public ResponseEntity<StudyMaterialResponse> updateMaterial(
            @PathVariable UUID materialId,
            @Valid @RequestBody UpdateMaterialRequest request
    ) {
        return ResponseEntity.ok(studyMaterialService.updateMaterial(materialId, request));
    }

    @DeleteMapping("/materials/{materialId}")
    @Operation(summary = "Delete a study material", description = "Admin only.")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID materialId) {
        studyMaterialService.deleteMaterial(materialId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/materials")
    @Operation(summary = "Search and browse study materials")
    public ResponseEntity<PageResponse<StudyMaterialResponse>> searchMaterials(
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) MaterialType materialType,
            @RequestParam(required = false) String search,
            @Parameter(description = "Supported sort fields: title, materialType, sourceType, downloadCount, createdAtUtc, updatedAtUtc.")
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAtUtc", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
                studyMaterialService.searchMaterials(folderId, materialType, search, pageable)
        ));
    }

    @GetMapping("/materials/{materialId}")
    @Operation(summary = "View study material details")
    public ResponseEntity<StudyMaterialResponse> getMaterial(@PathVariable UUID materialId) {
        return ResponseEntity.ok(studyMaterialService.getMaterial(materialId));
    }

    @GetMapping("/materials/{materialId}/download")
    @Operation(summary = "Download a study material file and track the download")
    public ResponseEntity<Resource> downloadFileMaterial(
            @PathVariable UUID materialId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        StudyMaterialResponse material = studyMaterialService.getMaterial(materialId);
        Resource resource = studyMaterialService.downloadFileMaterial(materialId, authenticatedUser);
        MediaType mediaType = MediaType.parseMediaType(
                material.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : material.contentType()
        );
        String filename = material.originalFilename() == null ? "study-material" : material.originalFilename();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename.replace("\"", "") + "\"")
                .body(resource);
    }

    @GetMapping("/materials/{materialId}/link")
    @Operation(summary = "Access a study material link and track the access")
    public ResponseEntity<LinkDownloadResponse> accessLinkMaterial(
            @PathVariable UUID materialId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(studyMaterialService.accessLinkMaterial(materialId, authenticatedUser));
    }
}

