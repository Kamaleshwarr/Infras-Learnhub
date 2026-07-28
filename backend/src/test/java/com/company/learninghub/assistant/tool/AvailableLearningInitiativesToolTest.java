package com.company.learninghub.assistant.tool;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.dto.InitiativeCreatedByResponse;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.service.LearningInitiativeService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailableLearningInitiativesToolTest {

    @Mock
    private LearningInitiativeService learningInitiativeService;

    private AvailableLearningInitiativesTool tool;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        tool = new AvailableLearningInitiativesTool(learningInitiativeService);
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        authenticatedUser = AuthenticatedUser.from(user);
    }

    @Test
    void returnsAvailableInitiatives() {
        InitiativeResponse initiative = new InitiativeResponse(
                UUID.randomUUID(),
                "Cloud Certification",
                "Description",
                "Reward",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                InitiativeStatus.ACTIVE,
                new InitiativeCreatedByResponse(UUID.randomUUID(), "ADMIN001", "Admin", "admin@learninghub.local"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        Page<InitiativeResponse> page = new PageImpl<>(List.of(initiative), PageRequest.of(0, 20), 1);
        when(learningInitiativeService.list(isNull(), isNull(), any(), eq(authenticatedUser))).thenReturn(page);

        ToolResult result = tool.execute(new AssistantToolContext(authenticatedUser, "learning initiatives"));

        assertThat(result.text()).contains("1 learning initiative");
        assertThat(result.structuredData()).isInstanceOf(PageResponse.class);
        verify(learningInitiativeService).list(isNull(), isNull(), any(), eq(authenticatedUser));
    }
}
