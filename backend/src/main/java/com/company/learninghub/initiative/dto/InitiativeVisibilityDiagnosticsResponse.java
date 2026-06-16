package com.company.learninghub.initiative.dto;

import java.time.Instant;
import java.util.List;

public record InitiativeVisibilityDiagnosticsResponse(
        Instant evaluatedAtUtc,
        String adminListPath,
        String employeeListPath,
        List<InitiativeVisibilityDiagnosticResponse> initiatives
) {
}
