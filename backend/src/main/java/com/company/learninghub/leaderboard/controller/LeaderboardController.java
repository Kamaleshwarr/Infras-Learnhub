package com.company.learninghub.leaderboard.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.leaderboard.dto.GlobalLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.InitiativeLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.PersonalLeaderboardResponse;
import com.company.learninghub.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaderboards")
@Tag(name = "Leaderboards", description = "Initiative, global, and personal learner rankings")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/global")
    @Operation(
            summary = "Get the global leaderboard",
            description = """
                    Ranks employees by total approved certifications descending.
                    Ties are resolved by earliest submittedAtUtc ascending, then employee ID.
                    Only approved submissions are eligible.
                    """
    )
    public ResponseEntity<PageResponse<GlobalLeaderboardEntryResponse>> getGlobalLeaderboard(
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields: rank, employeeId, employeeName, totalApprovedCertifications, earliestSubmittedAtUtc, latestApprovedAtUtc."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "rank", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(leaderboardService.getGlobalLeaderboard(pageable)));
    }

    @GetMapping("/initiatives/{initiativeId}")
    @Operation(
            summary = "Get an initiative leaderboard",
            description = """
                    Ranks approved submissions within one initiative by submittedAtUtc ascending.
                    Tie breakers: approvedAtUtc ascending, then submissionId ascending.
                    Approval determines eligibility only and does not become the primary ranking timestamp.
                    """
    )
    public ResponseEntity<PageResponse<InitiativeLeaderboardEntryResponse>> getInitiativeLeaderboard(
            @PathVariable UUID initiativeId,
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields: rank, submissionId, employeeId, employeeName, submittedAtUtc, approvedAtUtc."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "rank", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
                leaderboardService.getInitiativeLeaderboard(initiativeId, pageable)
        ));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get the current user's personal ranking",
            description = "Returns the current user's global rank, approved certification count, and recent approvals."
    )
    public ResponseEntity<PersonalLeaderboardResponse> getPersonalRanking(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(leaderboardService.getPersonalRanking(authenticatedUser));
    }
}

