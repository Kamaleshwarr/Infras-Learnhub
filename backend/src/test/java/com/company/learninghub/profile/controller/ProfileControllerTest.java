package com.company.learninghub.profile.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.service.ProfileService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileControllerTest {

    private ProfileService profileService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        profileService = mock(ProfileService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfileController(profileService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfileReturnsCurrentUserProfile() throws Exception {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(employeeUser());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null, authenticatedUser.getAuthorities())
        );

        UUID userId = authenticatedUser.getId();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        ProfileResponse response = new ProfileResponse(
                userId,
                "EMP001",
                "Jane Doe",
                "jane.doe@company.com",
                RoleName.EMPLOYEE,
                true,
                false,
                false,
                null,
                now,
                now
        );
        when(profileService.getProfile(authenticatedUser)).thenReturn(response);

        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@company.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.hasAvatar").value(false))
                .andExpect(jsonPath("$.avatarUrl").doesNotExist())
                .andExpect(jsonPath("$.createdAtUtc").exists())
                .andExpect(jsonPath("$.updatedAtUtc").exists());

        verify(profileService).getProfile(authenticatedUser);
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }
}
