package com.company.learninghub.learn.service;

import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnStageResourceOverride;
import com.company.learninghub.learn.domain.RoadmapResourceKind;
import com.company.learninghub.learn.dto.RoadmapResourceResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResourceOverrideResolver {

    public List<RoadmapResourceResponse> resolveEffectiveResources(
            String stageSlug,
            List<LearnRoadmapStageResource> catalogResources,
            List<LearnStageResourceOverride> overrides,
            RoadmapResourceKind kind
    ) {
        Map<String, LearnStageResourceOverride> overrideByCatalogSlug = overrides.stream()
                .filter(LearnStageResourceOverride::isEnabled)
                .filter(override -> override.getStageSlug().equals(stageSlug))
                .filter(override -> override.getResourceKind() == kind)
                .filter(override -> override.getCatalogResourceSlug() != null)
                .collect(Collectors.toMap(
                        LearnStageResourceOverride::getCatalogResourceSlug,
                        override -> override,
                        (left, right) -> left,
                        HashMap::new
                ));

        List<ResolvedResource> resolved = new ArrayList<>();

        for (LearnRoadmapStageResource catalogResource : catalogResources) {
            if (catalogResource.getResourceKind() != kind) {
                continue;
            }
            LearnStageResourceOverride override = overrideByCatalogSlug.get(catalogResource.getSlug());
            if (override != null && override.isDisabled()) {
                continue;
            }
            String url = catalogResource.getUrl();
            if (override != null && override.getOverrideUrl() != null && !override.getOverrideUrl().isBlank()) {
                url = override.getOverrideUrl();
            }
            boolean preferred = override != null && override.isPreferred();
            int order = catalogResource.getResourceOrder();
            resolved.add(new ResolvedResource(
                    toResourceResponse(catalogResource, url),
                    preferred,
                    order
            ));
        }

        overrides.stream()
                .filter(LearnStageResourceOverride::isEnabled)
                .filter(override -> override.getStageSlug().equals(stageSlug))
                .filter(override -> override.getResourceKind() == kind)
                .filter(LearnStageResourceOverride::isOrganizationResource)
                .filter(override -> !override.isDisabled())
                .forEach(override -> resolved.add(new ResolvedResource(
                        toOrganizationResourceResponse(override),
                        override.isPreferred(),
                        override.getResourceOrder()
                )));

        return resolved.stream()
                .sorted(Comparator.comparing((ResolvedResource resource) -> !resource.preferred())
                        .thenComparingInt(resource -> resource.order())
                        .thenComparing(resource -> resource.response().title()))
                .map(ResolvedResource::response)
                .toList();
    }

    public String resolveOverrideStatus(
            LearnRoadmapStageResource catalogResource,
            LearnStageResourceOverride override
    ) {
        if (override == null || !override.isEnabled()) {
            return "DEFAULT";
        }
        if (override.isOrganizationResource()) {
            return "ORGANIZATION";
        }
        if (override.isDisabled()) {
            return "DISABLED";
        }
        if (override.isPreferred()) {
            return "PREFERRED";
        }
        if (override.getOverrideUrl() != null && !override.getOverrideUrl().isBlank()) {
            return "URL_OVERRIDE";
        }
        return "DEFAULT";
    }

    public RoadmapResourceResponse resolveEffectiveCatalogResource(
            LearnRoadmapStageResource catalogResource,
            LearnStageResourceOverride override
    ) {
        if (override == null || !override.isEnabled() || override.isDisabled()) {
            return null;
        }
        String url = catalogResource.getUrl();
        if (override.getOverrideUrl() != null && !override.getOverrideUrl().isBlank()) {
            url = override.getOverrideUrl();
        }
        return toResourceResponse(catalogResource, url);
    }

    public RoadmapResourceResponse toOrganizationResourceResponseForAdmin(LearnStageResourceOverride override) {
        return toOrganizationResourceResponse(override);
    }

    private RoadmapResourceResponse toOrganizationResourceResponse(LearnStageResourceOverride override) {
        return new RoadmapResourceResponse(
                override.getResourceSlug(),
                override.getTitle(),
                override.getOverrideUrl(),
                override.getResourceType() == null ? null : override.getResourceType().name(),
                override.getProvider(),
                override.getFreePaid() == null ? null : override.getFreePaid().name()
        );
    }

    private RoadmapResourceResponse toResourceResponse(LearnRoadmapStageResource resource, String url) {
        return new RoadmapResourceResponse(
                resource.getSlug(),
                resource.getTitle(),
                url,
                resource.getResourceType().name(),
                resource.getProvider(),
                resource.getFreePaid() == null ? null : resource.getFreePaid().name()
        );
    }

    private record ResolvedResource(RoadmapResourceResponse response, boolean preferred, int order) {
    }
}
