package com.company.learninghub.learn.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.TechnologyCreatedByResponse;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.service.LearnTechnologyService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearnTechnologyControllerTest {

    @Test
    void listWrapsPagedServiceResponse() {
        LearnTechnologyService technologyService = mock(LearnTechnologyService.class);
        LearnTechnologyController controller = new LearnTechnologyController(technologyService);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        TechnologyResponse technology = technologyResponse();

        when(technologyService.listEmployeeTechnologies("aws", TechnologyCategory.CLOUD, null, pageable))
                .thenReturn(new PageImpl<>(List.of(technology), pageable, 1));

        ResponseEntity<?> response = controller.list("aws", TechnologyCategory.CLOUD, null, pageable);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("totalElements", 1L);
        verify(technologyService).listEmployeeTechnologies("aws", TechnologyCategory.CLOUD, null, pageable);
    }

    @Test
    void getByIdReturnsTechnology() {
        LearnTechnologyService technologyService = mock(LearnTechnologyService.class);
        LearnTechnologyController controller = new LearnTechnologyController(technologyService);
        AuthenticatedUser principal = AuthenticatedUser.from(user());
        UUID technologyId = UUID.randomUUID();
        TechnologyResponse technology = technologyResponse();

        when(technologyService.getById(technologyId, principal)).thenReturn(technology);

        ResponseEntity<TechnologyResponse> response = controller.getById(technologyId, principal);

        assertThat(response.getBody()).isEqualTo(technology);
    }

    private User user() {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }

    private TechnologyResponse technologyResponse() {
        return new TechnologyResponse(
                UUID.randomUUID(),
                "aws",
                "Amazon Web Services (AWS)",
                "AWS",
                "Cloud platform",
                TechnologyCategory.CLOUD,
                TechnologyDifficulty.INTERMEDIATE,
                TechnologyStatus.PUBLISHED,
                true,
                null,
                true,
                "6-8 weeks",
                "https://aws.amazon.com/",
                "https://docs.aws.amazon.com/",
                List.of("cloud"),
                null,
                "1.0.0",
                "platform-team",
                true,
                List.of(),
                new TechnologyCreatedByResponse(
                        UUID.randomUUID(),
                        "ADMIN001",
                        "Learning Hub Admin",
                        "admin@learninghub.local"
                ),
                Instant.parse("2026-06-06T00:00:00Z"),
                Instant.parse("2026-06-06T00:00:00Z")
        );
    }
}
