package com.company.learninghub.learn.integration;

import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.learn.service.LearnTechnologyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for F16-R startup failure: {@code lower(bytea) does not exist}.
 * Exercises criteria queries that apply LOWER() and sort by catalogFeatured.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class LearnCatalogStartupIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learninghub_startup")
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
    private LearnTechnologyService technologyService;

    @Autowired
    private LearnTechnologyRepository technologyRepository;

    @Test
    void applicationStartsWithCatalogImportEnabled() {
        assertThat(technologyRepository.count()).isGreaterThan(0);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeSearchBySlugExecutesWithoutLowerByteaError() {
        Page<TechnologyResponse> bySlug = technologyService.listEmployeeTechnologies(
                "spring",
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(bySlug.getContent()).isNotEmpty();
        assertThat(bySlug.getContent().getFirst().slug()).isEqualTo("spring-boot");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSortByFeaturedExecutesWithoutLowerByteaError() {
        Page<TechnologyResponse> adminFeaturedSort = technologyService.listAdminTechnologies(
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "featured"))
        );

        assertThat(adminFeaturedSort.getContent()).isNotEmpty();
        assertThat(adminFeaturedSort.getContent())
                .anyMatch(tech -> tech.status() == TechnologyStatus.PUBLISHED);
    }
}
