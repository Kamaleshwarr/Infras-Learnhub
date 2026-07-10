package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.ExternalContactType;

import java.time.Instant;
import java.util.UUID;

public record ProjectExternalContactResponse(
        UUID id,
        UUID projectId,
        String name,
        ExternalContactType contactType,
        String roleTitle,
        String organization,
        String email,
        String phone,
        String contactUrl,
        String notes,
        boolean primaryContact,
        int displayOrder,
        boolean active,
        ProjectUserResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}
