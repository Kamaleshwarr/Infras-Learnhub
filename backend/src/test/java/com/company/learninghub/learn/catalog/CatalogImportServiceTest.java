package com.company.learninghub.learn.catalog;

import com.company.learninghub.learn.catalog.dto.CatalogTechnologyPackage;
import com.company.learninghub.learn.catalog.dto.CatalogTechnologyRecord;
import com.company.learninghub.learn.domain.CatalogImportStatus;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.repository.LearnCatalogImportRepository;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogImportServiceTest {

    @Mock
    private CatalogSchemaValidator schemaValidator;

    @Mock
    private LearnTechnologyRepository technologyRepository;

    @Mock
    private LearnRoadmapRepository roadmapRepository;

    @Mock
    private LearnCatalogImportRepository catalogImportRepository;

    @Mock
    private com.company.learninghub.user.repository.UserRepository userRepository;

    private CatalogImportService catalogImportService;

    @BeforeEach
    void setUp() {
        CatalogImportProperties properties = new CatalogImportProperties();
        properties.setEnabled(true);
        properties.setFailFast(true);

        catalogImportService = new CatalogImportService(
                properties,
                schemaValidator,
                technologyRepository,
                roadmapRepository,
                catalogImportRepository,
                userRepository,
                new ObjectMapper().findAndRegisterModules()
        );
    }

    private void stubImportOwner() {
        User admin = new User("ADMIN001", "admin@learninghub.local", "Admin", "$2a$12$hash");
        ReflectionTestUtils.setField(admin, "id", UUID.randomUUID());
        admin.assignRole(new Role(RoleName.ADMIN));
        when(userRepository.findByEmailIgnoreCase("admin@learninghub.local")).thenReturn(Optional.of(admin));
    }

    @Test
    void importCatalogSkipsWhenVersionAlreadyImported() {
        when(catalogImportRepository.existsByCatalogVersionAndPackageTypeAndStatus(
                "1.1.0",
                "technologies",
                CatalogImportStatus.SUCCESS
        )).thenReturn(true);
        when(catalogImportRepository.existsByCatalogVersionAndPackageTypeAndStatus(
                "1.1.0",
                "roadmaps",
                CatalogImportStatus.SUCCESS
        )).thenReturn(true);

        catalogImportService.importCatalog();

        verify(technologyRepository, never()).save(any());
        verify(catalogImportRepository, never()).save(any());
    }

    @Test
    void importCatalogPreservesOrganizationOverridesOnReimport() {
        stubImportOwner();
        when(catalogImportRepository.existsByCatalogVersionAndPackageTypeAndStatus(
                "1.1.0",
                "technologies",
                CatalogImportStatus.SUCCESS
        )).thenReturn(false);
        when(catalogImportRepository.existsByCatalogVersionAndPackageTypeAndStatus(
                "1.1.0",
                "roadmaps",
                CatalogImportStatus.SUCCESS
        )).thenReturn(true);
        when(technologyRepository.findByCatalogPresentTrue()).thenReturn(List.of());

        LearnTechnology existing = new LearnTechnology(
                "spring-boot",
                "Old Name",
                "Old",
                "Old description",
                TechnologyCategory.BACKEND,
                TechnologyDifficulty.BEGINNER,
                TechnologyStatus.PUBLISHED,
                false,
                user()
        );
        existing.setFeaturedOverride(true);
        existing.setOrgNotes("Org context");
        ReflectionTestUtils.setField(existing, "id", UUID.randomUUID());

        when(technologyRepository.findBySlug("spring-boot")).thenReturn(Optional.of(existing));
        when(technologyRepository.save(any(LearnTechnology.class))).thenAnswer(invocation -> invocation.getArgument(0));

        catalogImportService.importCatalog();

        ArgumentCaptor<LearnTechnology> captor = ArgumentCaptor.forClass(LearnTechnology.class);
        verify(technologyRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        LearnTechnology saved = captor.getAllValues().stream()
                .filter(technology -> "spring-boot".equals(technology.getSlug()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getName()).isEqualTo("Spring Boot");
        assertThat(saved.getStatus()).isEqualTo(TechnologyStatus.PUBLISHED);
        assertThat(saved.getFeaturedOverride()).isTrue();
        assertThat(saved.getOrgNotes()).isEqualTo("Org context");
        assertThat(saved.isFeatured()).isTrue();
    }

    @Test
    void schemaValidatorRejectsDuplicateSlugs() {
        CatalogSchemaValidator validator = new CatalogSchemaValidator(new ObjectMapper().findAndRegisterModules());
        CatalogTechnologyPackage technologyPackage = new CatalogTechnologyPackage(
                "1.0.0",
                "technologies",
                "2026-07-02T00:00:00Z",
                List.of(
                        record("spring-boot"),
                        record("spring-boot")
                )
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> validator.validateTechnologyPackage("catalog/technologies/wave-1.json", technologyPackage)
        ).isInstanceOf(CatalogImportException.class)
                .hasMessageContaining("Duplicate technology slug");
    }

    private CatalogTechnologyRecord record(String slug) {
        return new CatalogTechnologyRecord(
                slug,
                "1.0.0",
                "Spring Boot",
                "Spring Boot",
                TechnologyCategory.BACKEND,
                "Description",
                TechnologyDifficulty.INTERMEDIATE,
                "4-6 weeks",
                "https://spring.io/projects/spring-boot",
                "https://docs.spring.io/spring-boot/docs/current/reference/html/",
                List.of("java"),
                false,
                "platform-team",
                null,
                "2026-07-02T00:00:00Z"
        );
    }

    private User user() {
        User user = new User("ADMIN001", "admin@learninghub.local", "Admin", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.ADMIN));
        return user;
    }
}
