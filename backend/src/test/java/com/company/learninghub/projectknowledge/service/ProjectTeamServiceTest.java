package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.domain.ProjectFunctionalRole;
import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.dto.ProjectMemberRequest;
import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.repository.ProjectExternalContactRepository;
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
class ProjectTeamServiceTest {

    @Mock private ProjectMemberRepository memberRepository;
    @Mock private ProjectExternalContactRepository externalContactRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    private ProjectScopeAuthorization authorization;
    private ProjectTeamService service;

    private User owner;
    private User contributor;
    private User outsider;
    private AuthenticatedUser ownerPrincipal;
    private AuthenticatedUser outsiderPrincipal;
    private Project project;
    private ProjectMember ownerMember;

    @BeforeEach
    void setUp() {
        authorization = new ProjectScopeAuthorization(projectRepository, memberRepository);
        service = new ProjectTeamService(
                memberRepository,
                externalContactRepository,
                authorization,
                new ProjectKnowledgeMapper(),
                userRepository
        );

        owner = user("OWNER001", "owner@example.com");
        contributor = user("DEV001", "dev@example.com");
        outsider = user("OUT001", "outsider@example.com");
        ownerPrincipal = AuthenticatedUser.from(owner);
        outsiderPrincipal = AuthenticatedUser.from(outsider);
        project = new Project("Payments", "desc", ProjectAccessType.MEMBERS_ONLY, owner);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        ownerMember = new ProjectMember(project, owner, ProjectRole.OWNER, ProjectFunctionalRole.PRODUCT_OWNER, null, true, 0);
        ReflectionTestUtils.setField(ownerMember, "id", UUID.randomUUID());
    }

    @Test
    void ownerCanAddMemberWithFunctionalRole() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER))
                .thenReturn(true);
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(memberRepository.findByProjectIdAndUserId(project.getId(), contributor.getId())).thenReturn(Optional.empty());
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.addOrUpdateMember(
                project.getId(),
                new ProjectMemberRequest(
                        contributor.getId(),
                        ProjectRole.CONTRIBUTOR,
                        ProjectFunctionalRole.DEVELOPER,
                        "Backend APIs",
                        false,
                        1
                ),
                ownerPrincipal
        );

        assertThat(response.functionalRole()).isEqualTo(ProjectFunctionalRole.DEVELOPER);
        assertThat(response.projectRole()).isEqualTo(ProjectRole.CONTRIBUTOR);
        assertThat(response.responsibility()).isEqualTo("Backend APIs");
    }

    @Test
    void cannotRemoveLastOwner() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER))
                .thenReturn(true);
        when(memberRepository.findByProjectIdAndUserId(project.getId(), owner.getId())).thenReturn(Optional.of(ownerMember));
        when(memberRepository.countByProjectIdAndProjectRole(project.getId(), ProjectRole.OWNER)).thenReturn(1L);

        assertThatThrownBy(() -> service.removeMember(project.getId(), owner.getId(), ownerPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project must retain at least one owner");
    }

    @Test
    void membersOnlyProjectHiddenFromOutsider() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> service.listMembers(project.getId(), outsiderPrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void countsPrimaryContactsAcrossMembersAndExternalContacts() {
        when(memberRepository.countByProjectIdAndPrimaryContactTrue(project.getId())).thenReturn(2L);
        when(externalContactRepository.countByProjectIdAndPrimaryContactTrueAndActiveTrue(project.getId())).thenReturn(1L);

        assertThat(service.countPrimaryContacts(project.getId())).isEqualTo(3);
    }

    @Test
    void listsMembersInDisplayOrder() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(project.getId(), owner.getId())).thenReturn(true);
        when(memberRepository.findByProjectIdOrdered(project.getId())).thenReturn(List.of(ownerMember));

        assertThat(service.listMembers(project.getId(), ownerPrincipal)).hasSize(1);
        verify(memberRepository).findByProjectIdOrdered(project.getId());
    }

    private User user(String employeeId, String email) {
        User user = new User(employeeId, email, employeeId, "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }
}
