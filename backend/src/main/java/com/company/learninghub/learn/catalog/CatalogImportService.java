package com.company.learninghub.learn.catalog;

import com.company.learninghub.learn.catalog.dto.CatalogManifest;
import com.company.learninghub.learn.catalog.dto.CatalogManifestPackage;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapResourceRecord;
import com.company.learninghub.learn.catalog.dto.CatalogRoadmapStageRecord;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyPackage;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyRecord;
import com.company.learninghub.learn.domain.CatalogImportStatus;
import com.company.learninghub.learn.domain.LearnCatalogImport;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.RoadmapResourceKind;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.repository.LearnCatalogImportRepository;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    private static final String ROADMAPS_PACKAGE_TYPE = "roadmaps";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@learninghub.local";

    private final CatalogImportProperties properties;
    private final CatalogSchemaValidator schemaValidator;
    private final LearnTechnologyRepository technologyRepository;
    private final LearnRoadmapRepository roadmapRepository;
    private final LearnCatalogImportRepository catalogImportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CatalogImportService(
            CatalogImportProperties properties,
            CatalogSchemaValidator schemaValidator,
            LearnTechnologyRepository technologyRepository,
            LearnRoadmapRepository roadmapRepository,
            LearnCatalogImportRepository catalogImportRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.schemaValidator = schemaValidator;
        this.technologyRepository = technologyRepository;
        this.roadmapRepository = roadmapRepository;
        this.catalogImportRepository = catalogImportRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
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

        for (CatalogManifestPackage catalogPackage : manifest.packages()) {
            if (TECHNOLOGIES_PACKAGE_TYPE.equals(catalogPackage.type())) {
                if (isPackageAlreadyImported(manifest.catalogVersion(), TECHNOLOGIES_PACKAGE_TYPE)) {
                    LOGGER.info(
                            "Technologies package for catalog {} already imported; skipping",
                            manifest.catalogVersion()
                    );
                    continue;
                }
                importTechnologyPackage(manifest.catalogVersion(), catalogPackage);
            } else if (ROADMAPS_PACKAGE_TYPE.equals(catalogPackage.type())) {
                if (isPackageAlreadyImported(manifest.catalogVersion(), ROADMAPS_PACKAGE_TYPE)) {
                    LOGGER.info(
                            "Roadmaps package for catalog {} already imported; skipping",
                            manifest.catalogVersion()
                    );
                    continue;
                }
                importRoadmapsPackage(manifest.catalogVersion(), catalogPackage);
            }
        }
    }

    private boolean isPackageAlreadyImported(String catalogVersion, String packageType) {
        return catalogImportRepository.existsByCatalogVersionAndPackageTypeAndStatus(
                catalogVersion,
                packageType,
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

        LOGGER.info("Imported {} technologies for catalog {}", upserted, catalogVersion);
        return upserted;
    }

    private int importRoadmapsPackage(String catalogVersion, CatalogManifestPackage catalogPackage) {
        List<String> roadmapPaths = listRoadmapFilePaths(catalogPackage.path());
        if (roadmapPaths.isEmpty()) {
            throw new CatalogImportException("No roadmap files found in package path: " + catalogPackage.path());
        }

        Set<String> importedSlugs = new HashSet<>();
        int upserted = 0;

        for (String roadmapPath : roadmapPaths) {
            CatalogRoadmapRecord record = readRoadmap(roadmapPath);
            schemaValidator.validateRoadmapRecord(roadmapPath, record);
            validateRoadmapTechnologyReference(record);
            upsertRoadmap(record);
            importedSlugs.add(record.technologySlug());
            upserted++;
        }

        markAbsentRoadmaps(importedSlugs);

        catalogImportRepository.save(new LearnCatalogImport(
                catalogVersion,
                Instant.now(),
                ROADMAPS_PACKAGE_TYPE,
                upserted,
                CatalogImportStatus.SUCCESS
        ));

        LOGGER.info("Imported {} roadmaps for catalog {}", upserted, catalogVersion);
        return upserted;
    }

    private void validateRoadmapTechnologyReference(CatalogRoadmapRecord record) {
        LearnTechnology technology = technologyRepository.findBySlug(record.technologySlug())
                .orElseThrow(() -> new CatalogImportException(
                        "Roadmap references unknown technology slug: " + record.technologySlug()
                ));
        if (!technology.isCatalogPresent()) {
            throw new CatalogImportException(
                    "Roadmap references technology not present in catalog: " + record.technologySlug()
            );
        }
    }

    private void upsertRoadmap(CatalogRoadmapRecord record) {
        LearnTechnology technology = technologyRepository.findBySlug(record.technologySlug())
                .orElseThrow();

        LearnRoadmap roadmap = roadmapRepository.findByTechnologySlug(record.technologySlug())
                .orElseGet(() -> new LearnRoadmap(technology));

        roadmap.applyCatalogData(
                record.version(),
                record.description(),
                record.source(),
                record.sourceUrl(),
                parseInstant(record.updatedAt())
        );

        List<LearnRoadmapStage> stages = buildStages(record);
        roadmap.replaceStages(stages);
        roadmapRepository.save(roadmap);
    }

    private List<LearnRoadmapStage> buildStages(CatalogRoadmapRecord record) {
        List<LearnRoadmapStage> stages = new ArrayList<>();
        for (CatalogRoadmapStageRecord stageRecord : record.stages()) {
            LearnRoadmapStage stage = LearnRoadmapStage.create();
            stage.setStageOrder(stageRecord.order());
            stage.setSlug(stageRecord.slug());
            stage.setTitle(stageRecord.title());
            stage.setDescription(stageRecord.description());
            stage.setEstimatedEffort(stageRecord.estimatedEffort());
            stage.setNotes(stageRecord.notes());

            List<LearnRoadmapStageResource> resources = new ArrayList<>();
            int learningOrder = 0;
            for (CatalogRoadmapResourceRecord resourceRecord : stageRecord.learningResources()) {
                resources.add(buildResource(resourceRecord, RoadmapResourceKind.LEARNING, learningOrder++));
            }
            if (stageRecord.practiceResources() != null) {
                int practiceOrder = 0;
                for (CatalogRoadmapResourceRecord resourceRecord : stageRecord.practiceResources()) {
                    resources.add(buildResource(resourceRecord, RoadmapResourceKind.PRACTICE, practiceOrder++));
                }
            }
            stage.replaceResources(resources);
            stages.add(stage);
        }
        return stages;
    }

    private LearnRoadmapStageResource buildResource(
            CatalogRoadmapResourceRecord record,
            RoadmapResourceKind kind,
            int order
    ) {
        LearnRoadmapStageResource resource = LearnRoadmapStageResource.create();
        resource.setResourceKind(kind);
        resource.setResourceOrder(order);
        resource.setSlug(record.slug());
        resource.setTitle(record.title());
        resource.setUrl(record.url());
        resource.setResourceType(record.type());
        resource.setProvider(record.provider());
        resource.setFreePaid(record.freePaid());
        resource.setVersion(record.version());
        resource.setSource(record.source());
        resource.setUpdatedAt(parseInstant(record.updatedAt()));
        return resource;
    }

    private void markAbsentRoadmaps(Set<String> importedSlugs) {
        Map<String, LearnRoadmap> existingBySlug = roadmapRepository.findByCatalogPresentTrue()
                .stream()
                .collect(Collectors.toMap(LearnRoadmap::getTechnologySlug, Function.identity()));

        for (Map.Entry<String, LearnRoadmap> entry : existingBySlug.entrySet()) {
            if (!importedSlugs.contains(entry.getKey())) {
                LearnRoadmap roadmap = entry.getValue();
                roadmap.markCatalogAbsent();
                roadmapRepository.save(roadmap);
            }
        }
    }

    private List<String> listRoadmapFilePaths(String directoryPath) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String normalizedPath = directoryPath.endsWith("/") ? directoryPath : directoryPath + "/";
            Resource[] resources = resolver.getResources("classpath:" + CATALOG_ROOT + normalizedPath + "*.json");
            return Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(StringUtils::hasText)
                    .sorted(Comparator.naturalOrder())
                    .map(filename -> CATALOG_ROOT + normalizedPath + filename)
                    .toList();
        } catch (IOException exception) {
            throw new CatalogImportException("Unable to list roadmap files in " + directoryPath, exception);
        }
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

    private CatalogRoadmapRecord readRoadmap(String path) {
        return readJson(path, CatalogRoadmapRecord.class);
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
