package com.company.learninghub.learn.controller;

import com.company.learninghub.learn.dto.CreateResourceOverrideRequest;
import com.company.learninghub.learn.dto.ManagedResourceResponse;
import com.company.learninghub.learn.dto.ResourceOverrideResponse;
import com.company.learninghub.learn.dto.RoadmapResourceResponse;
import com.company.learninghub.learn.dto.StageResourceAdminResponse;
import com.company.learninghub.learn.dto.UpdateResourceOverrideRequest;
import com.company.learninghub.learn.service.LearnResourceOverrideService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearnResourceOverrideManageControllerTest {

    @Test
    void getStageResourcesReturnsAdminView() {
        LearnResourceOverrideService service = mock(LearnResourceOverrideService.class);
        LearnResourceOverrideManageController controller = new LearnResourceOverrideManageController(service);
        UUID technologyId = UUID.randomUUID();
        StageResourceAdminResponse response = new StageResourceAdminResponse(
                "introduction",
                "Introduction",
                1,
                List.of(new ManagedResourceResponse(
                        new RoadmapResourceResponse(
                                "oracle-docs",
                                "Oracle Java Tutorial",
                                "https://docs.oracle.com/javase/tutorial/",
                                "OFFICIAL_DOCUMENTATION",
                                "Oracle Docs",
                                "FREE"
                        ),
                        new RoadmapResourceResponse(
                                "oracle-docs",
                                "Oracle Java Tutorial",
                                "https://internal.example.com/java",
                                "OFFICIAL_DOCUMENTATION",
                                "Oracle Docs",
                                "FREE"
                        ),
                        null,
                        "DEFAULT"
                ))
        );

        when(service.getStageResources(technologyId, "introduction")).thenReturn(response);

        ResponseEntity<StageResourceAdminResponse> result = controller.getStageResources(technologyId, "introduction");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void createOverrideReturnsCreated() {
        LearnResourceOverrideService service = mock(LearnResourceOverrideService.class);
        LearnResourceOverrideManageController controller = new LearnResourceOverrideManageController(service);
        UUID technologyId = UUID.randomUUID();
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
        ResourceOverrideResponse response = new ResourceOverrideResponse(
                UUID.randomUUID().toString(),
                "java",
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
                0,
                false,
                "URL_OVERRIDE"
        );

        when(service.createOverride(technologyId, request)).thenReturn(response);

        ResponseEntity<ResourceOverrideResponse> result = controller.createOverride(technologyId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void restoreDefaultDelegatesToService() {
        LearnResourceOverrideService service = mock(LearnResourceOverrideService.class);
        LearnResourceOverrideManageController controller = new LearnResourceOverrideManageController(service);
        UUID technologyId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.restoreDefault(technologyId, "introduction", "oracle-docs");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).restoreDefault(technologyId, "introduction", "oracle-docs");
    }

    @Test
    void updateOverrideReturnsUpdatedResource() {
        LearnResourceOverrideService service = mock(LearnResourceOverrideService.class);
        LearnResourceOverrideManageController controller = new LearnResourceOverrideManageController(service);
        UUID technologyId = UUID.randomUUID();
        UUID overrideId = UUID.randomUUID();
        UpdateResourceOverrideRequest request = new UpdateResourceOverrideRequest(
                true, null, null, null, null, null, null, null, null, null
        );
        ResourceOverrideResponse response = new ResourceOverrideResponse(
                overrideId.toString(),
                "java",
                "introduction",
                "oracle-docs",
                "oracle-docs",
                "LEARNING",
                true,
                null,
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                0,
                false,
                "DISABLED"
        );

        when(service.updateOverride(technologyId, overrideId, request)).thenReturn(response);

        ResponseEntity<ResourceOverrideResponse> result = controller.updateOverride(technologyId, overrideId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }
}
