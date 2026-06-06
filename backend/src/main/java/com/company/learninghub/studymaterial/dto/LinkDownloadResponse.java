package com.company.learninghub.studymaterial.dto;

import java.util.UUID;

public record LinkDownloadResponse(
        UUID materialId,
        String externalUrl,
        long downloadCount
) {
}

