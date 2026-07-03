package com.company.learninghub.learn.catalog.dto;

import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogRoadmapRecord(
        String technologySlug,
        String version,
        String source,
        String sourceUrl,
        String updatedAt,
        String description,
        List<CatalogRoadmapStageRecord> stages
) {
}
