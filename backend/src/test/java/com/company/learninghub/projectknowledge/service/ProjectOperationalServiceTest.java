package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.EnvironmentReferenceType;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.domain.ProjectEnvironment;
import com.company.learninghub.projectknowledge.domain.ProjectEnvironmentReference;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.domain.RepositoryProvider;
import com.company.learninghub.projectknowledge.domain.RepositoryType;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentReferenceRequest;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentRequest;
import com.company.learninghub.projectknowledge.dto.ProjectLinkedRepositoryRequest;
import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.mapper.ProjectOperationalMapper;
import com.company.learninghub.projectknowledge.repository.ProjectEnvironmentReferenceRepository;
import com.company.learninghub.projectknowledge.repository.ProjectEnvironmentRepository;
import com.company.learninghub.projectknowledge.repository.ProjectLinkedRepositoryRepository;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectOperationalServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository memberRepository;
    @Mock private ProjectEnvironmentRepository environmentRepository;
    @Mock private ProjectEnvironmentReferenceRepository referenceRepository;
    @Mock private ProjectLinkedRepositoryRepository linkedRepositoryRepository;
    @Mock private UserRepository userRepository;

    private ProjectScopeAuthorization authorization;
    private ProjectEnvironmentService environmentService;
    private ProjectLinkedRepositoryService repositoryService;

    private User owner;
    private User viewer;
    private AuthenticatedUser ownerPrincipal;
    private AuthenticatedUser viewerPrincipal;
    private Project project;
    private ProjectEnvironment environment;

    @BeforeEach
    void setUp() {
        authorization = new ProjectScopeAuthorization(projectRepository, memberRepository);
        ProjectOperationalMapper mapper = new ProjectOperationalMapper(new ProjectKnowledgeMapper());
        environmentService = new ProjectEnvironmentService(
                environmentRepository,
                referenceRepository,
                authorization,
                mapper,
                userRepository
        );
        repositoryService = new ProjectLinkedRepositoryService(
                linkedRepositoryRepository,
                authorization,
                mapper,
                userRepository
        );

        owner = user("OWNER001", "owner@example.com", RoleName.EMPLOYEE);
        viewer = user("VIEW001", "viewer@example.com", RoleName.EMPLOYEE);
        ownerPrincipal = AuthenticatedUser.from(owner);
        viewerPrincipal = AuthenticatedUser.from(viewer);
        project = new Project("Payments", "desc", ProjectAccessType.MEMBERS_ONLY, owner);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        environment = new ProjectEnvironment(project, "QA", "Quality", 0, owner);
        ReflectionTestUtils.setField(environment, "id", UUID.randomUUID());
    }

    @Test
    void ownerCanCreateEnvironmentAndReference() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(environmentRepository.existsByProjectIdAndNameIgnoreCase(project.getId(), "QA", null)).thenReturn(false);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(environmentRepository.save(any(ProjectEnvironment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(environmentRepository.findById(environment.getId())).thenReturn(Optional.of(environment));
        when(referenceRepository.save(any(ProjectEnvironmentReference.class))).thenAnswer(invocation -> {
            ProjectEnvironmentReference reference = invocation.getArgument(0);
            ReflectionTestUtils.setField(reference, "id", UUID.randomUUID());
            return reference;
        });

        environmentService.createEnvironment(project.getId(), new ProjectEnvironmentRequest("QA", "Quality", 0, true), ownerPrincipal);
        var response = environmentService.createReference(
                project.getId(),
                environment.getId(),
                new ProjectEnvironmentReferenceRequest(
                        "Swagger",
                        EnvironmentReferenceType.SWAGGER,
                        "https://example.com/swagger",
                        null,
                        0,
                        true
                ),
                ownerPrincipal
        );

        assertThat(response.referenceType()).isEqualTo(EnvironmentReferenceType.SWAGGER);
    }

    @Test
    void viewerCannotCreateEnvironment() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(
                project.getId(), viewer.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(false);
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(
                project.getId(), viewer.getId(), ProjectRole.OWNER)).thenReturn(false);

        assertThatThrownBy(() -> environmentService.createEnvironment(
                project.getId(),
                new ProjectEnvironmentRequest("QA", null, 0, true),
                viewerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project OWNER or CONTRIBUTOR role is required");
    }

    @Test
    void rejectsEmbeddedCredentialReferenceUrl() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(environmentRepository.findById(environment.getId())).thenReturn(Optional.of(environment));

        assertThatThrownBy(() -> environmentService.createReference(
                project.getId(),
                environment.getId(),
                new ProjectEnvironmentReferenceRequest(
                        "Swagger",
                        EnvironmentReferenceType.SWAGGER,
                        "https://user:pass@example.com/swagger",
                        null,
                        0,
                        true
                ),
                ownerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reference URL must not contain embedded credentials");
    }

    @Test
    void cannotDeleteEnvironmentWithReferences() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(environmentRepository.findById(environment.getId())).thenReturn(Optional.of(environment));
        when(referenceRepository.existsByEnvironmentId(environment.getId())).thenReturn(true);

        assertThatThrownBy(() -> environmentService.deleteEnvironment(project.getId(), environment.getId(), ownerPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Environment must be empty before deletion");
    }

    @Test
    void membersOnlyProjectHiddenFromOutsider() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> environmentService.listEnvironments(project.getId(), null, false, viewerPrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ownerCanCreateRepository() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(linkedRepositoryRepository.existsByProjectIdAndNameIgnoreCase(project.getId(), "Backend Service", null)).thenReturn(false);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(linkedRepositoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = repositoryService.createRepository(
                project.getId(),
                new ProjectLinkedRepositoryRequest(
                        "Backend Service",
                        "API",
                        RepositoryType.BACKEND,
                        RepositoryProvider.GITHUB,
                        "https://github.com/example/backend",
                        "main",
                        0,
                        true
                ),
                ownerPrincipal
        );

        assertThat(response.repositoryType()).isEqualTo(RepositoryType.BACKEND);
        verify(linkedRepositoryRepository).save(any());
    }

    private User user(String employeeId, String email, RoleName roleName) {
        User user = new User(employeeId, email, employeeId, "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }
}
