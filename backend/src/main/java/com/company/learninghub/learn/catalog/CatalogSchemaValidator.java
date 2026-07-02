package com.company.learninghub.learn.catalog;

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
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CatalogSchemaValidator {

    private static final String TECHNOLOGY_SCHEMA_PATH = "catalog/schemas/technology.schema.json";

    private final ObjectMapper objectMapper;
    private final JsonSchema technologySchema;

    public CatalogSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.technologySchema = loadSchema(TECHNOLOGY_SCHEMA_PATH);
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
            validateRecord(record);
        }
    }

    private void validateRecord(CatalogTechnologyRecord record) {
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

    private JsonSchema loadSchema(String path) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            return factory.getSchema(inputStream);
        } catch (IOException exception) {
            throw new CatalogImportException("Unable to load catalog schema: " + path, exception);
        }
    }
}
