package com.company.learninghub.submission.dto;

import com.company.learninghub.initiative.domain.InitiativeStatus;

import java.util.UUID;

public record SubmissionInitiativeResponse(
        UUID id,
        String title,
        InitiativeStatus status
) {
}

