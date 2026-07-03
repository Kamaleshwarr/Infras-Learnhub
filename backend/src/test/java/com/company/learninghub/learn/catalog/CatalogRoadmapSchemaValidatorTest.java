package com.company.learninghub.learn.catalog;

import com.company.learninghub.learn.catalog.dto.CatalogRoadmapRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapResourceRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapStageRecord;
import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogRoadmapSchemaValidatorTest {

    @Test
    void schemaValidatorRejectsNonContiguousStageOrder() {
        CatalogSchemaValidator validator = new CatalogSchemaValidator(new ObjectMapper().findAndRegisterModules());
        CatalogRoadmapRecord record = new CatalogRoadmapRecord(
                "java",
                "1.0.0",
                "platform-team",
                "https://roadmap.sh/java",
                "2026-07-03T00:00:00Z",
                "Java roadmap",
                List.of(
                        stage(1, "introduction"),
                        stage(2, "core-java"),
                        stage(4, "collections")
                )
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/java.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("contiguous order");
    }

    private CatalogRoadmapStageRecord stage(int order, String slug) {
        return new CatalogRoadmapStageRecord(
                order,
                slug,
                "Title",
                "Description",
                "1 week",
                null,
                List.of(resource(slug + "-docs")),
                List.of(resource(slug + "-practice"))
        );
    }

    private CatalogRoadmapResourceRecord resource(String slug) {
        return new CatalogRoadmapResourceRecord(
                slug,
                "Docs",
                "https://example.com/" + slug,
                RoadmapResourceType.OFFICIAL_DOCUMENTATION,
                "Provider",
                RoadmapResourceCost.FREE,
                "1.0.0",
                "provider",
                "2026-07-03T00:00:00Z"
        );
    }
}
