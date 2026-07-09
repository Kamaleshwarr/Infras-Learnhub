package com.company.learninghub.projectknowledge.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.projectknowledge.dto.ProjectExternalContactRequest;
import com.company.learninghub.projectknowledge.dto.ProjectExternalContactResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberCandidateResponse;
import com.company.learninghub.projectknowledge.service.ProjectTeamService;
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
@RequestMapping("/api/v1/projects/{projectId}")
@Tag(name = "Project Team & Contacts", description = "Project team directory, member lookup, and external contacts")
@SecurityRequirement(name = "bearerAuth")
public class ProjectTeamController {

    private final ProjectTeamService teamService;

    public ProjectTeamController(ProjectTeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/member-candidates")
    @Operation(summary = "Search active users to add as project members", description = "Requires OWNER project role.")
    public ResponseEntity<List<ProjectMemberCandidateResponse>> searchMemberCandidates(
            @PathVariable UUID projectId,
            @RequestParam String search,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(teamService.searchMemberCandidates(projectId, search, authenticatedUser));
    }

    @GetMapping("/contacts")
    @Operation(summary = "List external project contacts")
    public ResponseEntity<List<ProjectExternalContactResponse>> listExternalContacts(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(teamService.listExternalContacts(projectId, authenticatedUser));
    }

    @PostMapping("/contacts")
    @Operation(summary = "Create external project contact", description = "Requires OWNER project role.")
    public ResponseEntity<ProjectExternalContactResponse> createExternalContact(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectExternalContactRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ProjectExternalContactResponse response = teamService.createExternalContact(projectId, request, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{contactId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/contacts/{contactId}")
    @Operation(summary = "Update external project contact", description = "Requires OWNER project role.")
    public ResponseEntity<ProjectExternalContactResponse> updateExternalContact(
            @PathVariable UUID projectId,
            @PathVariable UUID contactId,
            @Valid @RequestBody ProjectExternalContactRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(teamService.updateExternalContact(projectId, contactId, request, authenticatedUser));
    }

    @DeleteMapping("/contacts/{contactId}")
    @Operation(summary = "Delete external project contact", description = "Requires OWNER project role.")
    public ResponseEntity<Void> deleteExternalContact(
            @PathVariable UUID projectId,
            @PathVariable UUID contactId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        teamService.deleteExternalContact(projectId, contactId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
