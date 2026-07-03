package com.company.learninghub.learn.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.learn.dto.CompleteStageRequest;
import com.company.learninghub.learn.dto.CreateEnrollmentRequest;
import com.company.learninghub.learn.dto.EnrollmentResponse;
import com.company.learninghub.learn.dto.JourneyResponse;
import com.company.learninghub.learn.dto.TechnologyProgressResponse;
import com.company.learninghub.learn.service.LearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learn")
@Tag(name = "Learn Progress", description = "Employee-owned learning journey and stage progress")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class LearningProgressController {

    private final LearningProgressService progressService;

    public LearningProgressController(LearningProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/enrollments")
    @Operation(summary = "Enroll in a technology roadmap", description = "Creates an enrollment and starts learning at stage 1.")
    public ResponseEntity<EnrollmentResponse> enroll(
            @Valid @RequestBody CreateEnrollmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressService.enroll(request, authenticatedUser));
    }

    @PostMapping("/enrollments/{enrollmentId}/start")
    @Operation(summary = "Start learning", description = "Moves a NOT_STARTED enrollment to IN_PROGRESS.")
    public ResponseEntity<EnrollmentResponse> startLearning(
            @PathVariable UUID enrollmentId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(progressService.startLearning(enrollmentId, authenticatedUser));
    }

    @PostMapping("/enrollments/{enrollmentId}/complete-stage")
    @Operation(summary = "Complete a roadmap stage", description = "Marks the next sequential stage complete for the enrollment.")
    public ResponseEntity<EnrollmentResponse> completeStage(
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody CompleteStageRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(progressService.completeStage(enrollmentId, request, authenticatedUser));
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Leave enrollment", description = "Ends the active learning journey while preserving history.")
    public ResponseEntity<Void> leaveEnrollment(
            @PathVariable UUID enrollmentId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        progressService.leaveEnrollment(enrollmentId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/journey")
    @Operation(summary = "Get learning journey", description = "Returns continue learning, active, completed, and left enrollments.")
    public ResponseEntity<JourneyResponse> getJourney(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(progressService.getJourney(authenticatedUser));
    }

    @GetMapping("/progress/technologies/{technologyId}")
    @Operation(summary = "Get technology progress", description = "Returns the authenticated user's progress overlay for a technology roadmap.")
    public ResponseEntity<TechnologyProgressResponse> getTechnologyProgress(
            @PathVariable UUID technologyId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(progressService.getTechnologyProgress(technologyId, authenticatedUser));
    }

    @GetMapping("/enrollments/technologies/{technologyId}")
    @Operation(summary = "Get active enrollment", description = "Returns the authenticated user's active enrollment for a technology.")
    public ResponseEntity<EnrollmentResponse> getActiveEnrollment(
            @PathVariable UUID technologyId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(progressService.getActiveEnrollment(technologyId, authenticatedUser));
    }
}
