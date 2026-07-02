package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearnTechnologyProjectLink;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.TechnologyCreateRequest;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.dto.TechnologyUpdateRequest;
import com.company.learninghub.learn.mapper.LearnTechnologyMapper;
import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearnTechnologyServiceTest {

    @Mock
    private LearnTechnologyRepository technologyRepository;

    @Mock
    private LearnTechnologyProjectLinkRepository projectLinkRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private LearnTechnologyService technologyService;

    private User adminUser;
    private User employeeUser;
    private AuthenticatedUser adminPrincipal;
    private AuthenticatedUser employeePrincipal;

    @BeforeEach
    void setUp() {
        technologyService = new LearnTechnologyService(
                technologyRepository,
                projectLinkRepository,
                projectRepository,
                userRepository,
                new LearnTechnologyMapper()
        );
        adminUser = user("ADMIN001", "admin@learninghub.local", RoleName.ADMIN);
        employeeUser = user("EMP001", "employee@learninghub.local", RoleName.EMPLOYEE);
        adminPrincipal = AuthenticatedUser.from(adminUser);
        employeePrincipal = AuthenticatedUser.from(employeeUser);
    }

    @Test
    void createPersistsDraftTechnology() {
        TechnologyCreateRequest request = new TechnologyCreateRequest(
                "Spring Boot",
                "Spring Boot",
                "Java framework",
                TechnologyCategory.LANGUAGES,
                TechnologyDifficulty.INTERMEDIATE
        );
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));
        when(technologyRepository.save(any(LearnTechnology.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TechnologyResponse response = technologyService.create(request, adminPrincipal);

        assertThat(response.name()).isEqualTo("Spring Boot");
        assertThat(response.status()).isEqualTo(TechnologyStatus.DRAFT);

        ArgumentCaptor<LearnTechnology> captor = ArgumentCaptor.forClass(LearnTechnology.class);
        verify(technologyRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(adminUser);
    }

    @Test
    void createRejectsDuplicateName() {
        TechnologyCreateRequest request = new TechnologyCreateRequest(
                "AWS",
                "AWS",
                null,
                TechnologyCategory.CLOUD,
                TechnologyDifficulty.BEGINNER
        );
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));
        when(technologyRepository.existsByNameIgnoreCase("AWS")).thenReturn(true);

        assertThatThrownBy(() -> technologyService.create(request, adminPrincipal))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void publishTransitionsDraftToPublished() {
        LearnTechnology technology = technology("Draft", TechnologyStatus.DRAFT);
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(projectLinkRepository.findByTechnologyIdOrderByProject_NameAsc(technologyId)).thenReturn(List.of());

        TechnologyResponse response = technologyService.publish(technologyId);

        assertThat(response.status()).isEqualTo(TechnologyStatus.PUBLISHED);
    }

    @Test
    void archiveTransitionsPublishedToArchived() {
        LearnTechnology technology = technology("Published", TechnologyStatus.PUBLISHED);
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(projectLinkRepository.findByTechnologyIdOrderByProject_NameAsc(technologyId)).thenReturn(List.of());

        TechnologyResponse response = technologyService.archive(technologyId);

        assertThat(response.status()).isEqualTo(TechnologyStatus.ARCHIVED);
    }

    @Test
    void employeeCannotViewDraftTechnology() {
        LearnTechnology technology = technology("Draft", TechnologyStatus.DRAFT);
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.of(technology));

        assertThatThrownBy(() -> technologyService.getById(technology.getId(), employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void employeeCanViewPublishedTechnology() {
        LearnTechnology technology = technology("Published", TechnologyStatus.PUBLISHED);
        when(technologyRepository.findById(technology.getId())).thenReturn(Optional.of(technology));
        when(projectLinkRepository.findByTechnologyIdOrderByProject_NameAsc(technology.getId())).thenReturn(List.of());

        TechnologyResponse response = technologyService.getById(technology.getId(), employeePrincipal);

        assertThat(response.status()).isEqualTo(TechnologyStatus.PUBLISHED);
    }

    @Test
    void addProjectLinkRejectsDuplicate() {
        LearnTechnology technology = technology("Published", TechnologyStatus.PUBLISHED);
        UUID technologyId = technology.getId();
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Payments", "Payments platform", ProjectAccessType.PUBLIC, adminUser);
        ReflectionTestUtils.setField(project, "id", projectId);

        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectLinkRepository.existsByTechnologyIdAndProjectId(technologyId, projectId)).thenReturn(true);

        assertThatThrownBy(() -> technologyService.addProjectLink(technologyId, projectId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage(LearnTechnologyService.DUPLICATE_PROJECT_LINK_MESSAGE);
    }

    @Test
    void addProjectLinkPersistsLink() {
        LearnTechnology technology = technology("Published", TechnologyStatus.PUBLISHED);
        UUID technologyId = technology.getId();
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Payments", "Payments platform", ProjectAccessType.PUBLIC, adminUser);
        ReflectionTestUtils.setField(project, "id", projectId);

        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectLinkRepository.existsByTechnologyIdAndProjectId(technologyId, projectId)).thenReturn(false);
        when(projectLinkRepository.findByTechnologyIdOrderByProject_NameAsc(technologyId))
                .thenReturn(List.of(new LearnTechnologyProjectLink(technology, project)));

        TechnologyResponse response = technologyService.addProjectLink(technologyId, projectId);

        verify(projectLinkRepository).save(any(LearnTechnologyProjectLink.class));
        assertThat(response.relatedProjects()).hasSize(1);
    }

    @Test
    void listEmployeeTechnologiesFiltersPublishedOnly() {
        Pageable pageable = PageRequest.of(0, 20);
        LearnTechnology technology = technology("Published", TechnologyStatus.PUBLISHED);
        when(technologyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(technology), pageable, 1));

        var page = technologyService.listEmployeeTechnologies(null, null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateAllowsFeaturedToggle() {
        LearnTechnology technology = technology("Published", TechnologyStatus.PUBLISHED);
        UUID technologyId = technology.getId();
        when(technologyRepository.findById(technologyId)).thenReturn(Optional.of(technology));
        when(projectLinkRepository.findByTechnologyIdOrderByProject_NameAsc(technologyId)).thenReturn(List.of());

        TechnologyResponse response = technologyService.update(
                technologyId,
                new TechnologyUpdateRequest(
                        "Published",
                        "Published",
                        "Updated description",
                        TechnologyCategory.CLOUD,
                        TechnologyDifficulty.ADVANCED,
                        true
                )
        );

        assertThat(response.featured()).isTrue();
        assertThat(response.description()).isEqualTo("Updated description");
    }

    private User user(String employeeId, String email, RoleName roleName) {
        User user = new User(employeeId, email, "Test User", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private LearnTechnology technology(String name, TechnologyStatus status) {
        LearnTechnology technology = new LearnTechnology(
                name,
                name,
                "Description",
                TechnologyCategory.CLOUD,
                TechnologyDifficulty.INTERMEDIATE,
                status,
                false,
                adminUser
        );
        ReflectionTestUtils.setField(technology, "id", UUID.randomUUID());
        return technology;
    }
}
