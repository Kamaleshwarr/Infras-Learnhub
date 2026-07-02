package com.company.learninghub.learn.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogTechnologyPackage(
        String packageVersion,
        String type,
        String updatedAt,
        List<CatalogTechnologyRecord> technologies
) {
}
