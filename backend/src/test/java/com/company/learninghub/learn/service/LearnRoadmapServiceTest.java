package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceKind;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.RoadmapResponse;
import com.company.learninghub.learn.mapper.LearnRoadmapMapper;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageResourceRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearnRoadmapServiceTest {

    @Mock
    private LearnTechnologyRepository technologyRepository;

    @Mock
    private LearnRoadmapRepository roadmapRepository;

    @Mock
    private LearnRoadmapStageRepository stageRepository;

    @Mock
    private LearnRoadmapStageResourceRepository resourceRepository;

    @Mock
    private LearnResourceOverrideService overrideService;

    private LearnRoadmapService roadmapService;

    @BeforeEach
    void setUp() {
        roadmapService = new LearnRoadmapService(
                technologyRepository,
                roadmapRepository,
                stageRepository,
                resourceRepository,
                new LearnRoadmapMapper(new ResourceOverrideResolver()),
                overrideService
        );
    }

    @Test
    void getRoadmapBySlugReturnsOrderedStagesForPublishedTechnology() {
        LearnTechnology technology = publishedTechnology();
        LearnRoadmap roadmap = roadmapFor(technology);
        LearnRoadmapStage stage = stage(roadmap, 1, "introduction");
        LearnRoadmapStageResource resource = learningResource(stage);

        when(technologyRepository.findBySlug("java")).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stage));
        when(resourceRepository.findByStageIdIn(List.of(stage.getId()))).thenReturn(List.of(resource));
        when(overrideService.listEnabledOverrides("java")).thenReturn(List.of());

        RoadmapResponse response = roadmapService.getRoadmapBySlug("java", employeePrincipal());

        assertThat(response.technologySlug()).isEqualTo("java");
        assertThat(response.stageCount()).isEqualTo(1);
        assertThat(response.stages()).hasSize(1);
        assertThat(response.stages().getFirst().learningResources()).hasSize(1);
    }

    @Test
    void getRoadmapBySlugHidesUnpublishedTechnologyFromEmployees() {
        LearnTechnology technology = publishedTechnology();
        technology.setStatus(TechnologyStatus.HIDDEN);

        when(technologyRepository.findBySlug("java")).thenReturn(Optional.of(technology));

        assertThatThrownBy(() -> roadmapService.getRoadmapBySlug("java", employeePrincipal()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRoadmapBySlugAllowsAdminPreviewForHiddenTechnology() {
        LearnTechnology technology = publishedTechnology();
        technology.setStatus(TechnologyStatus.HIDDEN);
        LearnRoadmap roadmap = roadmapFor(technology);
        LearnRoadmapStage stage = stage(roadmap, 1, "introduction");

        when(technologyRepository.findBySlug("java")).thenReturn(Optional.of(technology));
        when(roadmapRepository.findByTechnologySlug("java")).thenReturn(Optional.of(roadmap));
        when(stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId())).thenReturn(List.of(stage));
        when(resourceRepository.findByStageIdIn(List.of(stage.getId()))).thenReturn(List.of());
        when(overrideService.listEnabledOverrides("java")).thenReturn(List.of());

        RoadmapResponse response = roadmapService.getRoadmapBySlug("java", adminPrincipal());

        assertThat(response.technologySlug()).isEqualTo("java");
    }

    private LearnTechnology publishedTechnology() {
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

    private LearnRoadmapStage stage(LearnRoadmap roadmap, int order, String slug) {
        LearnRoadmapStage stage = LearnRoadmapStage.create();
        ReflectionTestUtils.setField(stage, "id", UUID.randomUUID());
        stage.setRoadmap(roadmap);
        stage.setStageOrder(order);
        stage.setSlug(slug);
        stage.setTitle("Stage " + order);
        stage.setDescription("Description");
        stage.setEstimatedEffort("1 week");
        return stage;
    }

    private LearnRoadmapStageResource learningResource(LearnRoadmapStage stage) {
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

    private AuthenticatedUser employeePrincipal() {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return AuthenticatedUser.from(user);
    }

    private AuthenticatedUser adminPrincipal() {
        User user = new User("ADMIN001", "admin@learninghub.local", "Admin", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.ADMIN));
        return AuthenticatedUser.from(user);
    }
}
