package com.company.learninghub.learn.catalog;

import com.company.learninghub.learn.catalog.dto.CatalogRoadmapRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapResourceRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapStageRecord;
import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogRoadmapCatalogQualityTest {

    private static final List<String> PRODUCTION_ROADMAPS = List.of(
            "catalog/roadmaps/java.json",
            "catalog/roadmaps/spring-boot.json",
            "catalog/roadmaps/react.json",
            "catalog/roadmaps/docker.json",
            "catalog/roadmaps/aws.json"
    );

    private CatalogSchemaValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        validator = new CatalogSchemaValidator(objectMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "catalog/roadmaps/java.json",
            "catalog/roadmaps/spring-boot.json",
            "catalog/roadmaps/react.json",
            "catalog/roadmaps/docker.json",
            "catalog/roadmaps/aws.json"
    })
    void productionRoadmapsImportSuccessfully(String roadmapPath) throws IOException {
        CatalogRoadmapRecord record = readRoadmap(roadmapPath);

        assertThatCode(() -> validator.validateRoadmapRecord(roadmapPath, record))
                .doesNotThrowAnyException();

        for (CatalogRoadmapStageRecord stage : record.stages()) {
            assert stage.learningResources() != null && !stage.learningResources().isEmpty();
            assert stage.practiceResources() != null && !stage.practiceResources().isEmpty();
        }
    }

    @Test
    void rejectsMissingPracticeResources() {
        CatalogRoadmapRecord record = roadmapWithStages(
                stage(1, "introduction", List.of(resource("learn-1")), List.of()),
                stage(2, "core", List.of(resource("learn-2")), List.of(resource("practice-2"))),
                stage(3, "advanced", List.of(resource("learn-3")), List.of(resource("practice-3")))
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/test.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("requires at least one practice resource");
    }

    @Test
    void rejectsEmptyLearningResources() {
        CatalogRoadmapRecord record = roadmapWithStages(
                stage(1, "introduction", List.of(), List.of(resource("practice-1"))),
                stage(2, "core", List.of(resource("learn-2")), List.of(resource("practice-2"))),
                stage(3, "advanced", List.of(resource("learn-3")), List.of(resource("practice-3")))
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/test.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("requires at least one learning resource");
    }

    @Test
    void rejectsInvalidHttpsUrl() {
        CatalogRoadmapRecord record = roadmapWithStages(
                stage(
                        1,
                        "introduction",
                        List.of(resourceWithUrl("learn-1", "http://insecure.example.com")),
                        List.of(resource("practice-1"))
                ),
                stage(2, "core", List.of(resource("learn-2")), List.of(resource("practice-2"))),
                stage(3, "advanced", List.of(resource("learn-3")), List.of(resource("practice-3")))
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/test.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("must use HTTPS");
    }

    @Test
    void rejectsDuplicateResourceUrlWithinStage() {
        CatalogRoadmapRecord record = roadmapWithStages(
                stage(
                        1,
                        "introduction",
                        List.of(resourceWithUrl("learn-1", "https://example.com/shared")),
                        List.of(resourceWithUrl("practice-1", "https://example.com/shared"))
                ),
                stage(2, "core", List.of(resource("learn-2")), List.of(resource("practice-2"))),
                stage(3, "advanced", List.of(resource("learn-3")), List.of(resource("practice-3")))
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/test.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("Duplicate resource URL");
    }

    @Test
    void rejectsDuplicateStageOrder() {
        CatalogRoadmapRecord record = roadmapWithStages(
                stage(1, "introduction", List.of(resource("learn-1")), List.of(resource("practice-1"))),
                stage(1, "duplicate-order", List.of(resource("learn-2")), List.of(resource("practice-2"))),
                stage(3, "advanced", List.of(resource("learn-3")), List.of(resource("practice-3")))
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/test.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("contiguous order");
    }

    @Test
    void rejectsEmptyRoadmap() {
        CatalogRoadmapRecord record = new CatalogRoadmapRecord(
                "java",
                "1.0.0",
                "platform-team",
                "https://roadmap.sh/java",
                "2026-07-03T07:40:00Z",
                "Empty roadmap",
                List.of()
        );

        assertThatThrownBy(() -> validator.validateRoadmapRecord("catalog/roadmaps/empty.json", record))
                .isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("at least one stage");
    }

    private CatalogRoadmapRecord roadmapWithStages(CatalogRoadmapStageRecord... stages) {
        return new CatalogRoadmapRecord(
                "java",
                "1.0.0",
                "platform-team",
                "https://roadmap.sh/java",
                "2026-07-03T07:40:00Z",
                "Test roadmap",
                List.of(stages)
        );
    }

    private CatalogRoadmapStageRecord stage(
            int order,
            String slug,
            List<CatalogRoadmapResourceRecord> learningResources,
            List<CatalogRoadmapResourceRecord> practiceResources
    ) {
        return new CatalogRoadmapStageRecord(
                order,
                slug,
                "Title for " + slug,
                "Description for " + slug,
                "1 week",
                null,
                learningResources,
                practiceResources
        );
    }

    private CatalogRoadmapResourceRecord resource(String slug) {
        return resourceWithUrl(slug, "https://example.com/" + slug);
    }

    private CatalogRoadmapResourceRecord resourceWithUrl(String slug, String url) {
        return new CatalogRoadmapResourceRecord(
                slug,
                "Resource " + slug,
                url,
                RoadmapResourceType.OFFICIAL_DOCUMENTATION,
                "Provider",
                RoadmapResourceCost.FREE,
                "1.0.0",
                "provider",
                "2026-07-03T07:40:00Z"
        );
    }

    private CatalogRoadmapRecord readRoadmap(String path) throws IOException {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(inputStream, CatalogRoadmapRecord.class);
        }
    }
}
