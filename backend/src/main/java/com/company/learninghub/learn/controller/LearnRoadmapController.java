package com.company.learninghub.learn.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.learn.dto.RoadmapResponse;
import com.company.learninghub.learn.service.LearnRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learn/technologies")
@Tag(name = "Learn Roadmaps", description = "Read-only roadmap viewer for catalog technologies")
@SecurityRequirement(name = "bearerAuth")
public class LearnRoadmapController {

    private final LearnRoadmapService roadmapService;

    public LearnRoadmapController(LearnRoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping("/{slug}/roadmap")
    @Operation(
            summary = "View roadmap by technology slug",
            description = "Returns the catalog-imported roadmap with ordered stages and external learning resources."
    )
    public ResponseEntity<RoadmapResponse> getBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(roadmapService.getRoadmapBySlug(slug, authenticatedUser));
    }

    @GetMapping("/id/{technologyId}/roadmap")
    @Operation(
            summary = "View roadmap by technology id",
            description = "Returns the catalog-imported roadmap for a technology UUID."
    )
    public ResponseEntity<RoadmapResponse> getByTechnologyId(
            @PathVariable UUID technologyId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(roadmapService.getRoadmapByTechnologyId(technologyId, authenticatedUser));
    }
}
