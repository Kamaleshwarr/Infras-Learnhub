package com.company.learninghub.initiative.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.mapper.LearningInitiativeMapper;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.submission.repository.CertificateSubmissionRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = LearningInitiativeMethodSecurityTest.TestConfig.class)
class LearningInitiativeMethodSecurityTest {

    private static final Instant NOW = Instant.parse("2026-06-06T06:00:00Z");

    @Autowired
    private LearningInitiativeService initiativeService;

    @Autowired
    private LearningInitiativeRepository initiativeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotCreateInitiative() {
        assertThatThrownBy(() -> initiativeService.create(createRequest(), principal(RoleName.EMPLOYEE)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateInitiative() {
        User admin = user(RoleName.ADMIN);
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(initiativeRepository.save(any(LearningInitiative.class))).thenAnswer(invocation -> invocation.getArgument(0));

        initiativeService.create(createRequest(), AuthenticatedUser.from(admin));

        verify(initiativeRepository).save(any(LearningInitiative.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanListInitiatives() {
        User admin = user(RoleName.ADMIN);
        LearningInitiative initiative = new LearningInitiative(
                "AWS AI",
                "Description",
                "Reward",
                NOW.minusSeconds(3600),
                NOW.plusSeconds(3600),
                InitiativeStatus.ACTIVE,
                admin
        );
        PageRequest pageable = PageRequest.of(0, 20);
        when(initiativeRepository.findAll(org.mockito.ArgumentMatchers.<Specification<LearningInitiative>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(initiative), pageable, 1));

        assertThat(initiativeService.list(null, null, pageable, principal(RoleName.EMPLOYEE)).getTotalElements())
                .isEqualTo(1);
    }

    private CreateInitiativeRequest createRequest() {
        return new CreateInitiativeRequest(
                "AWS AI",
                "Description",
                "Reward",
                NOW,
                NOW.plusSeconds(3600)
        );
    }

    private AuthenticatedUser principal(RoleName roleName) {
        return AuthenticatedUser.from(user(roleName));
    }

    private User user(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        LearningInitiativeService learningInitiativeService(
                LearningInitiativeRepository initiativeRepository,
                CertificateSubmissionRepository submissionRepository,
                UserRepository userRepository,
                LearningInitiativeMapper initiativeMapper
        ) {
            return new LearningInitiativeService(
                    initiativeRepository,
                    submissionRepository,
                    userRepository,
                    initiativeMapper,
                    Clock.fixed(NOW, ZoneOffset.UTC)
            );
        }

        @Bean
        CertificateSubmissionRepository certificateSubmissionRepository() {
            return mock(CertificateSubmissionRepository.class);
        }

        @Bean
        LearningInitiativeRepository learningInitiativeRepository() {
            return mock(LearningInitiativeRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        LearningInitiativeMapper learningInitiativeMapper() {
            return new LearningInitiativeMapper();
        }
    }
}

