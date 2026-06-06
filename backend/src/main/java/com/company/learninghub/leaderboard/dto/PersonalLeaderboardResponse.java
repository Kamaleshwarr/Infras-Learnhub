package com.company.learninghub.leaderboard.dto;

import java.time.Instant;
import java.util.List;

public record PersonalLeaderboardResponse(
        LeaderboardEmployeeResponse employee,
        Long globalRank,
        long totalApprovedCertifications,
        Instant earliestSubmittedAtUtc,
        List<RecentApprovalResponse> recentApprovals
) {
}

