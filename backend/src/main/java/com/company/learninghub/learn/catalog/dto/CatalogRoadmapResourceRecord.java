package com.company.learninghub.learn.catalog.dto;

import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogRoadmapResourceRecord(
        String slug,
        String title,
        String url,
        RoadmapResourceType type,
        String provider,
        RoadmapResourceCost freePaid,
        String version,
        String source,
        String updatedAt
) {
}
