package com.company.learninghub.leaderboard.dto;

import java.time.Instant;
import java.util.UUID;

public record InitiativeLeaderboardEntryResponse(
        long rank,
        UUID submissionId,
        LeaderboardEmployeeResponse employee,
        UUID initiativeId,
        String initiativeTitle,
        Instant submittedAtUtc,
        Instant approvedAtUtc
) {
}

