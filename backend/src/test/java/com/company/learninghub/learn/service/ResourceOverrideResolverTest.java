package com.company.learninghub.learn.service;

import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import com.company.learninghub.learn.domain.LearnStageResourceOverride;
import com.company.learninghub.learn.domain.RoadmapResourceCost;
import com.company.learninghub.learn.domain.RoadmapResourceKind;
import com.company.learninghub.learn.domain.RoadmapResourceType;
import com.company.learninghub.learn.dto.RoadmapResourceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceOverrideResolverTest {

    private ResourceOverrideResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ResourceOverrideResolver();
    }

    @Test
    void resolveEffectiveResourcesReturnsCatalogWhenNoOverrides() {
        LearnRoadmapStageResource catalog = catalogResource("oracle-docs", "https://docs.oracle.com/javase/tutorial/");

        List<RoadmapResourceResponse> effective = resolver.resolveEffectiveResources(
                "introduction",
                List.of(catalog),
                List.of(),
                RoadmapResourceKind.LEARNING
        );

        assertThat(effective).hasSize(1);
        assertThat(effective.getFirst().url()).isEqualTo("https://docs.oracle.com/javase/tutorial/");
    }

    @Test
    void resolveEffectiveResourcesUsesOverrideUrl() {
        LearnRoadmapStageResource catalog = catalogResource("oracle-docs", "https://docs.oracle.com/javase/tutorial/");
        LearnStageResourceOverride override = catalogOverride("oracle-docs", false, "https://internal.example.com/java", false);

        List<RoadmapResourceResponse> effective = resolver.resolveEffectiveResources(
                "introduction",
                List.of(catalog),
                List.of(override),
                RoadmapResourceKind.LEARNING
        );

        assertThat(effective).hasSize(1);
        assertThat(effective.getFirst().url()).isEqualTo("https://internal.example.com/java");
        assertThat(effective.getFirst().title()).isEqualTo("Oracle Java Tutorial");
    }

    @Test
    void resolveEffectiveResourcesHidesDisabledCatalogResource() {
        LearnRoadmapStageResource catalog = catalogResource("oracle-docs", "https://docs.oracle.com/javase/tutorial/");
        LearnStageResourceOverride override = catalogOverride("oracle-docs", true, null, false);

        List<RoadmapResourceResponse> effective = resolver.resolveEffectiveResources(
                "introduction",
                List.of(catalog),
                List.of(override),
                RoadmapResourceKind.LEARNING
        );

        assertThat(effective).isEmpty();
    }

    @Test
    void resolveEffectiveResourcesAddsOrganizationResource() {
        LearnStageResourceOverride orgResource = organizationOverride(
                "internal-wiki",
                "Internal Java Guide",
                "https://wiki.example.com/java"
        );

        List<RoadmapResourceResponse> effective = resolver.resolveEffectiveResources(
                "introduction",
                List.of(),
                List.of(orgResource),
                RoadmapResourceKind.LEARNING
        );

        assertThat(effective).hasSize(1);
        assertThat(effective.getFirst().slug()).isEqualTo("internal-wiki");
        assertThat(effective.getFirst().title()).isEqualTo("Internal Java Guide");
    }

    @Test
    void resolveEffectiveResourcesSortsPreferredFirst() {
        LearnRoadmapStageResource first = catalogResource("first", "https://example.com/first");
        first.setResourceOrder(1);
        LearnRoadmapStageResource second = catalogResource("second", "https://example.com/second");
        second.setResourceOrder(2);
        LearnStageResourceOverride preferred = catalogOverride("second", false, null, true);

        List<RoadmapResourceResponse> effective = resolver.resolveEffectiveResources(
                "introduction",
                List.of(first, second),
                List.of(preferred),
                RoadmapResourceKind.LEARNING
        );

        assertThat(effective).hasSize(2);
        assertThat(effective.getFirst().slug()).isEqualTo("second");
    }

    @Test
    void resolveOverrideStatusReturnsDisabledForDisabledOverride() {
        LearnRoadmapStageResource catalog = catalogResource("oracle-docs", "https://docs.oracle.com/javase/tutorial/");
        LearnStageResourceOverride override = catalogOverride("oracle-docs", true, null, false);

        assertThat(resolver.resolveOverrideStatus(catalog, override)).isEqualTo("DISABLED");
    }

    private LearnRoadmapStageResource catalogResource(String slug, String url) {
        LearnRoadmapStageResource resource = LearnRoadmapStageResource.create();
        resource.setResourceKind(RoadmapResourceKind.LEARNING);
        resource.setResourceOrder(0);
        resource.setSlug(slug);
        resource.setTitle("Oracle Java Tutorial");
        resource.setUrl(url);
        resource.setResourceType(RoadmapResourceType.OFFICIAL_DOCUMENTATION);
        resource.setProvider("Oracle Docs");
        resource.setFreePaid(RoadmapResourceCost.FREE);
        return resource;
    }

    private LearnStageResourceOverride catalogOverride(
            String catalogSlug,
            boolean disabled,
            String overrideUrl,
            boolean preferred
    ) {
        LearnStageResourceOverride override = LearnStageResourceOverride.create();
        override.setTechnologySlug("java");
        override.setStageSlug("introduction");
        override.setResourceSlug(catalogSlug);
        override.setCatalogResourceSlug(catalogSlug);
        override.setResourceKind(RoadmapResourceKind.LEARNING);
        override.setDisabled(disabled);
        override.setOverrideUrl(overrideUrl);
        override.setPreferred(preferred);
        override.setEnabled(true);
        override.setResourceOrder(0);
        return override;
    }

    private LearnStageResourceOverride organizationOverride(String slug, String title, String url) {
        LearnStageResourceOverride override = LearnStageResourceOverride.create();
        override.setTechnologySlug("java");
        override.setStageSlug("introduction");
        override.setResourceSlug(slug);
        override.setCatalogResourceSlug(null);
        override.setResourceKind(RoadmapResourceKind.LEARNING);
        override.setDisabled(false);
        override.setOverrideUrl(url);
        override.setPreferred(false);
        override.setEnabled(true);
        override.setTitle(title);
        override.setResourceType(RoadmapResourceType.OTHER);
        override.setProvider("Internal");
        override.setFreePaid(RoadmapResourceCost.FREE);
        override.setResourceOrder(0);
        return override;
    }
}
