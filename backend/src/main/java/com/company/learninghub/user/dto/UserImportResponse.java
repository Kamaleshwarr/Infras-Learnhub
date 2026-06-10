package com.company.learninghub.user.dto;

import java.util.List;

public record UserImportResponse(
        int totalRows,
        int imported,
        int failed,
        List<String> errors
) {
}

