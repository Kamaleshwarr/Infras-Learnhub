package com.company.learninghub.projectknowledge.dto;

import java.util.UUID;

public record ProjectLinkAccessResponse(
        UUID itemId,
        String externalUrl,
        long accessCount
) {
}

