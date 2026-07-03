package com.company.learninghub.learn.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.learn.dto.RoadmapResponse;
import com.company.learninghub.learn.dto.RoadmapStageResponse;
import com.company.learninghub.learn.service.LearnRoadmapService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearnRoadmapControllerTest {

    @Test
    void getBySlugReturnsRoadmap() {
        LearnRoadmapService roadmapService = mock(LearnRoadmapService.class);
        LearnRoadmapController controller = new LearnRoadmapController(roadmapService);
        AuthenticatedUser principal = employee();
        RoadmapResponse roadmap = roadmapResponse();

        when(roadmapService.getRoadmapBySlug("java", principal)).thenReturn(roadmap);

        ResponseEntity<RoadmapResponse> response = controller.getBySlug("java", principal);

        assertThat(response.getBody()).isEqualTo(roadmap);
        verify(roadmapService).getRoadmapBySlug("java", principal);
    }

    @Test
    void getByTechnologyIdReturnsRoadmap() {
        LearnRoadmapService roadmapService = mock(LearnRoadmapService.class);
        LearnRoadmapController controller = new LearnRoadmapController(roadmapService);
        AuthenticatedUser principal = employee();
        UUID technologyId = UUID.randomUUID();
        RoadmapResponse roadmap = roadmapResponse();

        when(roadmapService.getRoadmapByTechnologyId(technologyId, principal)).thenReturn(roadmap);

        ResponseEntity<RoadmapResponse> response = controller.getByTechnologyId(technologyId, principal);

        assertThat(response.getBody()).isEqualTo(roadmap);
        verify(roadmapService).getRoadmapByTechnologyId(technologyId, principal);
    }

    private AuthenticatedUser employee() {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return AuthenticatedUser.from(user);
    }

    private RoadmapResponse roadmapResponse() {
        return new RoadmapResponse(
                UUID.randomUUID().toString(),
                "java",
                "Java",
                "1.0.0",
                "Java roadmap",
                "platform-team",
                "https://roadmap.sh/java",
                "2026-07-03T00:00:00Z",
                1,
                "1 week",
                1,
                1,
                List.of(new RoadmapStageResponse(
                        UUID.randomUUID().toString(),
                        1,
                        "introduction",
                        "Introduction",
                        "Get started",
                        "1 week",
                        null,
                        List.of(),
                        List.of()
                ))
        );
    }
}
