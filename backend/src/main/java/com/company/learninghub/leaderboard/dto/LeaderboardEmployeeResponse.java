package com.company.learninghub.leaderboard.dto;

import java.util.UUID;

public record LeaderboardEmployeeResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email
) {
}

