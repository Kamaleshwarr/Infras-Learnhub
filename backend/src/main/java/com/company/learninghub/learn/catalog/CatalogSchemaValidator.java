package com.company.learninghub.learn.catalog;

import com.company.learninghub.learn.catalog.dto.CatalogRoadmapRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapResourceRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapStageRecord;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyPackage;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CatalogSchemaValidator {

    private static final String TECHNOLOGY_SCHEMA_PATH = "catalog/schemas/technology.schema.json";
    private static final String ROADMAP_SCHEMA_PATH = "catalog/schemas/roadmap.schema.json";

    private final ObjectMapper objectMapper;
    private final JsonSchema technologySchema;
    private final JsonSchema roadmapSchema;

    public CatalogSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.technologySchema = loadSchema(TECHNOLOGY_SCHEMA_PATH);
        this.roadmapSchema = loadSchema(ROADMAP_SCHEMA_PATH);
    }

    public void validateTechnologyPackage(String packagePath, CatalogTechnologyPackage technologyPackage) {
        if (technologyPackage.technologies() == null || technologyPackage.technologies().isEmpty()) {
            throw new CatalogImportException("Technology package is empty: " + packagePath);
        }

        Set<String> slugs = new HashSet<>();
        for (CatalogTechnologyRecord record : technologyPackage.technologies()) {
            if (!slugs.add(record.slug())) {
                throw new CatalogImportException("Duplicate technology slug in package: " + record.slug());
            }
        }

        for (CatalogTechnologyRecord record : technologyPackage.technologies()) {
            validateTechnologyRecord(record);
        }
    }

    public void validateRoadmapRecord(String filePath, CatalogRoadmapRecord record) {
        if (!StringUtils.hasText(record.technologySlug())) {
            throw new CatalogImportException("Roadmap technologySlug is required: " + filePath);
        }
        if (record.stages() == null || record.stages().isEmpty()) {
            throw new CatalogImportException(
                    "Roadmap " + record.technologySlug() + " must contain at least one stage: " + filePath
            );
        }
        if (record.stages().size() < 3) {
            throw new CatalogImportException(
                    "Roadmap " + record.technologySlug() + " must contain at least 3 stages"
            );
        }

        Set<String> stageSlugs = new HashSet<>();
        int expectedOrder = 1;
        for (CatalogRoadmapStageRecord stage : record.stages()) {
            validateStageContent(record.technologySlug(), stage);
            if (stage.order() != expectedOrder) {
                throw new CatalogImportException(
                        "Roadmap " + record.technologySlug() + " stages must have contiguous order starting at 1"
                );
            }
            if (!stageSlugs.add(stage.slug())) {
                throw new CatalogImportException(
                        "Duplicate stage slug in roadmap " + record.technologySlug() + ": " + stage.slug()
                );
            }
            validateStageResources(record.technologySlug(), stage.slug(), stage.learningResources(), "learning");
            validateStageResources(record.technologySlug(), stage.slug(), stage.practiceResources(), "practice");
            validateNoDuplicateUrlsInStage(record.technologySlug(), stage);
            expectedOrder++;
        }

        validateRoadmapSchema(record);
    }

    private void validateStageContent(String technologySlug, CatalogRoadmapStageRecord stage) {
        if (!StringUtils.hasText(stage.title())) {
            throw new CatalogImportException(
                    "Stage " + stage.slug() + " in roadmap " + technologySlug + " requires a title"
            );
        }
        if (!StringUtils.hasText(stage.description())) {
            throw new CatalogImportException(
                    "Stage " + stage.slug() + " in roadmap " + technologySlug + " requires a description"
            );
        }
        if (!StringUtils.hasText(stage.estimatedEffort())) {
            throw new CatalogImportException(
                    "Stage " + stage.slug() + " in roadmap " + technologySlug + " requires estimated effort"
            );
        }
        if (stage.learningResources() == null || stage.learningResources().isEmpty()) {
            throw new CatalogImportException(
                    "Stage " + stage.slug() + " in roadmap " + technologySlug
                            + " requires at least one learning resource"
            );
        }
        if (stage.practiceResources() == null || stage.practiceResources().isEmpty()) {
            throw new CatalogImportException(
                    "Stage " + stage.slug() + " in roadmap " + technologySlug
                            + " requires at least one practice resource"
            );
        }
    }

    private void validateNoDuplicateUrlsInStage(String technologySlug, CatalogRoadmapStageRecord stage) {
        Set<String> urls = new HashSet<>();
        for (CatalogRoadmapResourceRecord resource : stage.learningResources()) {
            assertUniqueUrl(technologySlug, stage.slug(), resource, urls);
        }
        for (CatalogRoadmapResourceRecord resource : stage.practiceResources()) {
            assertUniqueUrl(technologySlug, stage.slug(), resource, urls);
        }
    }

    private void assertUniqueUrl(
            String technologySlug,
            String stageSlug,
            CatalogRoadmapResourceRecord resource,
            Set<String> urls
    ) {
        if (!urls.add(resource.url())) {
            throw new CatalogImportException(
                    "Duplicate resource URL in roadmap " + technologySlug
                            + " stage " + stageSlug + ": " + resource.url()
            );
        }
    }

    private void validateStageResources(
            String technologySlug,
            String stageSlug,
            List<CatalogRoadmapResourceRecord> resources,
            String resourceKind
    ) {
        if (resources == null || resources.isEmpty()) {
            throw new CatalogImportException(
                    "Stage " + stageSlug + " in roadmap " + technologySlug
                            + " requires at least one " + resourceKind + " resource"
            );
        }

        Set<String> resourceSlugs = new HashSet<>();
        for (CatalogRoadmapResourceRecord resource : resources) {
            if (!resourceSlugs.add(resource.slug())) {
                throw new CatalogImportException(
                        "Duplicate resource slug in roadmap " + technologySlug
                                + " stage " + stageSlug + ": " + resource.slug()
                );
            }
            if (!StringUtils.hasText(resource.url()) || !resource.url().startsWith("https://")) {
                throw new CatalogImportException(
                        "Resource " + resource.slug() + " in roadmap " + technologySlug
                                + " stage " + stageSlug + " must use HTTPS"
                );
            }
        }
    }

    private void validateTechnologyRecord(CatalogTechnologyRecord record) {
        try {
            JsonNode node = objectMapper.valueToTree(record);
            Set<ValidationMessage> errors = technologySchema.validate(node);
            if (!errors.isEmpty()) {
                String message = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("; "));
                throw new CatalogImportException(
                        "Technology record failed schema validation for slug "
                                + record.slug()
                                + ": "
                                + message
                );
            }
        } catch (CatalogImportException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new CatalogImportException(
                    "Unable to validate technology record for slug " + record.slug(),
                    exception
            );
        }
    }

    private void validateRoadmapSchema(CatalogRoadmapRecord record) {
        try {
            JsonNode node = objectMapper.valueToTree(record);
            Set<ValidationMessage> errors = roadmapSchema.validate(node);
            if (!errors.isEmpty()) {
                String message = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("; "));
                throw new CatalogImportException(
                        "Roadmap record failed schema validation for "
                                + record.technologySlug()
                                + ": "
                                + message
                );
            }
        } catch (CatalogImportException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new CatalogImportException(
                    "Unable to validate roadmap record for " + record.technologySlug(),
                    exception
            );
        }
    }

    private JsonSchema loadSchema(String path) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            return factory.getSchema(inputStream);
        } catch (IOException exception) {
            throw new CatalogImportException("Unable to load catalog schema: " + path, exception);
        }
    }
}
