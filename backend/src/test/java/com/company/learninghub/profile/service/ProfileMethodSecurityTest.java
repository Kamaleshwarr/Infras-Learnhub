package com.company.learninghub.profile.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.profile.mapper.ProfileMapper;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = ProfileMethodSecurityTest.TestConfig.class)
class ProfileMethodSecurityTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void unauthenticatedUserCannotAccessProfile() {
        User user = employeeUser();

        assertThatThrownBy(() -> profileService.getProfile(AuthenticatedUser.from(user)))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanAccessProfile() {
        User user = employeeUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(profileService.getProfile(AuthenticatedUser.from(user)).fullName()).isEqualTo("Jane Doe");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessProfile() {
        User user = employeeUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(profileService.getProfile(AuthenticatedUser.from(user)).email()).isEqualTo("jane.doe@company.com");
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        ProfileService profileService(UserRepository userRepository, ProfileMapper profileMapper) {
            return new ProfileService(userRepository, profileMapper);
        }

        @Bean
        ProfileMapper profileMapper() {
            return new ProfileMapper();
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }
}
