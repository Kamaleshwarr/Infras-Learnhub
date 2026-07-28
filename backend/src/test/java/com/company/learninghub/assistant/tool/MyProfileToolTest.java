package com.company.learninghub.assistant.tool;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.service.ProfileService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyProfileToolTest {

    @Mock
    private ProfileService profileService;

    private MyProfileTool tool;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        tool = new MyProfileTool(profileService);
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        authenticatedUser = AuthenticatedUser.from(user);
    }

    @Test
    void returnsProfileStructuredResult() {
        ProfileResponse profile = new ProfileResponse(
                UUID.randomUUID(),
                "EMP001",
                "Jane Doe",
                "jane.doe@company.com",
                RoleName.EMPLOYEE,
                true,
                false,
                false,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
        when(profileService.getProfile(authenticatedUser)).thenReturn(profile);

        ToolResult result = tool.execute(new AssistantToolContext(authenticatedUser, "my profile"));

        assertThat(result.text()).contains("Jane Doe");
        assertThat(result.structuredData()).isEqualTo(profile);
    }
}
