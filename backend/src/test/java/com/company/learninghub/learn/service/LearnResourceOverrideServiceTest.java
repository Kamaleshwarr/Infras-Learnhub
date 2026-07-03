package com.company.learninghub.learn.service;

import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnStageResourceOverride;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceKind;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.CreateResourceOverrideRequest;
import com.company.learninghub.learn.dto.ResourceOverrideResponse;
import com.company.learninghub.learn.dto.UpdateResourceOverrideRequest;
import com.company.learninghub.learn.mapper.LearnResourceOverrideMapper;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageResourceRepository;
import com.company.learninghub.learn.repository.LearnStageResourceOverrideRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearnResourceOverrideServiceTest {

    @Mock
    private LearnTechnologyRepository technologyRepository;
    @Mock
    private LearnRoadmapRepository roadmapRepository;
    @Mock
    private LearnRoadmapStageRepository stageRepository;
    @Mock
    private LearnRoadmapStageResourceRepository resourceRepository;
    @Mock
    private LearnStageResourceOverrideRepository overrideRepository;

    private LearnResourceOverrideService overrideService;
    private LearnTechnology technology;
    private LearnRoadmap roadmap;
    private LearnRoadmapStage stage;
    private LearnRoadmapStageResource catalogResource;

    @BeforeEach
    void setUp() {
        ResourceOverrideResolver resolver = new ResourceOverrideResolver();
        overrideService = new LearnResourceOverrideService(
                technologyRepository,
                roadmapRepository,
                stageRepository,
                resourceRepository,
                overrideRepository,
                resolver,
                new LearnResourceOverrideMapper(resolver)
        );

        technology = technology();
        roadmap = roadmapFor(technology);
        stage = stage(roadmap, "introduction");
        catalogResource = catalogResource(stage);
    }

    @Test
    void createCatalogOverrideReplacesUrl() {
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stage));
        when(resourceRepository.findByStageIdIn(List.of(stage.getId()))).thenReturn(List.of(catalogResource));
        when(overrideRepository.findByTechnologySlugAndStageSlugAndCatalogResourceSlug(
                "java", "introduction", "oracle-docs")).thenReturn(Optional.empty());
        when(overrideRepository.save(any())).thenAnswer(invocation -> {
            LearnStageResourceOverride saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        CreateResourceOverrideRequest request = new CreateResourceOverrideRequest(
                "introduction",
                "oracle-docs",
                "oracle-docs",
                "LEARNING",
                false,
                "https://internal.example.com/java",
                false,
                true,
                "Internal docs preferred",
                null,
                null,
                null,
                null,
                null
        );

        ResourceOverrideResponse response = overrideService.createOverride(technologyId, request);

        assertThat(response.overrideUrl()).isEqualTo("https://internal.example.com/java");
        assertThat(response.status()).isEqualTo("URL_OVERRIDE");
    }

    @Test
    void createCatalogOverrideRejectsDuplicate() {
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stage));
        when(resourceRepository.findByStageIdIn(List.of(stage.getId()))).thenReturn(List.of(catalogResource));
        when(overrideRepository.findByTechnologySlugAndStageSlugAndCatalogResourceSlug(
                "java", "introduction", "oracle-docs")).thenReturn(Optional.of(LearnStageResourceOverride.create()));

        CreateResourceOverrideRequest request = new CreateResourceOverrideRequest(
                "introduction",
                "oracle-docs",
                "oracle-docs",
                "LEARNING",
                false,
                "https://internal.example.com/java",
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> overrideService.createOverride(technologyId, request))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void createCatalogOverrideRejectsNonHttpsUrl() {
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stage));
        when(resourceRepository.findByStageIdIn(List.of(stage.getId()))).thenReturn(List.of(catalogResource));
        when(overrideRepository.findByTechnologySlugAndStageSlugAndCatalogResourceSlug(
                "java", "introduction", "oracle-docs")).thenReturn(Optional.empty());

        CreateResourceOverrideRequest request = new CreateResourceOverrideRequest(
                "introduction",
                "oracle-docs",
                "oracle-docs",
                "LEARNING",
                false,
                "http://insecure.example.com/java",
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> overrideService.createOverride(technologyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void restoreDefaultDeletesOverride() {
        UUID technologyId = technology.getId();
        LearnStageResourceOverride override = LearnStageResourceOverride.create();
        ReflectionTestUtils.setField(override, "id", UUID.randomUUID());
        override.setTechnologySlug("java");
        override.setStageSlug("introduction");
        override.setResourceSlug("oracle-docs");

        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(overrideRepository.findByTechnologySlugAndStageSlugAndResourceSlug(
                "java", "introduction", "oracle-docs")).thenReturn(Optional.of(override));

        overrideService.restoreDefault(technologyId, "introduction", "oracle-docs");

        verify(overrideRepository).delete(override);
    }

    @Test
    void updateOverrideCanDisableResource() {
        UUID technologyId = technology.getId();
        UUID overrideId = UUID.randomUUID();
        LearnStageResourceOverride override = LearnStageResourceOverride.create();
        ReflectionTestUtils.setField(override, "id", overrideId);
        override.setTechnologySlug("java");
        override.setCatalogResourceSlug("oracle-docs");
        override.setResourceKind(RoadmapResourceKind.LEARNING);
        override.setEnabled(true);

        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(overrideRepository.findById(overrideId)).thenReturn(Optional.of(override));
        when(overrideRepository.save(override)).thenReturn(override);

        ResourceOverrideResponse response = overrideService.updateOverride(
                technologyId,
                overrideId,
                new UpdateResourceOverrideRequest(true, null, null, null, null, null, null, null, null, null)
        );

        assertThat(response.disabled()).isTrue();
        assertThat(response.status()).isEqualTo("DISABLED");
    }

    @Test
    void restoreDefaultThrowsWhenMissing() {
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(overrideRepository.findByTechnologySlugAndStageSlugAndResourceSlug(
                "java", "introduction", "oracle-docs")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.restoreDefault(technologyId, "introduction", "oracle-docs"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createOrganizationOverridePersistsOrgResource() {
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stage));
        when(overrideRepository.findByTechnologySlugAndStageSlugAndResourceSlug(
                "java", "introduction", "internal-wiki")).thenReturn(Optional.empty());
        when(overrideRepository.save(any())).thenAnswer(invocation -> {
            LearnStageResourceOverride saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        CreateResourceOverrideRequest request = new CreateResourceOverrideRequest(
                "introduction",
                null,
                "internal-wiki",
                "LEARNING",
                false,
                "https://wiki.example.com/java",
                true,
                true,
                "Company wiki",
                "Internal Java Guide",
                "OTHER",
                "Internal",
                "FREE",
                1
        );

        ResourceOverrideResponse response = overrideService.createOverride(technologyId, request);

        ArgumentCaptor<LearnStageResourceOverride> captor = ArgumentCaptor.forClass(LearnStageResourceOverride.class);
        verify(overrideRepository).save(captor.capture());
        assertThat(captor.getValue().isOrganizationResource()).isTrue();
        assertThat(response.organizationResource()).isTrue();
        assertThat(response.status()).isEqualTo("ORGANIZATION");
    }

    private LearnTechnology technology() {
        User owner = new User("ADMIN001", "admin@learninghub.local", "Admin", "$2a$12$hash");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        LearnTechnology technology = new LearnTechnology(
                "java",
                "Java",
                "Java",
                "Enterprise language",
                TechnologyCategory.BACKEND,
                TechnologyDifficulty.INTERMEDIATE,
                TechnologyStatus.PUBLISHED,
                false,
                owner
        );
        ReflectionTestUtils.setField(technology, "id", UUID.randomUUID());
        return technology;
    }

    private LearnRoadmap roadmapFor(LearnTechnology technology) {
        LearnRoadmap roadmap = new LearnRoadmap(technology);
        ReflectionTestUtils.setField(roadmap, "id", UUID.randomUUID());
        roadmap.applyCatalogData(
                "1.0.0",
                "Java learning path",
                "platform-team",
                "https://roadmap.sh/java",
                Instant.parse("2026-07-03T00:00:00Z")
        );
        return roadmap;
    }

    private LearnRoadmapStage stage(LearnRoadmap roadmap, String slug) {
        LearnRoadmapStage stage = LearnRoadmapStage.create();
        ReflectionTestUtils.setField(stage, "id", UUID.randomUUID());
        stage.setRoadmap(roadmap);
        stage.setStageOrder(1);
        stage.setSlug(slug);
        stage.setTitle("Introduction");
        stage.setDescription("Description");
        stage.setEstimatedEffort("1 week");
        return stage;
    }

    private LearnRoadmapStageResource catalogResource(LearnRoadmapStage stage) {
        LearnRoadmapStageResource resource = LearnRoadmapStageResource.create();
        resource.setStage(stage);
        resource.setResourceKind(RoadmapResourceKind.LEARNING);
        resource.setResourceOrder(0);
        resource.setSlug("oracle-docs");
        resource.setTitle("Oracle Java Tutorial");
        resource.setUrl("https://docs.oracle.com/javase/tutorial/");
        resource.setResourceType(RoadmapResourceType.OFFICIAL_DOCUMENTATION);
        resource.setProvider("Oracle Docs");
        resource.setFreePaid(RoadmapResourceCost.FREE);
        return resource;
    }
}
