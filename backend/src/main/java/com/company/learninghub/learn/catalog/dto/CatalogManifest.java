package com.company.learninghub.learn.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogManifest(
        String catalogVersion,
        String releasedAt,
        String description,
        List<CatalogManifestPackage> packages
) {
}
