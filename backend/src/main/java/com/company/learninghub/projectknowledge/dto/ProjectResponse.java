package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.learn.dto.RelatedTechnologySummary;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectAccessType accessType,
        boolean archived,
        ProjectUserResponse createdBy,
        List<RelatedTechnologySummary> relatedTechnologies,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

