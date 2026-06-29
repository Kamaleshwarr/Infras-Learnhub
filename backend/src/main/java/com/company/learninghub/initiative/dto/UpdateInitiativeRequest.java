package com.company.learninghub.initiative.dto;

import com.company.learninghub.initiative.domain.InitiativeStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateInitiativeRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 5000)
        String description,

        @Size(max = 2000)
        String rewardDescription,

        @NotNull
        Instant startDateUtc,

        @NotNull
        Instant expiryDateUtc,

        @NotNull
        InitiativeStatus status
) {

    @AssertTrue(message = "expiryDateUtc must be on or after startDateUtc")
    public boolean isDateRangeValid() {
        return startDateUtc == null || expiryDateUtc == null || !expiryDateUtc.isBefore(startDateUtc);
    }
}

