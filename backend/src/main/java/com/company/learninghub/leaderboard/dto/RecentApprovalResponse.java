package com.company.learninghub.leaderboard.dto;

import java.time.Instant;
import java.util.UUID;

public record RecentApprovalResponse(
        UUID submissionId,
        UUID initiativeId,
        String initiativeTitle,
        Instant submittedAtUtc,
        Instant approvedAtUtc
) {
}

