package com.company.learninghub.initiative.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.dto.InitiativeCreatedByResponse;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.service.LearningInitiativeService;
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

class LearningInitiativeControllerTest {

    @Test
    void listWrapsPagedServiceResponse() {
        LearningInitiativeService initiativeService = mock(LearningInitiativeService.class);
        LearningInitiativeController controller = new LearningInitiativeController(initiativeService);
        AuthenticatedUser principal = AuthenticatedUser.from(user());
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        InitiativeResponse initiative = initiativeResponse();

        when(initiativeService.list(InitiativeStatus.ACTIVE, "AI", pageable, principal))
                .thenReturn(new PageImpl<>(List.of(initiative), pageable, 1));

        ResponseEntity<?> response = controller.list(InitiativeStatus.ACTIVE, "AI", pageable, principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("totalElements", 1L);
        verify(initiativeService).list(InitiativeStatus.ACTIVE, "AI", pageable, principal);
    }

    private User user() {
        User user = new User("ADMIN001", "admin@learninghub.local", "Learning Hub Admin", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.ADMIN));
        return user;
    }

    private InitiativeResponse initiativeResponse() {
        return new InitiativeResponse(
                UUID.randomUUID(),
                "AWS AI Certification",
                "Complete the certification.",
                "Recognition",
                Instant.parse("2026-06-06T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                InitiativeStatus.ACTIVE,
                new InitiativeCreatedByResponse(
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

