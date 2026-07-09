package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.projectknowledge.domain.ProjectStatus;
import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeAccessEvent;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeFolder;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeItem;
import com.company.learninghub.projectknowledge.domain.ProjectFunctionalRole;
import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.dto.CreateProjectLinkRequest;
import com.company.learninghub.projectknowledge.dto.CreateProjectRequest;
import com.company.learninghub.projectknowledge.dto.ProjectFolderRequest;
import com.company.learninghub.projectknowledge.dto.ProjectFolderResponse;
import com.company.learninghub.projectknowledge.dto.ProjectLinkAccessResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberRequest;
import com.company.learninghub.projectknowledge.dto.ProjectResponse;
import com.company.learninghub.projectknowledge.dto.UpdateProjectRequest;
import com.company.learninghub.projectknowledge.mapper.ProjectKnowledgeMapper;
import com.company.learninghub.projectknowledge.repository.ProjectEnvironmentRepository;
import com.company.learninghub.projectknowledge.repository.ProjectKnowledgeAccessEventRepository;
import com.company.learninghub.projectknowledge.repository.ProjectLinkedRepositoryRepository;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectKnowledgeServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository memberRepository;
    @Mock private ProjectKnowledgeFolderRepository folderRepository;
    @Mock private ProjectKnowledgeItemRepository itemRepository;
    @Mock private ProjectEnvironmentRepository environmentRepository;
    @Mock private ProjectLinkedRepositoryRepository linkedRepositoryRepository;
    @Mock private ProjectKnowledgeAccessEventRepository accessEventRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectKnowledgeStorageService storageService;
    @Mock private LearnTechnologyProjectLinkRepository projectLinkRepository;
    @Mock private ProjectTeamService teamService;

    private ProjectKnowledgeService service;
    private User admin;
    private User owner;
    private User contributor;
    private User viewer;
    private User outsider;
    private AuthenticatedUser adminPrincipal;
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
                environmentRepository,
                linkedRepositoryRepository,
                accessEventRepository,
                userRepository,
                storageService,
                storageProperties,
                new ProjectKnowledgeMapper(),
                projectLinkRepository,
                teamService
        );
        admin = user("ADMIN001", "admin@example.com", RoleName.ADMIN);
        owner = user("OWNER001", "owner@example.com", RoleName.EMPLOYEE);
        contributor = user("CONTRIB001", "contributor@example.com", RoleName.EMPLOYEE);
        viewer = user("VIEWER001", "viewer@example.com", RoleName.EMPLOYEE);
        outsider = user("OUT001", "outsider@example.com", RoleName.EMPLOYEE);
        adminPrincipal = AuthenticatedUser.from(admin);
        ownerPrincipal = AuthenticatedUser.from(owner);
        contributorPrincipal = AuthenticatedUser.from(contributor);
        viewerPrincipal = AuthenticatedUser.from(viewer);
        project = project("Payments", ProjectAccessType.MEMBERS_ONLY, owner);
        rootFolder = folder(project, "Architecture", null, owner);
        lenient().when(environmentRepository.countByProjectIdAndActiveTrue(any())).thenReturn(0L);
        lenient().when(linkedRepositoryRepository.countByProjectIdAndActiveTrue(any())).thenReturn(0L);
        lenient().when(itemRepository.countByFolderId(any())).thenReturn(0L);
    }

    @Test
    void adminCreateProjectCreatesOwnerMembership() {
        when(projectRepository.existsByNameIgnoreCase("Payments")).thenReturn(false);
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubEnrichmentForProject();

        ProjectResponse response = service.createProject(
                new CreateProjectRequest(" Payments ", " Core payments ", ProjectAccessType.MEMBERS_ONLY),
                adminPrincipal
        );

        assertThat(response.name()).isEqualTo("Payments");
        assertThat(response.status()).isEqualTo(ProjectStatus.ACTIVE);
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getProjectRole()).isEqualTo(ProjectRole.OWNER);
        assertThat(memberCaptor.getValue().getFunctionalRole()).isEqualTo(ProjectFunctionalRole.PRODUCT_OWNER);
        assertThat(memberCaptor.getValue().isPrimaryContact()).isTrue();
        assertThat(memberCaptor.getValue().getUser()).isEqualTo(admin);
    }

    @Test
    void ownerCanUpdateProjectAndOutsiderCannotReadMembersOnlyProject() {
        Project privateProject = project("Private Payments", ProjectAccessType.MEMBERS_ONLY, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        stubEnrichmentForProject();

        ProjectResponse response = service.updateProject(
                project.getId(),
                new UpdateProjectRequest("Payments Modernization", "Updated", ProjectAccessType.PUBLIC, ProjectStatus.ON_HOLD),
                ownerPrincipal
        );

        assertThat(response.name()).isEqualTo("Payments Modernization");
        assertThat(response.status()).isEqualTo(ProjectStatus.ON_HOLD);

        when(projectRepository.findById(privateProject.getId())).thenReturn(Optional.of(privateProject));
        assertThatThrownBy(() -> service.getProject(privateProject.getId(), AuthenticatedUser.from(outsider)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project was not found");
    }

    @Test
    void ownerManagesMembersButViewerCannot() {
        ProjectMemberRequest contributorRequest = new ProjectMemberRequest(
                contributor.getId(),
                ProjectRole.CONTRIBUTOR,
                ProjectFunctionalRole.DEVELOPER,
                null,
                false,
                0
        );
        ProjectMemberRequest outsiderRequest = new ProjectMemberRequest(
                outsider.getId(),
                ProjectRole.VIEWER,
                ProjectFunctionalRole.OTHER,
                null,
                false,
                0
        );
        when(teamService.addOrUpdateMember(project.getId(), contributorRequest, ownerPrincipal))
                .thenReturn(new com.company.learninghub.projectknowledge.dto.ProjectMemberResponse(
                        UUID.randomUUID(),
                        project.getId(),
                        new com.company.learninghub.projectknowledge.dto.ProjectUserResponse(
                                contributor.getId(),
                                contributor.getEmployeeId(),
                                contributor.getFullName(),
                                contributor.getEmail()
                        ),
                        ProjectRole.CONTRIBUTOR,
                        ProjectFunctionalRole.DEVELOPER,
                        null,
                        false,
                        0,
                        null,
                        null
                ));
        when(teamService.addOrUpdateMember(project.getId(), outsiderRequest, viewerPrincipal))
                .thenThrow(new IllegalArgumentException("Project OWNER role is required"));

        assertThat(service.addOrUpdateMember(project.getId(), contributorRequest, ownerPrincipal).projectRole())
                .isEqualTo(ProjectRole.CONTRIBUTOR);

        assertThatThrownBy(() -> service.addOrUpdateMember(project.getId(), outsiderRequest, viewerPrincipal))
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
        when(projectRepository.search(eq("%pay%"), eq(ProjectAccessType.PUBLIC), eq(null), eq(false), eq(false), eq(owner.getId()), eq(false), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(project), invocation.getArgument(7), 1));
        stubEnrichmentForProject();

        service.searchProjects(" pay ", ProjectAccessType.PUBLIC, null, false, false, pageable, ownerPrincipal);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(projectRepository).search(eq("%pay%"), eq(ProjectAccessType.PUBLIC), eq(null), eq(false), eq(false), eq(owner.getId()), eq(false), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void nonAdminSearchCannotIncludeArchivedProjects() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(projectRepository.search(eq(null), eq(null), eq(null), eq(false), eq(false), eq(owner.getId()), eq(false), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(7), 0));

        service.searchProjects(null, null, null, false, true, pageable, ownerPrincipal);

        verify(projectRepository).search(eq(null), eq(null), eq(null), eq(false), eq(false), eq(owner.getId()), eq(false), any(Pageable.class));
    }

    @Test
    void assignedSearchRequestsMembershipOnlyProjects() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(projectRepository.search(eq(null), eq(null), eq(null), eq(true), eq(false), eq(owner.getId()), eq(false), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(7), 0));

        service.searchProjects(null, null, null, true, false, pageable, ownerPrincipal);

        verify(projectRepository).search(eq(null), eq(null), eq(null), eq(true), eq(false), eq(owner.getId()), eq(false), any(Pageable.class));
    }

    @Test
    void archivedProjectIsHiddenFromEmployees() {
        Project archivedProject = project("Archived", ProjectAccessType.PUBLIC, owner);
        ReflectionTestUtils.setField(archivedProject, "status", ProjectStatus.ARCHIVED);
        ReflectionTestUtils.setField(archivedProject, "archived", true);
        when(projectRepository.findById(archivedProject.getId())).thenReturn(Optional.of(archivedProject));

        assertThatThrownBy(() -> service.getProject(archivedProject.getId(), ownerPrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createLevel1FolderSucceeds() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.existsSiblingWithName(project.getId(), "QA", null, null)).thenReturn(false);
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(folderRepository.save(any(ProjectKnowledgeFolder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectFolderResponse response = service.createFolder(
                project.getId(),
                new ProjectFolderRequest("QA", "Quality area", null),
                contributorPrincipal
        );

        assertThat(response.name()).isEqualTo("QA");
    }

    @Test
    void createLevel2FolderSucceeds() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsSiblingWithName(project.getId(), "Automation", rootFolder.getId(), null)).thenReturn(false);
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(folderRepository.save(any(ProjectKnowledgeFolder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectFolderResponse response = service.createFolder(
                project.getId(),
                new ProjectFolderRequest("Automation", null, rootFolder.getId()),
                contributorPrincipal
        );

        assertThat(response.name()).isEqualTo("Automation");
    }

    @Test
    void createLevel3FolderSucceeds() {
        ProjectKnowledgeFolder level2Folder = folder(project, "Automation", rootFolder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.findById(level2Folder.getId())).thenReturn(Optional.of(level2Folder));
        when(folderRepository.existsSiblingWithName(project.getId(), "API Testing", level2Folder.getId(), null)).thenReturn(false);
        when(userRepository.findById(contributor.getId())).thenReturn(Optional.of(contributor));
        when(folderRepository.save(any(ProjectKnowledgeFolder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectFolderResponse response = service.createFolder(
                project.getId(),
                new ProjectFolderRequest("API Testing", null, level2Folder.getId()),
                contributorPrincipal
        );

        assertThat(response.name()).isEqualTo("API Testing");
    }

    @Test
    void createLevel4FolderFails() {
        ProjectKnowledgeFolder level2Folder = folder(project, "Automation", rootFolder, owner);
        ProjectKnowledgeFolder level3Folder = folder(project, "API Testing", level2Folder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.findById(level3Folder.getId())).thenReturn(Optional.of(level3Folder));

        assertThatThrownBy(() -> service.createFolder(
                project.getId(),
                new ProjectFolderRequest("Postman", null, level3Folder.getId()),
                contributorPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Knowledge Base folders support a maximum depth of 3 levels.");
    }

    @Test
    void updateFolderWithinThreeLevelsSucceeds() {
        ProjectKnowledgeFolder level2Folder = folder(project, "Automation", rootFolder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), contributor.getId(), ProjectRole.CONTRIBUTOR)).thenReturn(true);
        when(folderRepository.findById(level2Folder.getId())).thenReturn(Optional.of(level2Folder));
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsSiblingWithName(project.getId(), "Automation Suite", rootFolder.getId(), level2Folder.getId())).thenReturn(false);
        when(folderRepository.findByProjectId(project.getId())).thenReturn(List.of(rootFolder, level2Folder));

        ProjectFolderResponse response = service.updateFolder(
                project.getId(),
                level2Folder.getId(),
                new ProjectFolderRequest("Automation Suite", "Updated", rootFolder.getId()),
                contributorPrincipal
        );

        assertThat(response.name()).isEqualTo("Automation Suite");
    }

    @Test
    void movingFolderSubtreeBeyondMaximumDepthFails() {
        ProjectKnowledgeFolder qa = folder(project, "QA", null, owner);
        ProjectKnowledgeFolder automation = folder(project, "Automation", qa, owner);
        ProjectKnowledgeFolder apiTesting = folder(project, "API Testing", automation, owner);
        ProjectKnowledgeFolder engineering = folder(project, "Engineering", null, owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(folderRepository.findById(qa.getId())).thenReturn(Optional.of(qa));
        when(folderRepository.findById(engineering.getId())).thenReturn(Optional.of(engineering));
        when(folderRepository.findByProjectId(project.getId())).thenReturn(List.of(qa, automation, apiTesting, engineering));

        assertThatThrownBy(() -> service.updateFolder(
                project.getId(),
                qa.getId(),
                new ProjectFolderRequest("QA", null, engineering.getId()),
                ownerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Knowledge Base folders support a maximum depth of 3 levels.");
    }

    @Test
    void movingFolderUnderItselfFails() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));

        assertThatThrownBy(() -> service.updateFolder(
                project.getId(),
                rootFolder.getId(),
                new ProjectFolderRequest(rootFolder.getName(), null, rootFolder.getId()),
                ownerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder cannot be its own parent");
    }

    @Test
    void movingFolderUnderDescendantFails() {
        ProjectKnowledgeFolder level2Folder = folder(project, "Automation", rootFolder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.findById(level2Folder.getId())).thenReturn(Optional.of(level2Folder));

        assertThatThrownBy(() -> service.updateFolder(
                project.getId(),
                rootFolder.getId(),
                new ProjectFolderRequest(rootFolder.getName(), null, level2Folder.getId()),
                ownerPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder cannot be moved under its descendant");
    }

    @Test
    void existingDeepHierarchyRemainsReadable() {
        ProjectKnowledgeFolder level2Folder = folder(project, "Automation", rootFolder, owner);
        ProjectKnowledgeFolder level3Folder = folder(project, "API Testing", level2Folder, owner);
        ProjectKnowledgeFolder level4Folder = folder(project, "Legacy Deep", level3Folder, owner);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(project.getId(), viewer.getId())).thenReturn(true);
        when(folderRepository.findById(level4Folder.getId())).thenReturn(Optional.of(level4Folder));
        when(folderRepository.countByParentId(level4Folder.getId())).thenReturn(0L);
        when(itemRepository.countByFolderId(level4Folder.getId())).thenReturn(0L);

        ProjectFolderResponse response = service.getFolder(project.getId(), level4Folder.getId(), viewerPrincipal);

        assertThat(response.name()).isEqualTo("Legacy Deep");
    }

    @Test
    void getFolderReturnsCountsForViewer() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(project.getId(), viewer.getId())).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.countByParentId(rootFolder.getId())).thenReturn(2L);
        when(itemRepository.countByFolderId(rootFolder.getId())).thenReturn(4L);

        ProjectFolderResponse response = service.getFolder(project.getId(), rootFolder.getId(), viewerPrincipal);

        assertThat(response.childFolderCount()).isEqualTo(2L);
        assertThat(response.itemCount()).isEqualTo(4L);
    }

    @Test
    void searchItemsUsesPreparedSearchPattern() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(project.getId(), viewer.getId())).thenReturn(true);
        when(itemRepository.search(eq(project.getId()), eq(null), eq(null), eq(null), eq("%postman%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.searchItems(project.getId(), null, null, null, " Postman ", pageable, viewerPrincipal);

        verify(itemRepository).search(eq(project.getId()), eq(null), eq(null), eq(null), eq("%postman%"), any(Pageable.class));
    }

    @Test
    void ownerCannotDeleteNonEmptyFolder() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), owner.getId(), ProjectRole.OWNER)).thenReturn(true);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(itemRepository.existsByFolderId(rootFolder.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.deleteFolder(project.getId(), rootFolder.getId(), ownerPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder must be empty before deletion");
    }

    private void stubEnrichmentForProject() {
        when(memberRepository.findOwnersByProjectIds(any(), eq(ProjectRole.OWNER))).thenReturn(Collections.emptyList());
        when(memberRepository.countByProjectId(any())).thenReturn(0L);
        when(memberRepository.findByProjectIdInAndUserId(any(), any())).thenReturn(Collections.emptyList());
        when(projectLinkRepository.findPublishedTechnologiesByProjectIds(any(), any())).thenReturn(Collections.emptyList());
        lenient().when(teamService.countPrimaryContacts(any())).thenReturn(0);
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

