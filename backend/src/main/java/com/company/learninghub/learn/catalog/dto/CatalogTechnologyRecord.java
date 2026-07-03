package com.company.learninghub.learn.catalog.dto;

import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogTechnologyRecord(
        String slug,
        String version,
        String name,
        String shortName,
        TechnologyCategory category,
        String shortDescription,
        TechnologyDifficulty difficulty,
        String estimatedDuration,
        String officialWebsite,
        String officialDocumentation,
        List<String> tags,
        Boolean featured,
        String source,
        String sourceUrl,
        String updatedAt
) {
}
