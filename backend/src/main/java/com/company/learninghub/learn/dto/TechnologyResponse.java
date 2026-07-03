package com.company.learninghub.learn.dto;

import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TechnologyResponse(
        UUID id,
        String slug,
        String name,
        String shortName,
        String description,
        TechnologyCategory category,
        TechnologyDifficulty difficulty,
        TechnologyStatus status,
        boolean featured,
        Boolean featuredOverride,
        boolean catalogFeatured,
        String estimatedDuration,
        String officialWebsite,
        String officialDocumentation,
        List<String> tags,
        String orgNotes,
        String catalogVersion,
        String catalogSource,
        boolean catalogPresent,
        List<RelatedProjectSummary> relatedProjects,
        TechnologyCreatedByResponse createdBy,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}
