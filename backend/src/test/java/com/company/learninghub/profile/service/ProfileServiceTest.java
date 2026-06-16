package com.company.learninghub.profile.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.service.AuthenticationService;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.dto.ProfileUpdateResponse;
import com.company.learninghub.profile.dto.UpdateProfileRequest;
import com.company.learninghub.profile.mapper.ProfileMapper;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(userRepository, new ProfileMapper(), authenticationService);
    }

    @Test
    void getProfileReturnsCurrentUserProfile() {
        User user = employeeUser();
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ProfileResponse response = profileService.getProfile(principal);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.employeeId()).isEqualTo("EMP001");
        assertThat(response.fullName()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane.doe@company.com");
        assertThat(response.role()).isEqualTo(RoleName.EMPLOYEE);
        assertThat(response.active()).isTrue();
        assertThat(response.mustChangePassword()).isFalse();
        assertThat(response.hasAvatar()).isFalse();
        assertThat(response.avatarUrl()).isNull();
        assertThat(response.createdAtUtc()).isEqualTo(user.getCreatedAt());
        assertThat(response.updatedAtUtc()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void getProfileIncludesAvatarUrlWhenAvatarExists() {
        User user = employeeUser();
        user.setAvatarStorageKey("avatars/user/photo.png");
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ProfileResponse response = profileService.getProfile(principal);

        assertThat(response.hasAvatar()).isTrue();
        assertThat(response.avatarUrl()).isEqualTo("/api/v1/profile/avatar");
    }

    @Test
    void getProfilePrefersAdminRoleWhenBothRolesPresent() {
        User user = employeeUser();
        user.assignRole(adminRole());
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ProfileResponse response = profileService.getProfile(principal);

        assertThat(response.role()).isEqualTo(RoleName.ADMIN);
    }

    @Test
    void getProfileThrowsWhenAuthenticatedUserMissingFromDatabase() {
        User user = employeeUser();
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Authenticated user was not found");
    }

    @Test
    void updateProfileUpdatesFullNameWithoutIssuingNewToken() {
        User user = employeeUser();
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateResponse response = profileService.updateProfile(
                principal,
                new UpdateProfileRequest("Jane Smith", "jane.doe@company.com")
        );

        assertThat(response.profile().fullName()).isEqualTo("Jane Smith");
        assertThat(response.profile().email()).isEqualTo("jane.doe@company.com");
        assertThat(response.accessToken()).isNull();
        verify(authenticationService, never()).issueAccessToken(any(User.class));
    }

    @Test
    void updateProfileIssuesNewTokenWhenEmailChanges() {
        User user = employeeUser();
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("jane.smith@company.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authenticationService.issueAccessToken(any(User.class))).thenReturn("new-jwt");

        ProfileUpdateResponse response = profileService.updateProfile(
                principal,
                new UpdateProfileRequest("Jane Doe", "JANE.SMITH@company.com")
        );

        assertThat(response.profile().email()).isEqualTo("jane.smith@company.com");
        assertThat(response.accessToken()).isEqualTo("new-jwt");
        verify(authenticationService).issueAccessToken(any(User.class));
    }

    @Test
    void updateProfileRejectsDuplicateEmail() {
        User user = employeeUser();
        User otherUser = employeeUser();
        ReflectionTestUtils.setField(otherUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(otherUser, "email", "existing@company.com");
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("existing@company.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> profileService.updateProfile(
                principal,
                new UpdateProfileRequest("Jane Doe", "existing@company.com")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void updateProfileAllowsKeepingSameEmail() {
        User user = employeeUser();
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("jane.doe@company.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateResponse response = profileService.updateProfile(
                principal,
                new UpdateProfileRequest("Jane Smith", "jane.doe@company.com")
        );

        assertThat(response.profile().fullName()).isEqualTo("Jane Smith");
        assertThat(response.accessToken()).isNull();
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", id);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        ReflectionTestUtils.setField(user, "createdAt", now);
        ReflectionTestUtils.setField(user, "updatedAt", now);
        user.assignRole(employeeRole());
        return user;
    }

    private Role employeeRole() {
        Role role = new Role(RoleName.EMPLOYEE);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }

    private Role adminRole() {
        Role role = new Role(RoleName.ADMIN);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }
}
