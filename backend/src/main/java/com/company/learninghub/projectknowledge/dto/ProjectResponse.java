package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.learn.dto.RelatedTechnologySummary;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.domain.ProjectStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectAccessType accessType,
        ProjectStatus status,
        boolean archived,
        ProjectUserResponse createdBy,
        ProjectUserResponse owner,
        Integer memberCount,
        ProjectRole currentMemberRole,
        List<RelatedTechnologySummary> relatedTechnologies,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

