package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeAccessEvent;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeFolder;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeItem;
import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.dto.CreateProjectLinkRequest;
import com.company.learninghub.projectknowledge.dto.CreateProjectRequest;
import com.company.learninghub.projectknowledge.dto.ProjectFolderRequest;
import com.company.learninghub.projectknowledge.dto.ProjectLinkAccessResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberRequest;
import com.company.learninghub.projectknowledge.dto.ProjectResponse;
import com.company.learninghub.projectknowledge.dto.UpdateProjectRequest;
import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeAccessEventRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeFolderRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeItemRepository;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.storage.ProjectKnowledgeStorageService;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.storage.StoredFile;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectKnowledgeServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository memberRepository;
    @Mock private ProjectKnowledgeFolderRepository folderRepository;
    @Mock private ProjectKnowledgeItemRepository itemRepository;
    @Mock private ProjectKnowledgeAccessEventRepository accessEventRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectKnowledgeStorageService storageService;

    private ProjectKnowledgeService service;
    private User owner;
    private User contributor;
    private User viewer;
    private User outsider;
    private AuthenticatedUser ownerPrincipal;
    private AuthenticatedUser contributorPrincipal;
    private AuthenticatedUser viewerPrincipal;
    private Project project;
    private ProjectKnowledgeFolder rootFolder;

    @BeforeEach
    void setUp() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setMaxFileSizeBytes(1024);
        service = new ProjectKnowledgeService(
                projectRepository,
                memberRepository,
                folderRepository,
                itemRepository,
                accessEventRepository,
                userRepository,
                storageService,
                storageProperties,
                new ProjectKnowledgeMapper()
        );
        owner = user("OWNER001", "owner@example.com", RoleName.EMPLOYEE);
        contributor = user("CONTRIB001", "contributor@example.com", RoleName.EMPLOYEE);
        viewer = user("VIEWER001", "viewer@example.com", RoleName.EMPLOYEE);
        outsider = user("OUT001", "outsider@example.com", RoleName.EMPLOYEE);
        ownerPrincipal = AuthenticatedUser.from(owner);
        contributorPrincipal = AuthenticatedUser.from(contributor);
        viewerPrincipal = AuthenticatedUser.from(viewer);
        project = project("Payments", ProjectAccessType.MEMBERS_ONLY, owner);
        rootFolder = folder(project, "Architecture", null, owner);
    }

    @Test
    void createProjectCreatesOwnerMembership() {
        when(projectRepository.existsByNameIgnoreCase("Payments")).thenReturn(false);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = service.createProject(
                new CreateProjectRequest(" Payments ", " Core payments ", ProjectAccessType.MEMBERS_ONLY),
                ownerPrincipal
        );

        assertThat(response.name()).isEqualTo("Payments");
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getProjectRole()).isEqualTo(ProjectRole.OWNER);
        assertThat(memberCaptor.getValue().getUser()).isEqualTo(owner);
    }

    @Test
    void ownerCanUpdateProjectAndOutsiderCannotReadMembersOnlyProject() {
        Project privateProject = project("Private Payments", ProjectAccessType.MEMBERS_ONLY, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);

        ProjectResponse response = service.updateProject(
                project.getId(),
                new UpdateProjectRequest("Payments Modernization", "Updated", ProjectAccessType.PUBLIC),
                ownerPrincipal
        );

        assertThat(response.name()).isEqualTo("Payments Modernization");

        when(projectRepository.findById(privateProject.getId())).thenReturn(Optional.of(privateProject));
        assertThatThrownBy(() -> service.getProject(privateProject.getId(), AuthenticatedUser.from(outsider)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project was not found");
    }

    @Test
    void ownerManagesMembersButViewerCannot() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(memberRepository.findByProjectIdAndUserId(project.getId(), contributor.getId())).thenReturn(Optional.empty());
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.addOrUpdateMember(
                project.getId(),
                new ProjectMemberRequest(contributor.getId(), ProjectRole.CONTRIBUTOR),
                ownerPrincipal
        ).projectRole()).isEqualTo(ProjectRole.CONTRIBUTOR);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> service.addOrUpdateMember(
                project.getId(),
                new ProjectMemberRequest(outsider.getId(), ProjectRole.VIEWER),
                viewerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project OWNER role is required");
    }

    @Test
    void contributorCanCreateFolderButCannotDeleteFolder() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.OWNER)).thenReturn(false);
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.existsSiblingWithName(project.getId(), "Architecture", null, null)).thenReturn(false);
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(folderRepository.save(any(ProjectKnowledgeFolder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.createFolder(
                project.getId(),
                new ProjectFolderRequest("Architecture", null, null),
                contributorPrincipal
        ).name()).isEqualTo("Architecture");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> service.deleteFolder(project.getId(), rootFolder.getId(), contributorPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project OWNER role is required");
    }

    @Test
    void folderCannotBeMovedUnderDescendant() {
        ProjectKnowledgeFolder child = folder(project, "Child", rootFolder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.findById(child.getId())).thenReturn(Optional.of(child));

        assertThatThrownBy(() -> service.updateFolder(
                project.getId(),
                rootFolder.getId(),
                new ProjectFolderRequest("Architecture", null, child.getId()),
                ownerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder cannot be moved under its descendant");
    }

    @Test
    void contributorUploadsFileAndStoredFileIsCleanedOnPersistenceFailure() {
        MockMultipartFile file = new MockMultipartFile("file", "arch.pdf", "application/pdf", "pdf".getBytes());
        StoredFile storedFile = new StoredFile("LOCAL", "project-knowledge/orphan.pdf", "arch.pdf", "application/pdf", file.getSize());
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.OWNER)).thenReturn(false);
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(storageService.store(file)).thenReturn(storedFile);
        when(itemRepository.save(any(ProjectKnowledgeItem.class))).thenThrow(new IllegalStateException("db unavailable"));

        assertThatThrownBy(() -> service.uploadFile(
                project.getId(),
                rootFolder.getId(),
                "Architecture",
                null,
                KnowledgeCategory.ARCHITECTURE_DOCUMENTS,
                file,
                contributorPrincipal
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("db unavailable");

        verify(storageService).deleteQuietly("project-knowledge/orphan.pdf");
    }

    @Test
    void viewerCanReadAndTrackLinkButCannotUpdateContent() {
        ProjectKnowledgeItem item = linkItem(project, rootFolder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(project.getId(), viewer.getId())).thenReturn(true);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(viewer.getId())).thenReturn(Optional.of(viewer));

        ProjectLinkAccessResponse response = service.accessLink(project.getId(), item.getId(), viewerPrincipal);

        assertThat(response.accessCount()).isEqualTo(1);
        verify(accessEventRepository).save(any(ProjectKnowledgeAccessEvent.class));

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> service.updateItem(
                project.getId(),
                item.getId(),
                new com.company.learninghub.projectknowledge.dto.UpdateProjectItemRequest(null, "Updated", null, KnowledgeCategory.EXTERNAL_LINKS, "https://example.com"),
                viewerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project OWNER or CONTRIBUTOR role is required");
    }

    @Test
    void searchProjectsPassesAccessContextAndSortTranslation() {
        PageRequest pageable = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("createdAtUtc")));
        when(projectRepository.search(eq("pay"), eq(ProjectAccessType.PUBLIC), eq(false), eq(owner.getId()), eq(false), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(project), invocation.getArgument(5), 1));

        service.searchProjects(" pay ", ProjectAccessType.PUBLIC, false, pageable, ownerPrincipal);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(projectRepository).search(eq("pay"), eq(ProjectAccessType.PUBLIC), eq(false), eq(owner.getId()), eq(false), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void nonAdminSearchCannotIncludeArchivedProjects() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(projectRepository.search(eq(null), eq(null), eq(false), eq(owner.getId()), eq(false), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(5), 0));

        service.searchProjects(null, null, true, pageable, ownerPrincipal);

        verify(projectRepository).search(eq(null), eq(null), eq(false), eq(owner.getId()), eq(false), any(Pageable.class));
    }

    @Test
    void downloadFileTracksAccessAndLoadsResource() {
        ProjectKnowledgeItem item = fileItem(project, rootFolder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(project.getId(), owner.getId())).thenReturn(true);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(storageService.loadAsResource(item.getStorageKey())).thenReturn(new ByteArrayResource("content".getBytes()));

        service.downloadFile(project.getId(), item.getId(), ownerPrincipal);

        assertThat(item.getAccessCount()).isEqualTo(1);
        verify(accessEventRepository).save(any(ProjectKnowledgeAccessEvent.class));
        verify(storageService).loadAsResource(item.getStorageKey());
    }

    private User user(String employeeId, String email, RoleName roleName) {
        User user = new User(employeeId, email, employeeId, "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private Project project(String name, ProjectAccessType accessType, User createdBy) {
        Project project = new Project(name, name + " description", accessType, createdBy);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        return project;
    }

    private ProjectKnowledgeFolder folder(Project project, String name, ProjectKnowledgeFolder parent, User createdBy) {
        ProjectKnowledgeFolder folder = new ProjectKnowledgeFolder(project, name, null, parent, createdBy);
        ReflectionTestUtils.setField(folder, "id", UUID.randomUUID());
        return folder;
    }

    private ProjectKnowledgeItem linkItem(Project project, ProjectKnowledgeFolder folder, User uploadedBy) {
        ProjectKnowledgeItem item = ProjectKnowledgeItem.linkItem(project, folder, "Docs", null, KnowledgeCategory.EXTERNAL_LINKS, "https://example.com", uploadedBy);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }

    private ProjectKnowledgeItem fileItem(Project project, ProjectKnowledgeFolder folder, User uploadedBy) {
        ProjectKnowledgeItem item = ProjectKnowledgeItem.fileItem(project, folder, "Architecture", null, KnowledgeCategory.ARCHITECTURE_DOCUMENTS,
                "LOCAL", "project-knowledge/file.pdf", "file.pdf", "application/pdf", 10, uploadedBy);
        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
        return item;
    }
}

