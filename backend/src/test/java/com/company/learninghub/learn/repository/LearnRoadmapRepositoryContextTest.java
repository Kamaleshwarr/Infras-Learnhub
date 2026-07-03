package com.company.learninghub.learn.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression: Spring Data must resolve roadmap repository derived queries at startup.
 * Invalid property paths (e.g. {@code technologySlug} instead of {@code technology.slug})
 * prevent the application context from loading.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class LearnRoadmapRepositoryContextTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_roadmap_repo")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.catalog.import.enabled", () -> "false");
    }

    @Autowired
    private LearnRoadmapRepository learnRoadmapRepository;

    @Test
    void applicationContextLoadsLearnRoadmapRepository() {
        assertThat(learnRoadmapRepository).isNotNull();
    }

    @Test
    void derivedExistsByTechnologySlugQueryExecutes() {
        assertThat(learnRoadmapRepository.existsByTechnology_SlugAndCatalogPresentTrue("java")).isFalse();
    }
}
