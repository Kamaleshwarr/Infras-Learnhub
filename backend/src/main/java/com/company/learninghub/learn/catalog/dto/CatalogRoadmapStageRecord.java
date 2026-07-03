package com.company.learninghub.learn.catalog.dto;

import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogRoadmapStageRecord(
        int order,
        String slug,
        String title,
        String description,
        String estimatedEffort,
        String notes,
        List<CatalogRoadmapResourceRecord> learningResources,
        List<CatalogRoadmapResourceRecord> practiceResources
) {
}
