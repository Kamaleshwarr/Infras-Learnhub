package com.company.learninghub.learn.integration;

import com.company.learninghub.learn.catalog.CatalogImportService;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.repository.LearnCatalogImportRepository;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Regression for F17 startup failure: {@code LazyInitializationException} on
 * {@code LearnRoadmap.stages} during roadmap catalog reimport.
 *
 * <p>Reimport loads an existing {@link LearnRoadmap} whose {@code stages} collection is lazy.
 * {@link com.company.learninghub.learn.domain.LearnRoadmap#replaceStages} must run inside an
 * active persistence context so Hibernate can initialize and replace the collection.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class CatalogRoadmapReimportIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_roadmap_reimport")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.catalog.import.enabled", () -> "true");
    }

    @Autowired
    private CatalogImportService catalogImportService;

    @Autowired
    private LearnRoadmapRepository roadmapRepository;

    @Autowired
    private LearnRoadmapStageRepository stageRepository;

    @Autowired
    private LearnCatalogImportRepository catalogImportRepository;

    @Test
    void startupImportsRoadmaps() {
        assertThat(roadmapRepository.count()).isEqualTo(5);
    }

    @Test
    void reimportExistingRoadmapsReplacesStagesWithoutLazyInitializationFailure() {
        LearnRoadmap javaRoadmap = roadmapRepository.findByTechnologySlug("java").orElseThrow();
        assertThat(stageRepository.findByRoadmapIdOrderByStageOrder(javaRoadmap.getId())).isNotEmpty();

        catalogImportRepository.findByCatalogVersionOrderByImportedAtDesc("1.1.1").stream()
                .filter(record -> "roadmaps".equals(record.getPackageType()))
                .forEach(catalogImportRepository::delete);

        assertThatCode(() -> catalogImportService.importCatalog()).doesNotThrowAnyException();

        LearnRoadmap reimported = roadmapRepository.findByTechnologySlug("java").orElseThrow();
        assertThat(stageRepository.findByRoadmapIdOrderByStageOrder(reimported.getId())).hasSize(7);
    }
}
