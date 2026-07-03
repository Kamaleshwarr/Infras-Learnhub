package com.company.learninghub.learn.mapper;

import com.company.learninghub.learn.domain.LearnStageResourceOverride;
import com.company.learninghub.learn.dto.ResourceOverrideResponse;
import com.company.learninghub.learn.service.ResourceOverrideResolver;
import org.springframework.stereotype.Component;

@Component
public class LearnResourceOverrideMapper {

    private final ResourceOverrideResolver resolver;

    public LearnResourceOverrideMapper(ResourceOverrideResolver resolver) {
        this.resolver = resolver;
    }

    public ResourceOverrideResponse toResponse(LearnStageResourceOverride override) {
        return new ResourceOverrideResponse(
                override.getId().toString(),
                override.getTechnologySlug(),
                override.getStageSlug(),
                override.getResourceSlug(),
                override.getCatalogResourceSlug(),
                override.getResourceKind().name(),
                override.isDisabled(),
                override.getOverrideUrl(),
                override.isPreferred(),
                override.isEnabled(),
                override.getReason(),
                override.getTitle(),
                override.getResourceType() == null ? null : override.getResourceType().name(),
                override.getProvider(),
                override.getFreePaid() == null ? null : override.getFreePaid().name(),
                override.getResourceOrder(),
                override.isOrganizationResource(),
                mapStatus(override)
        );
    }

    private String mapStatus(LearnStageResourceOverride override) {
        if (!override.isEnabled()) {
            return "INACTIVE";
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
}
