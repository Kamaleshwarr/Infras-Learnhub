package com.company.learninghub.learn.dto;

import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TechnologyResponse(
        UUID id,
        String name,
        String shortName,
        String description,
        TechnologyCategory category,
        TechnologyDifficulty difficulty,
        TechnologyStatus status,
        boolean featured,
        List<RelatedProjectSummary> relatedProjects,
        TechnologyCreatedByResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}
