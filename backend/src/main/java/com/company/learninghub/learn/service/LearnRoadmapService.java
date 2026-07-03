package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.dto.RoadmapResponse;
import com.company.learninghub.learn.mapper.LearnRoadmapMapper;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageResourceRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.user.domain.RoleName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearnRoadmapService {

    private static final String ROADMAP_NOT_FOUND = "No roadmap is available for this technology.";

    private final LearnTechnologyRepository technologyRepository;
    private final LearnRoadmapRepository roadmapRepository;
    private final LearnRoadmapStageRepository stageRepository;
    private final LearnRoadmapStageResourceRepository resourceRepository;
    private final LearnRoadmapMapper roadmapMapper;

    public LearnRoadmapService(
            LearnTechnologyRepository technologyRepository,
            LearnRoadmapRepository roadmapRepository,
            LearnRoadmapStageRepository stageRepository,
            LearnRoadmapStageResourceRepository resourceRepository,
            LearnRoadmapMapper roadmapMapper
    ) {
        this.technologyRepository = technologyRepository;
        this.roadmapRepository = roadmapRepository;
        this.stageRepository = stageRepository;
        this.resourceRepository = resourceRepository;
        this.roadmapMapper = roadmapMapper;
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getRoadmapBySlug(String slug, AuthenticatedUser authenticatedUser) {
        LearnTechnology technology = technologyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(ROADMAP_NOT_FOUND));

        assertTechnologyVisible(technology, authenticatedUser);

        LearnRoadmap roadmap = roadmapRepository.findByTechnologySlug(slug)
                .filter(LearnRoadmap::isCatalogPresent)
                .orElseThrow(() -> new ResourceNotFoundException(ROADMAP_NOT_FOUND));

        return buildResponse(technology, roadmap);
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getRoadmapByTechnologyId(UUID technologyId, AuthenticatedUser authenticatedUser) {
        LearnTechnology technology = technologyRepository.findById(technologyId)
                .orElseThrow(() -> new ResourceNotFoundException(ROADMAP_NOT_FOUND));

        assertTechnologyVisible(technology, authenticatedUser);

        LearnRoadmap roadmap = roadmapRepository.findByTechnologySlug(technology.getSlug())
                .filter(LearnRoadmap::isCatalogPresent)
                .orElseThrow(() -> new ResourceNotFoundException(ROADMAP_NOT_FOUND));

        return buildResponse(technology, roadmap);
    }

    private RoadmapResponse buildResponse(LearnTechnology technology, LearnRoadmap roadmap) {
        List<LearnRoadmapStage> stages = stageRepository.findByRoadmapIdOrderByStageOrder(roadmap.getId());
        List<UUID> stageIds = stages.stream().map(LearnRoadmapStage::getId).toList();
        Map<UUID, List<LearnRoadmapStageResource>> resourcesByStageId = stageIds.isEmpty()
                ? Map.of()
                : resourceRepository.findByStageIdIn(stageIds).stream()
                        .collect(Collectors.groupingBy(resource -> resource.getStage().getId()));

        return roadmapMapper.toResponse(technology, roadmap, stages, resourcesByStageId);
    }

    private void assertTechnologyVisible(LearnTechnology technology, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser != null && authenticatedUser.getRoleNames().contains(RoleName.ADMIN)) {
            return;
        }
        if (!technology.isVisibleToEmployees()) {
            throw new ResourceNotFoundException(ROADMAP_NOT_FOUND);
        }
    }
}
