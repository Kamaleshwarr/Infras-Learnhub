package com.company.learninghub.learn.dto;

import java.time.Instant;

public record CatalogImportStatusResponse(
        String catalogVersion,
        Instant importedAt,
        String packageType,
        int recordsUpserted,
        String status
) {
}
