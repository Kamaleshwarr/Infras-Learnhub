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
import com.company.learninghub.learn.dto.CreateResourceOverrideRequest;
import com.company.learninghub.learn.dto.ManagedResourceResponse;
import com.company.learninghub.learn.dto.ResourceOverrideResponse;
import com.company.learninghub.learn.dto.RoadmapResourceResponse;
import com.company.learninghub.learn.dto.StageResourceAdminResponse;
import com.company.learninghub.learn.dto.UpdateResourceOverrideRequest;
import com.company.learninghub.learn.mapper.LearnResourceOverrideMapper;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageResourceRepository;
import com.company.learninghub.learn.repository.LearnStageResourceOverrideRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LearnResourceOverrideService {

    private static final String TECHNOLOGY_NOT_FOUND = "Technology not found.";
    private static final String STAGE_NOT_FOUND = "Stage not found.";
    private static final String RESOURCE_NOT_FOUND = "Catalog resource not found.";
    private static final String OVERRIDE_NOT_FOUND = "Resource override not found.";
    private static final String DUPLICATE_OVERRIDE = "An override already exists for this resource.";

    private final LearnTechnologyRepository technologyRepository;
    private final LearnRoadmapRepository roadmapRepository;
    private final LearnRoadmapStageRepository stageRepository;
    private final LearnRoadmapStageResourceRepository resourceRepository;
    private final LearnStageResourceOverrideRepository overrideRepository;
    private final ResourceOverrideResolver overrideResolver;
    private final LearnResourceOverrideMapper overrideMapper;

    public LearnResourceOverrideService(
            LearnTechnologyRepository technologyRepository,
            LearnRoadmapRepository roadmapRepository,
            LearnRoadmapStageRepository stageRepository,
            LearnRoadmapStageResourceRepository resourceRepository,
            LearnStageResourceOverrideRepository overrideRepository,
            ResourceOverrideResolver overrideResolver,
            LearnResourceOverrideMapper overrideMapper
    ) {
        this.technologyRepository = technologyRepository;
        this.roadmapRepository = roadmapRepository;
        this.stageRepository = stageRepository;
        this.resourceRepository = resourceRepository;
        this.overrideRepository = overrideRepository;
        this.overrideResolver = overrideResolver;
        this.overrideMapper = overrideMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public StageResourceAdminResponse getStageResources(UUID technologyId, String stageSlug) {
        LearnTechnology technology = requireTechnology(technologyId);
        LearnRoadmapStage stage = requireStage(technology, stageSlug);
        List<LearnRoadmapStageResource> catalogResources = resourceRepository.findByStageIdIn(List.of(stage.getId()));
        List<LearnStageResourceOverride> overrides = overrideRepository.findByTechnologySlugAndStageSlug(
                technology.getSlug(),
                stageSlug
        );
        Map<String, LearnStageResourceOverride> overrideByCatalogSlug = overrides.stream()
                .filter(override -> override.getCatalogResourceSlug() != null)
                .collect(Collectors.toMap(
                        LearnStageResourceOverride::getCatalogResourceSlug,
                        Function.identity(),
                        (left, right) -> left
                ));

        List<ManagedResourceResponse> managed = catalogResources.stream()
                .map(catalogResource -> toManagedResource(catalogResource, overrideByCatalogSlug.get(catalogResource.getSlug())))
                .toList();

        List<ManagedResourceResponse> organizationResources = overrides.stream()
                .filter(LearnStageResourceOverride::isOrganizationResource)
                .map(override -> new ManagedResourceResponse(
                        null,
                        overrideResolver.toOrganizationResourceResponseForAdmin(override),
                        overrideMapper.toResponse(override),
                        overrideMapper.toResponse(override).status()
                ))
                .toList();

        List<ManagedResourceResponse> allResources = new java.util.ArrayList<>(managed);
        allResources.addAll(organizationResources);

        return new StageResourceAdminResponse(stage.getSlug(), stage.getTitle(), stage.getStageOrder(), allResources);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceOverrideResponse createOverride(UUID technologyId, CreateResourceOverrideRequest request) {
        LearnTechnology technology = requireTechnology(technologyId);
        LearnRoadmapStage stage = requireStage(technology, request.stageSlug());
        RoadmapResourceKind kind = parseKind(request.resourceKind());

        boolean organizationResource = request.catalogResourceSlug() == null || request.catalogResourceSlug().isBlank();
        if (organizationResource) {
            return createOrganizationOverride(technology, stage, kind, request);
        }
        return createCatalogOverride(technology, stage, kind, request);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceOverrideResponse updateOverride(UUID technologyId, UUID overrideId, UpdateResourceOverrideRequest request) {
        LearnTechnology technology = requireTechnology(technologyId);
        LearnStageResourceOverride override = overrideRepository.findById(overrideId)
                .filter(existing -> existing.getTechnologySlug().equals(technology.getSlug()))
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND));

        if (request.disabled() != null) {
            override.setDisabled(request.disabled());
        }
        if (request.overrideUrl() != null) {
            validateHttpsUrl(request.overrideUrl());
            override.setOverrideUrl(request.overrideUrl());
        }
        if (request.preferred() != null) {
            override.setPreferred(request.preferred());
        }
        if (request.enabled() != null) {
            override.setEnabled(request.enabled());
        }
        if (request.reason() != null) {
            override.setReason(trimToNull(request.reason()));
        }
        if (override.isOrganizationResource()) {
            if (request.title() != null) {
                validateOrgTitle(request.title());
                override.setTitle(request.title().trim());
            }
            if (request.resourceType() != null) {
                override.setResourceType(RoadmapResourceType.valueOf(request.resourceType()));
            }
            if (request.provider() != null) {
                override.setProvider(trimToNull(request.provider()));
            }
            if (request.freePaid() != null) {
                override.setFreePaid(parseCost(request.freePaid()));
            }
            if (request.resourceOrder() != null) {
                override.setResourceOrder(request.resourceOrder());
            }
        }

        return overrideMapper.toResponse(overrideRepository.save(override));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOverride(UUID technologyId, UUID overrideId) {
        LearnTechnology technology = requireTechnology(technologyId);
        LearnStageResourceOverride override = overrideRepository.findById(overrideId)
                .filter(existing -> existing.getTechnologySlug().equals(technology.getSlug()))
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND));
        overrideRepository.delete(override);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreDefault(UUID technologyId, String stageSlug, String resourceSlug) {
        LearnTechnology technology = requireTechnology(technologyId);
        LearnStageResourceOverride override = overrideRepository
                .findByTechnologySlugAndStageSlugAndResourceSlug(technology.getSlug(), stageSlug, resourceSlug)
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND));
        overrideRepository.delete(override);
    }

    @Transactional(readOnly = true)
    public List<LearnStageResourceOverride> listEnabledOverrides(String technologySlug) {
        return overrideRepository.findByTechnologySlug(technologySlug).stream()
                .filter(LearnStageResourceOverride::isEnabled)
                .toList();
    }

    private ResourceOverrideResponse createCatalogOverride(
            LearnTechnology technology,
            LearnRoadmapStage stage,
            RoadmapResourceKind kind,
            CreateResourceOverrideRequest request
    ) {
        LearnRoadmapStageResource catalogResource = resourceRepository.findByStageIdIn(List.of(stage.getId())).stream()
                .filter(resource -> resource.getSlug().equals(request.catalogResourceSlug()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));

        if (overrideRepository.findByTechnologySlugAndStageSlugAndCatalogResourceSlug(
                technology.getSlug(), stage.getSlug(), catalogResource.getSlug()).isPresent()) {
            throw new BusinessConflictException(DUPLICATE_OVERRIDE);
        }

        LearnStageResourceOverride override = LearnStageResourceOverride.create();
        override.setTechnologySlug(technology.getSlug());
        override.setStageSlug(stage.getSlug());
        override.setResourceSlug(catalogResource.getSlug());
        override.setCatalogResourceSlug(catalogResource.getSlug());
        override.setResourceKind(catalogResource.getResourceKind());
        override.setDisabled(Boolean.TRUE.equals(request.disabled()));
        if (request.overrideUrl() != null && !request.overrideUrl().isBlank()) {
            validateHttpsUrl(request.overrideUrl());
            override.setOverrideUrl(request.overrideUrl().trim());
        }
        override.setPreferred(Boolean.TRUE.equals(request.preferred()));
        override.setEnabled(request.enabled() == null || request.enabled());
        override.setReason(trimToNull(request.reason()));
        override.setResourceOrder(catalogResource.getResourceOrder());

        return overrideMapper.toResponse(overrideRepository.save(override));
    }

    private ResourceOverrideResponse createOrganizationOverride(
            LearnTechnology technology,
            LearnRoadmapStage stage,
            RoadmapResourceKind kind,
            CreateResourceOverrideRequest request
    ) {
        validateOrgTitle(request.title());
        validateHttpsUrl(request.overrideUrl());
        if (overrideRepository.findByTechnologySlugAndStageSlugAndResourceSlug(
                technology.getSlug(), stage.getSlug(), request.resourceSlug()).isPresent()) {
            throw new BusinessConflictException(DUPLICATE_OVERRIDE);
        }

        LearnStageResourceOverride override = LearnStageResourceOverride.create();
        override.setTechnologySlug(technology.getSlug());
        override.setStageSlug(stage.getSlug());
        override.setResourceSlug(request.resourceSlug().trim());
        override.setCatalogResourceSlug(null);
        override.setResourceKind(kind);
        override.setDisabled(false);
        override.setOverrideUrl(request.overrideUrl().trim());
        override.setPreferred(Boolean.TRUE.equals(request.preferred()));
        override.setEnabled(request.enabled() == null || request.enabled());
        override.setReason(trimToNull(request.reason()));
        override.setTitle(request.title().trim());
        override.setResourceType(RoadmapResourceType.valueOf(request.resourceType()));
        override.setProvider(trimToNull(request.provider()));
        override.setFreePaid(request.freePaid() == null ? null : parseCost(request.freePaid()));
        override.setResourceOrder(request.resourceOrder() == null ? 0 : request.resourceOrder());

        return overrideMapper.toResponse(overrideRepository.save(override));
    }

    private ManagedResourceResponse toManagedResource(
            LearnRoadmapStageResource catalogResource,
            LearnStageResourceOverride override
    ) {
        RoadmapResourceResponse catalog = new RoadmapResourceResponse(
                catalogResource.getSlug(),
                catalogResource.getTitle(),
                catalogResource.getUrl(),
                catalogResource.getResourceType().name(),
                catalogResource.getProvider(),
                catalogResource.getFreePaid() == null ? null : catalogResource.getFreePaid().name()
        );
        ResourceOverrideResponse overrideResponse = override == null ? null : overrideMapper.toResponse(override);
        RoadmapResourceResponse effective;
        if (override != null && override.isEnabled() && override.isDisabled()) {
            effective = null;
        } else {
            effective = overrideResolver.resolveEffectiveCatalogResource(catalogResource, override);
            if (effective == null) {
                effective = catalog;
            }
        }
        String status = overrideResolver.resolveOverrideStatus(catalogResource, override);
        return new ManagedResourceResponse(catalog, effective, overrideResponse, status);
    }

    private LearnTechnology requireTechnology(UUID technologyId) {
        return technologyRepository.findById(technologyId)
                .orElseThrow(() -> new ResourceNotFoundException(TECHNOLOGY_NOT_FOUND));
    }

    private LearnRoadmapStage requireStage(LearnTechnology technology, String stageSlug) {
        LearnRoadmap roadmap = roadmapRepository.findByTechnologySlug(technology.getSlug())
                .filter(LearnRoadmap::isCatalogPresent)
                .orElseThrow(() -> new ResourceNotFoundException(STAGE_NOT_FOUND));
        return stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId()).stream()
                .filter(stage -> stage.getSlug().equals(stageSlug))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(STAGE_NOT_FOUND));
    }

    private RoadmapResourceKind parseKind(String kind) {
        return RoadmapResourceKind.valueOf(kind);
    }

    private RoadmapResourceCost parseCost(String cost) {
        return RoadmapResourceCost.valueOf(cost);
    }

    private void validateHttpsUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is required.");
        }
        URI uri = URI.create(url.trim());
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Override URLs must use HTTPS.");
        }
    }

    private void validateOrgTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required for organization resources.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
