package com.company.learninghub.initiative.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReactivateInitiativeRequest(
        @NotNull
        Instant expiryDateUtc
) {
}
