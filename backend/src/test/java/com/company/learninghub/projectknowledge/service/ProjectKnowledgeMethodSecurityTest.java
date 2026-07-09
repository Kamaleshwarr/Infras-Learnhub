package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.projectknowledge.dto.CreateProjectRequest;
import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.repository.ProjectEnvironmentRepository;
import com.company.learninghub.projectknowledge.repository.ProjectLinkedRepositoryRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeAccessEventRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeFolderRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeItemRepository;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.storage.ProjectKnowledgeStorageService;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = ProjectKnowledgeMethodSecurityTest.TestConfig.class)
class ProjectKnowledgeMethodSecurityTest {

    @Autowired
    private ProjectKnowledgeService service;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearnTechnologyProjectLinkRepository projectLinkRepository;

    @Autowired
    private ProjectMemberRepository memberRepository;

    @Autowired
    private ProjectEnvironmentRepository environmentRepository;

    @Autowired
    private ProjectLinkedRepositoryRepository linkedRepositoryRepository;

    @Test
    void unauthenticatedSearchIsDenied() {
        assertThatThrownBy(() -> service.searchProjects(null, null, null, false, false, PageRequest.of(0, 20), null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanSearchProjects() {
        when(projectRepository.search(eq(null), eq(null), eq(null), eq(false), eq(false), any(), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        com.company.learninghub.user.domain.User user = new com.company.learninghub.user.domain.User("EMP001", "employee@example.com", "Employee", "$2a$12$hash");
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", java.util.UUID.randomUUID());
        user.assignRole(new com.company.learninghub.user.domain.Role(com.company.learninghub.user.domain.RoleName.EMPLOYEE));

        assertThat(service.searchProjects(null, null, null, false, false, PageRequest.of(0, 20), com.company.learninghub.auth.security.AuthenticatedUser.from(user)).getTotalElements())
                .isZero();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotCreateProject() {
        assertThatThrownBy(() -> service.createProject(
                new CreateProjectRequest("Payments", "desc", ProjectAccessType.PUBLIC),
                employeePrincipal()
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateProject() {
        com.company.learninghub.user.domain.User admin = new com.company.learninghub.user.domain.User("ADM001", "admin@example.com", "Admin", "$2a$12$hash");
        java.util.UUID adminId = java.util.UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(admin, "id", adminId);
        admin.assignRole(new com.company.learninghub.user.domain.Role(com.company.learninghub.user.domain.RoleName.ADMIN));

        when(projectRepository.existsByNameIgnoreCase("Payments")).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(projectRepository.save(any())).thenAnswer(invocation -> {
            com.company.learninghub.projectknowledge.domain.Project saved = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", java.util.UUID.randomUUID());
            return saved;
        });
        when(memberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findOwnersByProjectIds(any(), any())).thenReturn(Collections.emptyList());
        when(memberRepository.countByProjectId(any())).thenReturn(0L);
        when(memberRepository.findByProjectIdInAndUserId(any(), any())).thenReturn(Collections.emptyList());
        when(projectLinkRepository.findPublishedTechnologiesByProjectIds(any(), any())).thenReturn(Collections.emptyList());
        when(environmentRepository.countByProjectIdAndActiveTrue(any())).thenReturn(0L);
        when(linkedRepositoryRepository.countByProjectIdAndActiveTrue(any())).thenReturn(0L);

        assertThat(service.createProject(
                new CreateProjectRequest("Payments", "desc", ProjectAccessType.PUBLIC),
                com.company.learninghub.auth.security.AuthenticatedUser.from(admin)
        ).name()).isEqualTo("Payments");
    }

    private com.company.learninghub.auth.security.AuthenticatedUser employeePrincipal() {
        com.company.learninghub.user.domain.User user = new com.company.learninghub.user.domain.User("EMP001", "employee@example.com", "Employee", "$2a$12$hash");
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", java.util.UUID.randomUUID());
        user.assignRole(new com.company.learninghub.user.domain.Role(com.company.learninghub.user.domain.RoleName.EMPLOYEE));
        return com.company.learninghub.auth.security.AuthenticatedUser.from(user);
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        ProjectKnowledgeService projectKnowledgeService(
                ProjectRepository projectRepository,
                ProjectMemberRepository memberRepository,
                ProjectKnowledgeFolderRepository folderRepository,
                ProjectKnowledgeItemRepository itemRepository,
                ProjectEnvironmentRepository environmentRepository,
                ProjectLinkedRepositoryRepository linkedRepositoryRepository,
                ProjectKnowledgeAccessEventRepository accessEventRepository,
                UserRepository userRepository,
                ProjectKnowledgeStorageService storageService,
                StorageProperties storageProperties,
                ProjectKnowledgeMapper mapper,
                LearnTechnologyProjectLinkRepository projectLinkRepository
        ) {
            return new ProjectKnowledgeService(projectRepository, memberRepository, folderRepository, itemRepository,
                    environmentRepository, linkedRepositoryRepository, accessEventRepository, userRepository,
                    storageService, storageProperties, mapper, projectLinkRepository);
        }

        @Bean ProjectRepository projectRepository() { return mock(ProjectRepository.class); }
        @Bean ProjectMemberRepository projectMemberRepository() { return mock(ProjectMemberRepository.class); }
        @Bean ProjectKnowledgeFolderRepository projectKnowledgeFolderRepository() { return mock(ProjectKnowledgeFolderRepository.class); }
        @Bean ProjectKnowledgeItemRepository projectKnowledgeItemRepository() { return mock(ProjectKnowledgeItemRepository.class); }
        @Bean ProjectEnvironmentRepository projectEnvironmentRepository() { return mock(ProjectEnvironmentRepository.class); }
        @Bean ProjectLinkedRepositoryRepository projectLinkedRepositoryRepository() { return mock(ProjectLinkedRepositoryRepository.class); }
        @Bean ProjectKnowledgeAccessEventRepository projectKnowledgeAccessEventRepository() { return mock(ProjectKnowledgeAccessEventRepository.class); }
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean ProjectKnowledgeStorageService projectKnowledgeStorageService() { return mock(ProjectKnowledgeStorageService.class); }
        @Bean StorageProperties storageProperties() { return new StorageProperties(); }
        @Bean ProjectKnowledgeMapper projectKnowledgeMapper() { return new ProjectKnowledgeMapper(); }
        @Bean LearnTechnologyProjectLinkRepository learnTechnologyProjectLinkRepository() { return mock(LearnTechnologyProjectLinkRepository.class); }
    }
}
