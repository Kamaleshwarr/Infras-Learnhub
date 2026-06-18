package com.company.learninghub.submission.dto;

import org.springframework.core.io.Resource;

public record CertificateContent(
        Resource resource,
        String contentType,
        String originalFilename
) {
}
