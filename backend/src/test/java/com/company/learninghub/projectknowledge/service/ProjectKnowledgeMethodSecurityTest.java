package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeAccessEventRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeFolderRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeItemRepository;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.storage.ProjectKnowledgeStorageService;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

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

    @Test
    void unauthenticatedProjectSearchIsDenied() {
        assertThatThrownBy(() -> service.searchProjects(null, null, false, PageRequest.of(0, 20), null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanSearchProjects() {
        when(projectRepository.search(eq(null), eq(null), eq(false), any(), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        com.company.learninghub.user.domain.User user = new com.company.learninghub.user.domain.User("EMP001", "employee@example.com", "Employee", "$2a$12$hash");
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", java.util.UUID.randomUUID());
        user.assignRole(new com.company.learninghub.user.domain.Role(com.company.learninghub.user.domain.RoleName.EMPLOYEE));

        assertThat(service.searchProjects(null, null, false, PageRequest.of(0, 20), com.company.learninghub.auth.security.AuthenticatedUser.from(user)).getTotalElements())
                .isZero();
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
                ProjectKnowledgeAccessEventRepository accessEventRepository,
                UserRepository userRepository,
                ProjectKnowledgeStorageService storageService,
                StorageProperties storageProperties,
                ProjectKnowledgeMapper mapper
        ) {
            return new ProjectKnowledgeService(projectRepository, memberRepository, folderRepository, itemRepository,
                    accessEventRepository, userRepository, storageService, storageProperties, mapper);
        }

        @Bean ProjectRepository projectRepository() { return mock(ProjectRepository.class); }
        @Bean ProjectMemberRepository projectMemberRepository() { return mock(ProjectMemberRepository.class); }
        @Bean ProjectKnowledgeFolderRepository projectKnowledgeFolderRepository() { return mock(ProjectKnowledgeFolderRepository.class); }
        @Bean ProjectKnowledgeItemRepository projectKnowledgeItemRepository() { return mock(ProjectKnowledgeItemRepository.class); }
        @Bean ProjectKnowledgeAccessEventRepository projectKnowledgeAccessEventRepository() { return mock(ProjectKnowledgeAccessEventRepository.class); }
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean ProjectKnowledgeStorageService projectKnowledgeStorageService() { return mock(ProjectKnowledgeStorageService.class); }
        @Bean StorageProperties storageProperties() { return new StorageProperties(); }
        @Bean ProjectKnowledgeMapper projectKnowledgeMapper() { return new ProjectKnowledgeMapper(); }
    }
}

