package com.company.learninghub.leaderboard.dto;

import java.time.Instant;

public record GlobalLeaderboardEntryResponse(
        long rank,
        LeaderboardEmployeeResponse employee,
        long totalApprovedCertifications,
        Instant earliestSubmittedAtUtc,
        Instant latestApprovedAtUtc
) {
}

