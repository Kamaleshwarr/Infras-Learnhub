package com.company.learninghub.learn.catalog;

import com.company.learninghub.learn.catalog.dto.CatalogManifest;
import com.company.learninghub.learn.catalog.dto.CatalogManifestPackage;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyPackage;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyRecord;
import com.company.learninghub.learn.domain.CatalogImportStatus;
import com.company.learninghub.learn.domain.LearnCatalogImport;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.repository.LearnCatalogImportRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "app.catalog.import", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CatalogImportService implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogImportService.class);
    private static final String CATALOG_ROOT = "catalog/";
    private static final String MANIFEST_PATH = CATALOG_ROOT + "manifest.json";
    private static final String TECHNOLOGIES_PACKAGE_TYPE = "technologies";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@learninghub.local";

    private final CatalogImportProperties properties;
    private final CatalogSchemaValidator schemaValidator;
    private final LearnTechnologyRepository technologyRepository;
    private final LearnCatalogImportRepository catalogImportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CatalogImportService(
            CatalogImportProperties properties,
            CatalogSchemaValidator schemaValidator,
            LearnTechnologyRepository technologyRepository,
            LearnCatalogImportRepository catalogImportRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.schemaValidator = schemaValidator;
        this.technologyRepository = technologyRepository;
        this.catalogImportRepository = catalogImportRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            LOGGER.info("Catalog import is disabled");
            return;
        }

        try {
            importCatalog();
        } catch (RuntimeException exception) {
            if (properties.isFailFast()) {
                throw exception;
            }
            LOGGER.error("Catalog import failed but fail-fast is disabled", exception);
        }
    }

    @Transactional
    public void importCatalog() {
        CatalogManifest manifest = readManifest();
        validateManifest(manifest);

        if (isCatalogVersionAlreadyImported(manifest.catalogVersion())) {
            LOGGER.info("Catalog version {} already imported; skipping", manifest.catalogVersion());
            return;
        }

        int totalUpserted = 0;
        for (CatalogManifestPackage catalogPackage : manifest.packages()) {
            if (!TECHNOLOGIES_PACKAGE_TYPE.equals(catalogPackage.type())) {
                continue;
            }
            totalUpserted += importTechnologyPackage(manifest.catalogVersion(), catalogPackage);
        }

        LOGGER.info(
                "Catalog version {} imported successfully ({} technology records upserted)",
                manifest.catalogVersion(),
                totalUpserted
        );
    }

    private boolean isCatalogVersionAlreadyImported(String catalogVersion) {
        return catalogImportRepository.existsByCatalogVersionAndPackageTypeAndStatus(
                catalogVersion,
                TECHNOLOGIES_PACKAGE_TYPE,
                CatalogImportStatus.SUCCESS
        );
    }

    private int importTechnologyPackage(String catalogVersion, CatalogManifestPackage catalogPackage) {
        String packagePath = CATALOG_ROOT + catalogPackage.path();
        CatalogTechnologyPackage technologyPackage = readTechnologyPackage(packagePath);
        schemaValidator.validateTechnologyPackage(packagePath, technologyPackage);

        Set<String> importedSlugs = new HashSet<>();
        User importOwner = resolveImportOwner();
        int upserted = 0;

        for (CatalogTechnologyRecord record : technologyPackage.technologies()) {
            validateTechnologyRecord(record);
            upsertTechnology(catalogVersion, record, importOwner);
            importedSlugs.add(record.slug());
            upserted++;
        }

        softHideRemovedTechnologies(importedSlugs);

        catalogImportRepository.save(new LearnCatalogImport(
                catalogVersion,
                Instant.now(),
                TECHNOLOGIES_PACKAGE_TYPE,
                upserted,
                CatalogImportStatus.SUCCESS
        ));

        return upserted;
    }

    private void upsertTechnology(String catalogVersion, CatalogTechnologyRecord record, User importOwner) {
        LearnTechnology technology = technologyRepository.findBySlug(record.slug())
                .orElseGet(() -> new LearnTechnology(
                        record.slug(),
                        record.name(),
                        record.shortName(),
                        record.shortDescription(),
                        record.category(),
                        record.difficulty(),
                        TechnologyStatus.HIDDEN,
                        record.featured() != null && record.featured(),
                        importOwner
                ));

        technology.applyCatalogData(
                record.name(),
                record.shortName(),
                record.shortDescription(),
                record.category(),
                record.difficulty(),
                record.featured() != null && record.featured(),
                record.estimatedDuration(),
                record.officialWebsite(),
                record.officialDocumentation(),
                record.tags(),
                record.version(),
                record.source(),
                record.sourceUrl(),
                parseInstant(record.updatedAt())
        );

        technologyRepository.save(technology);
    }

    private void softHideRemovedTechnologies(Set<String> importedSlugs) {
        Map<String, LearnTechnology> existingBySlug = technologyRepository.findByCatalogPresentTrue()
                .stream()
                .collect(Collectors.toMap(LearnTechnology::getSlug, Function.identity()));

        for (Map.Entry<String, LearnTechnology> entry : existingBySlug.entrySet()) {
            if (!importedSlugs.contains(entry.getKey())) {
                LearnTechnology technology = entry.getValue();
                if (technology.hasOrganizationCuration()) {
                    technology.markCatalogAbsent();
                    technologyRepository.save(technology);
                }
            }
        }
    }

    private void validateManifest(CatalogManifest manifest) {
        if (!StringUtils.hasText(manifest.catalogVersion())) {
            throw new CatalogImportException("Catalog manifest catalogVersion is required");
        }
        if (manifest.packages() == null || manifest.packages().isEmpty()) {
            throw new CatalogImportException("Catalog manifest packages are required");
        }
    }

    private void validateTechnologyRecord(CatalogTechnologyRecord record) {
        if (!StringUtils.hasText(record.slug())) {
            throw new CatalogImportException("Technology slug is required");
        }
        if (!StringUtils.hasText(record.name())) {
            throw new CatalogImportException("Technology name is required for slug " + record.slug());
        }
        if (record.category() == null) {
            throw new CatalogImportException("Technology category is required for slug " + record.slug());
        }
        if (record.difficulty() == null) {
            throw new CatalogImportException("Technology difficulty is required for slug " + record.slug());
        }
        validateHttpsUrl(record.officialWebsite(), "officialWebsite", record.slug());
        validateHttpsUrl(record.officialDocumentation(), "officialDocumentation", record.slug());
        validateHttpsUrl(record.sourceUrl(), "sourceUrl", record.slug());
    }

    private void validateHttpsUrl(String url, String fieldName, String slug) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        if (!url.startsWith("https://")) {
            throw new CatalogImportException(
                    "Technology " + slug + " field " + fieldName + " must use HTTPS"
            );
        }
    }

    private CatalogManifest readManifest() {
        return readJson(MANIFEST_PATH, CatalogManifest.class);
    }

    private CatalogTechnologyPackage readTechnologyPackage(String path) {
        return readJson(path, CatalogTechnologyPackage.class);
    }

    private <T> T readJson(String path, Class<T> type) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException exception) {
            throw new CatalogImportException("Unable to read catalog file: " + path, exception);
        }
    }

    private User resolveImportOwner() {
        return userRepository.findByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL)
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new CatalogImportException(
                                "No user available to own catalog-imported technologies"
                        )));
    }

    private Instant parseInstant(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Instant.parse(value);
    }
}
