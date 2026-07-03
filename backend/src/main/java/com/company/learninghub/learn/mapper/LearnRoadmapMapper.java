package com.company.learninghub.learn.mapper;

import com.company.learninghub.learn.domain.LearnRoadmap;
import com.company.learninghub.learn.domain.LearnRoadmapStage;
import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnStageResourceOverride;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.RoadmapResourceKind;
import com.company.learninghub.learn.dto.RoadmapResourceResponse;
import com.company.learninghub.learn.dto.RoadmapResponse;
import com.company.learninghub.learn.dto.RoadmapStageResponse;
import com.company.learninghub.learn.service.ResourceOverrideResolver;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LearnRoadmapMapper {

    private final ResourceOverrideResolver overrideResolver;

    public LearnRoadmapMapper(ResourceOverrideResolver overrideResolver) {
        this.overrideResolver = overrideResolver;
    }

    public RoadmapResponse toResponse(
            LearnTechnology technology,
            LearnRoadmap roadmap,
            List<LearnRoadmapStage> stages,
            Map<UUID, List<LearnRoadmapStageResource>> resourcesByStageId,
            List<LearnStageResourceOverride> overrides
    ) {
        List<LearnRoadmapStage> orderedStages = stages.stream()
                .sorted(Comparator.comparingInt(LearnRoadmapStage::getStageOrder))
                .toList();

        List<RoadmapStageResponse> stageResponses = orderedStages.stream()
                .map(stage -> toStageResponse(
                        stage,
                        resourcesByStageId.getOrDefault(stage.getId(), List.of()),
                        overrides))
                .toList();

        int recommendedStageOrder = orderedStages.isEmpty() ? 0 : orderedStages.getFirst().getStageOrder();
        int nextStageOrder = orderedStages.size() > 1
                ? orderedStages.get(1).getStageOrder()
                : recommendedStageOrder;

        return new RoadmapResponse(
                technology.getId().toString(),
                technology.getSlug(),
                technology.getName(),
                roadmap.getVersion(),
                roadmap.getDescription(),
                roadmap.getSource(),
                roadmap.getSourceUrl(),
                formatInstant(roadmap.getCatalogUpdatedAt()),
                orderedStages.size(),
                summarizeEffort(orderedStages),
                recommendedStageOrder,
                nextStageOrder,
                stageResponses
        );
    }

    private RoadmapStageResponse toStageResponse(
            LearnRoadmapStage stage,
            List<LearnRoadmapStageResource> resources,
            List<LearnStageResourceOverride> overrides
    ) {
        List<RoadmapResourceResponse> learning = overrideResolver.resolveEffectiveResources(
                stage.getSlug(),
                resources,
                overrides,
                RoadmapResourceKind.LEARNING
        );
        List<RoadmapResourceResponse> practice = overrideResolver.resolveEffectiveResources(
                stage.getSlug(),
                resources,
                overrides,
                RoadmapResourceKind.PRACTICE
        );

        return new RoadmapStageResponse(
                stage.getId().toString(),
                stage.getStageOrder(),
                stage.getSlug(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getEstimatedEffort(),
                stage.getNotes(),
                learning,
                practice
        );
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private String summarizeEffort(List<LearnRoadmapStage> stages) {
        if (stages.isEmpty()) {
            return "";
        }
        List<String> efforts = new ArrayList<>();
        for (LearnRoadmapStage stage : stages) {
            efforts.add(stage.getEstimatedEffort());
        }
        return String.join(" + ", efforts);
    }
}
