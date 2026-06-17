package com.company.learninghub.initiative.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.dto.UpdateInitiativeRequest;
import com.company.learninghub.initiative.service.LearningInitiativeService;
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
@RequestMapping("/api/v1/initiatives")
@Tag(name = "Learning Initiatives", description = "Create, manage, and view learning initiatives")
@SecurityRequirement(name = "bearerAuth")
public class LearningInitiativeController {

    private final LearningInitiativeService initiativeService;

    public LearningInitiativeController(LearningInitiativeService initiativeService) {
        this.initiativeService = initiativeService;
    }

    @PostMapping
    @Operation(summary = "Create a learning initiative", description = "Admin only.")
    public ResponseEntity<InitiativeResponse> create(
            @Valid @RequestBody CreateInitiativeRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        InitiativeResponse response = initiativeService.create(request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{initiativeId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{initiativeId}")
    @Operation(summary = "Update a learning initiative", description = "Admin only.")
    public ResponseEntity<InitiativeResponse> update(
            @PathVariable UUID initiativeId,
            @Valid @RequestBody UpdateInitiativeRequest request
    ) {
        return ResponseEntity.ok(initiativeService.update(initiativeId, request));
    }

    @DeleteMapping("/{initiativeId}")
    @Operation(summary = "Delete a learning initiative", description = "Admin only.")
    public ResponseEntity<Void> delete(@PathVariable UUID initiativeId) {
        initiativeService.delete(initiativeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{initiativeId}")
    @Operation(
            summary = "View a learning initiative",
            description = "Admins can view all initiatives. Employees can view only active initiatives within their UTC date window."
    )
    public ResponseEntity<InitiativeResponse> getById(
            @PathVariable UUID initiativeId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(initiativeService.getById(initiativeId, authenticatedUser));
    }

    @GetMapping
    @Operation(
            summary = "List learning initiatives",
            description = """
                    Admins can view all initiatives and optionally filter by status.
                    Employees receive only active initiatives within their UTC date window.
                    Supports pagination, sorting, and title search.
                    """
    )
    public ResponseEntity<PageResponse<InitiativeResponse>> list(
            @RequestParam(required = false) InitiativeStatus status,
            @RequestParam(required = false) String search,
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields include title, status, startDateUtc, expiryDateUtc, createdAtUtc, and updatedAtUtc."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAtUtc", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(PageResponse.from(
                initiativeService.list(status, search, pageable, authenticatedUser)
        ));
    }
}

