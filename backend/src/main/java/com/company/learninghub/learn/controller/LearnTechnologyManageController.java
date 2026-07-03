package com.company.learninghub.learn.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.TechnologyCreateRequest;
import com.company.learninghub.learn.dto.TechnologyProjectLinkRequest;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.dto.TechnologyUpdateRequest;
import com.company.learninghub.learn.service.LearnTechnologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learn/manage/technologies")
@Tag(name = "Learn Technology Management", description = "Admin management for Learn technologies")
@SecurityRequirement(name = "bearerAuth")
public class LearnTechnologyManageController {

    private final LearnTechnologyService technologyService;

    public LearnTechnologyManageController(LearnTechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @GetMapping
    @Operation(
            summary = "List technologies for admin management",
            description = "Supports status, search, category, difficulty, pagination, and sorting."
    )
    public ResponseEntity<PageResponse<TechnologyResponse>> list(
            @RequestParam(required = false) TechnologyStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TechnologyCategory category,
            @RequestParam(required = false) TechnologyDifficulty difficulty,
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields include name, status, category, difficulty, featured, createdAtUtc, and updatedAtUtc."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAtUtc", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
                technologyService.listAdminTechnologies(status, search, category, difficulty, pageable)
        ));
    }

    @PostMapping
    @Operation(summary = "Create a technology", description = "Admin only. Creates a DRAFT technology.")
    public ResponseEntity<TechnologyResponse> create(
            @Valid @RequestBody TechnologyCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        TechnologyResponse response = technologyService.create(request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{technologyId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{technologyId}")
    @Operation(summary = "Update a technology", description = "Admin only.")
    public ResponseEntity<TechnologyResponse> update(
            @PathVariable UUID technologyId,
            @Valid @RequestBody TechnologyUpdateRequest request
    ) {
        return ResponseEntity.ok(technologyService.update(technologyId, request));
    }

    @PostMapping("/{technologyId}/publish")
    @Operation(summary = "Publish a draft technology", description = "Admin only. Transitions DRAFT to PUBLISHED.")
    public ResponseEntity<TechnologyResponse> publish(@PathVariable UUID technologyId) {
        return ResponseEntity.ok(technologyService.publish(technologyId));
    }

    @PostMapping("/{technologyId}/archive")
    @Operation(summary = "Archive a published technology", description = "Admin only. Transitions PUBLISHED to ARCHIVED.")
    public ResponseEntity<TechnologyResponse> archive(@PathVariable UUID technologyId) {
        return ResponseEntity.ok(technologyService.archive(technologyId));
    }

    @PostMapping("/{technologyId}/project-links")
    @Operation(summary = "Link an organizational project", description = "Admin only.")
    public ResponseEntity<TechnologyResponse> addProjectLink(
            @PathVariable UUID technologyId,
            @Valid @RequestBody TechnologyProjectLinkRequest request
    ) {
        return ResponseEntity.ok(technologyService.addProjectLink(technologyId, request.projectId()));
    }

    @DeleteMapping("/{technologyId}/project-links/{projectId}")
    @Operation(summary = "Unlink an organizational project", description = "Admin only.")
    public ResponseEntity<TechnologyResponse> removeProjectLink(
            @PathVariable UUID technologyId,
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(technologyService.removeProjectLink(technologyId, projectId));
    }
}
