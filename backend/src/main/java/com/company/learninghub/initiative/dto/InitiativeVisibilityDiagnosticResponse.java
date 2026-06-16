package com.company.learninghub.initiative.dto;

import com.company.learninghub.initiative.domain.InitiativeStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InitiativeVisibilityDiagnosticResponse(
        UUID id,
        String title,
        InitiativeStatus status,
        Instant startDateUtc,
        Instant expiryDateUtc,
        LocalDate startDateUtcCalendarDay,
        LocalDate expiryDateUtcCalendarDay,
        LocalDate evaluatedOnUtcCalendarDay,
        boolean visibleToEmployees,
        List<String> employeeExclusionReasons,
        boolean includedInAdminActiveList,
        boolean includedInEmployeeList
) {
}
