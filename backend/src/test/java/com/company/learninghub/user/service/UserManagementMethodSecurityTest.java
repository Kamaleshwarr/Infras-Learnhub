package com.company.learninghub.user.service;

import com.company.learninghub.auth.service.PasswordService;
import com.company.learninghub.user.repository.RoleRepository;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import com.company.learninghub.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = UserManagementMethodSecurityTest.TestConfig.class)
class UserManagementMethodSecurityTest {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotUseUserAdministration() {
        assertThatThrownBy(() -> userManagementService.listUsers(null, null, null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUseUserAdministration() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAll(org.mockito.ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(userManagementService.listUsers(null, null, null, null, null, pageable).getTotalElements()).isZero();
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        UserManagementService userManagementService(
                UserRepository userRepository,
                RoleRepository roleRepository,
                PasswordEncoder passwordEncoder,
                PasswordService passwordService
        ) {
            return new UserManagementService(userRepository, roleRepository, passwordEncoder, passwordService);
        }

        @Bean
        PasswordService passwordService() {
            return mock(PasswordService.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        RoleRepository roleRepository() {
            return mock(RoleRepository.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }
    }
}

