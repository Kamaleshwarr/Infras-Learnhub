package com.company.learninghub.learn.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogManifestPackage(
        String type,
        String path,
        String version,
        Integer recordCount,
        String pattern
) {
}
