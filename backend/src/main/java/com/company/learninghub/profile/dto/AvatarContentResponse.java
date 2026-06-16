package com.company.learninghub.profile.dto;

import org.springframework.core.io.Resource;

import java.time.Instant;

public record AvatarContentResponse(
        Resource resource,
        String contentType,
        Instant updatedAtUtc
) {
}
