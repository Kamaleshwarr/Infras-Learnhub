package com.company.learninghub.submission.dto;

import java.util.UUID;

public record CertificateDocumentResponse(
        UUID id,
        String originalFilename,
        String contentType,
        long fileSizeBytes
) {
}

