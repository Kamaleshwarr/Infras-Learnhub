package com.company.learninghub.submission.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.notification.service.NotificationService;
import com.company.learninghub.storage.CertificateFileStorageService;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.submission.mapper.CertificateSubmissionMapper;
import com.company.learninghub.submission.repository.CertificateDocumentRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = CertificateSubmissionMethodSecurityTest.TestConfig.class)
class CertificateSubmissionMethodSecurityTest {

    private static final Instant NOW = Instant.parse("2026-06-06T07:00:00Z");

    @Autowired
    private CertificateSubmissionService submissionService;

    @Autowired
    private CertificateSubmissionRepository submissionRepository;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotListAllSubmissionsOrApprove() {
        assertThatThrownBy(() -> submissionService.listAll(null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> submissionService.approve(UUID.randomUUID(), principal(RoleName.EMPLOYEE)))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> submissionService.reject(UUID.randomUUID(), "reason", principal(RoleName.EMPLOYEE)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCannotUseEmployeeOnlyEndpoints() {
        assertThatThrownBy(() -> submissionService.listOwn(null, null, PageRequest.of(0, 20), principal(RoleName.ADMIN)))
                .isInstanceOf(AccessDeniedException.class);

        MockMultipartFile file = new MockMultipartFile(
                "certificateFile",
                "certificate.pdf",
                "application/pdf",
                "certificate".getBytes()
        );
        assertThatThrownBy(() -> submissionService.submit(UUID.randomUUID(), file, null, principal(RoleName.ADMIN)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanListAllSubmissions() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(submissionRepository.findForAdmin(eq(null), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(submissionService.listAll(null, null, null, pageable).getTotalElements()).isZero();
    }

    private AuthenticatedUser principal(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return AuthenticatedUser.from(user);
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        CertificateSubmissionService certificateSubmissionService(
                CertificateSubmissionRepository submissionRepository,
                CertificateDocumentRepository documentRepository,
                LearningInitiativeRepository initiativeRepository,
                UserRepository userRepository,
                CertificateFileStorageService fileStorageService,
                StorageProperties storageProperties,
                CertificateSubmissionMapper submissionMapper,
                NotificationService notificationService
        ) {
            return new CertificateSubmissionService(
                    submissionRepository,
                    documentRepository,
                    initiativeRepository,
                    userRepository,
                    fileStorageService,
                    storageProperties,
                    submissionMapper,
                    notificationService,
                    Clock.fixed(NOW, ZoneOffset.UTC)
            );
        }

        @Bean
        CertificateSubmissionRepository certificateSubmissionRepository() {
            return mock(CertificateSubmissionRepository.class);
        }

        @Bean
        CertificateDocumentRepository certificateDocumentRepository() {
            return mock(CertificateDocumentRepository.class);
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
        CertificateFileStorageService certificateFileStorageService() {
            return mock(CertificateFileStorageService.class);
        }

        @Bean
        StorageProperties storageProperties() {
            StorageProperties storageProperties = new StorageProperties();
            storageProperties.setMaxFileSizeBytes(1024);
            return storageProperties;
        }

        @Bean
        CertificateSubmissionMapper certificateSubmissionMapper() {
            return new CertificateSubmissionMapper();
        }

        @Bean
        NotificationService notificationService() {
            return mock(NotificationService.class);
        }
    }
}

