package com.company.learninghub.profile.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.GlobalExceptionHandler;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.dto.ProfileUpdateResponse;
import com.company.learninghub.profile.dto.UpdateProfileRequest;
import com.company.learninghub.profile.service.ProfileService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProfileService profileService;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        profileService = mock(ProfileService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfileController(profileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
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

    @Test
    void updateProfileReturnsUpdatedProfile() throws Exception {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(employeeUser());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null, authenticatedUser.getAuthorities())
        );

        UUID userId = authenticatedUser.getId();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        ProfileResponse profile = new ProfileResponse(
                userId,
                "EMP001",
                "Jane Smith",
                "jane.smith@company.com",
                RoleName.EMPLOYEE,
                true,
                false,
                false,
                null,
                now,
                now
        );
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Smith", "jane.smith@company.com");
        when(profileService.updateProfile(eq(authenticatedUser), any(UpdateProfileRequest.class)))
                .thenReturn(new ProfileUpdateResponse(profile, "new-jwt"));

        mockMvc.perform(put("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.fullName").value("Jane Smith"))
                .andExpect(jsonPath("$.profile.email").value("jane.smith@company.com"))
                .andExpect(jsonPath("$.accessToken").value("new-jwt"));

        verify(profileService).updateProfile(eq(authenticatedUser), any(UpdateProfileRequest.class));
    }

    @Test
    void updateProfileRejectsInvalidRequestPayload() throws Exception {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(employeeUser());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null, authenticatedUser.getAuthorities())
        );

        UpdateProfileRequest request = new UpdateProfileRequest("", "not-an-email");

        mockMvc.perform(put("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.fullName").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void uploadAvatarReturnsUpdatedProfile() throws Exception {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(employeeUser());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null, authenticatedUser.getAuthorities())
        );
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "avatar".getBytes());
        ProfileResponse profile = new ProfileResponse(
                authenticatedUser.getId(),
                "EMP001",
                "Jane Doe",
                "jane.doe@company.com",
                RoleName.EMPLOYEE,
                true,
                false,
                true,
                "/api/v1/profile/avatar",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z")
        );
        when(profileService.uploadAvatar(eq(authenticatedUser), any())).thenReturn(profile);

        mockMvc.perform(multipart("/api/v1/profile/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAvatar").value(true))
                .andExpect(jsonPath("$.avatarUrl").value("/api/v1/profile/avatar"));
    }

    @Test
    void deleteAvatarReturnsNoContent() throws Exception {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(employeeUser());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null, authenticatedUser.getAuthorities())
        );

        mockMvc.perform(delete("/api/v1/profile/avatar"))
                .andExpect(status().isNoContent());

        verify(profileService).deleteAvatar(authenticatedUser);
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }
}
