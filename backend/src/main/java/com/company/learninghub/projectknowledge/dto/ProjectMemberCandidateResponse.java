package com.company.learninghub.projectknowledge.dto;

import java.util.UUID;

public record ProjectMemberCandidateResponse(
        UUID id,
        String employeeId,
        String fullName,
        String email
) {
}
