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
import com.company.learninghub.projectknowledge.dto.ProjectFolderResponse;
import com.company.learninghub.projectknowledge.dto.ProjectKnowledgeItemResponse;
import com.company.learninghub.projectknowledge.dto.ProjectLinkAccessResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberRequest;
import com.company.learninghub.projectknowledge.dto.ProjectMemberResponse;
import com.company.learninghub.projectknowledge.dto.ProjectResponse;
import com.company.learninghub.projectknowledge.dto.UpdateProjectItemRequest;
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
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProjectKnowledgeService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectKnowledgeFolderRepository folderRepository;
    private final ProjectKnowledgeItemRepository itemRepository;
    private final ProjectKnowledgeAccessEventRepository accessEventRepository;
    private final UserRepository userRepository;
    private final ProjectKnowledgeStorageService storageService;
    private final StorageProperties storageProperties;
    private final ProjectKnowledgeMapper mapper;

    public ProjectKnowledgeService(
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
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.folderRepository = folderRepository;
        this.itemRepository = itemRepository;
        this.accessEventRepository = accessEventRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
        this.mapper = mapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, AuthenticatedUser principal) {
        String name = normalizeRequired(request.name(), "Project name is required");
        if (projectRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A project with this name already exists");
        }
        User user = findUser(principal.getId());
        Project project = projectRepository.save(new Project(
                name,
                normalizeOptional(request.description()),
                request.accessType(),
                user
        ));
        memberRepository.save(new ProjectMember(project, user, ProjectRole.OWNER));
        return mapper.toProjectResponse(project);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        project.updateDetails(
                normalizeRequired(request.name(), "Project name is required"),
                normalizeOptional(request.description()),
                request.accessType()
        );
        return mapper.toProjectResponse(project);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectResponse archiveProject(UUID projectId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        project.archive();
        return mapper.toProjectResponse(project);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(String search, ProjectAccessType accessType, boolean includeArchived, Pageable pageable, AuthenticatedUser principal) {
        return projectRepository.search(
                        normalizeSearch(search),
                        accessType,
                        includeArchived,
                        principal.getId(),
                        isAdmin(principal),
                        normalizeProjectPageable(pageable)
                )
                .map(mapper::toProjectResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        return mapper.toProjectResponse(project);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectMemberResponse addOrUpdateMember(UUID projectId, ProjectMemberRequest request, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        User user = findUser(request.userId());
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseGet(() -> new ProjectMember(project, user, request.projectRole()));
        member.updateRole(request.projectRole());
        return mapper.toMemberResponse(memberRepository.save(member));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID projectId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        return memberRepository.findByProjectId(projectId).stream()
                .map(mapper::toMemberResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void removeMember(UUID projectId, UUID userId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member was not found"));
        memberRepository.delete(member);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectFolderResponse createFolder(UUID projectId, ProjectFolderRequest request, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireContributor(project, principal);
        ProjectKnowledgeFolder parent = resolveFolder(project, request.parentId());
        String name = normalizeRequired(request.name(), "Folder name is required");
        ensureUniqueFolderName(projectId, name, request.parentId(), null);
        ProjectKnowledgeFolder folder = new ProjectKnowledgeFolder(project, name, normalizeOptional(request.description()), parent, findUser(principal.getId()));
        return mapper.toFolderResponse(folderRepository.save(folder));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectFolderResponse updateFolder(UUID projectId, UUID folderId, ProjectFolderRequest request, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireContributor(project, principal);
        if (folderId.equals(request.parentId())) {
            throw new IllegalArgumentException("Folder cannot be its own parent");
        }
        ProjectKnowledgeFolder folder = findFolder(folderId);
        ensureFolderBelongsToProject(folder, projectId);
        ProjectKnowledgeFolder parent = resolveFolder(project, request.parentId());
        ensureNotDescendant(folderId, parent);
        String name = normalizeRequired(request.name(), "Folder name is required");
        ensureUniqueFolderName(projectId, name, request.parentId(), folderId);
        folder.updateDetails(name, normalizeOptional(request.description()), parent);
        return mapper.toFolderResponse(folder);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void deleteFolder(UUID projectId, UUID folderId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        ProjectKnowledgeFolder folder = findFolder(folderId);
        ensureFolderBelongsToProject(folder, projectId);
        if (folderRepository.existsByParentId(folderId) || itemRepository.existsByFolderId(folderId)) {
            throw new IllegalArgumentException("Folder must be empty before deletion");
        }
        folderRepository.delete(folder);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<ProjectFolderResponse> listFolders(UUID projectId, UUID parentId, Pageable pageable, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        return folderRepository.findByProjectAndParent(projectId, parentId, normalizeFolderPageable(pageable))
                .map(mapper::toFolderResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectKnowledgeItemResponse uploadFile(UUID projectId, UUID folderId, String title, String description, KnowledgeCategory category, MultipartFile file, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireContributor(project, principal);
        validateFile(file);
        ProjectKnowledgeFolder folder = resolveFolder(project, folderId);
        User user = findUser(principal.getId());
        StoredFile storedFile = storageService.store(file);
        try {
            ProjectKnowledgeItem item = ProjectKnowledgeItem.fileItem(
                    project,
                    folder,
                    normalizeRequired(title, "Knowledge item title is required"),
                    normalizeOptional(description),
                    category,
                    storedFile.storageProvider(),
                    storedFile.storageKey(),
                    storedFile.originalFilename(),
                    storedFile.contentType(),
                    storedFile.fileSizeBytes(),
                    user
            );
            return mapper.toItemResponse(itemRepository.save(item));
        } catch (RuntimeException ex) {
            storageService.deleteQuietly(storedFile.storageKey());
            throw ex;
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectKnowledgeItemResponse createLink(UUID projectId, CreateProjectLinkRequest request, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireContributor(project, principal);
        ProjectKnowledgeFolder folder = resolveFolder(project, request.folderId());
        User user = findUser(principal.getId());
        ProjectKnowledgeItem item = ProjectKnowledgeItem.linkItem(
                project,
                folder,
                normalizeRequired(request.title(), "Knowledge item title is required"),
                normalizeOptional(request.description()),
                request.category(),
                normalizeUrl(request.externalUrl()),
                user
        );
        return mapper.toItemResponse(itemRepository.save(item));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectKnowledgeItemResponse updateItem(UUID projectId, UUID itemId, UpdateProjectItemRequest request, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireContributor(project, principal);
        ProjectKnowledgeItem item = findItem(itemId);
        ensureItemBelongsToProject(item, projectId);
        ProjectKnowledgeFolder folder = resolveFolder(project, request.folderId());
        if (item.isLink()) {
            item.updateLink(normalizeRequired(request.title(), "Knowledge item title is required"), normalizeOptional(request.description()), folder, request.category(), normalizeUrl(request.externalUrl()));
        } else {
            item.updateMetadata(normalizeRequired(request.title(), "Knowledge item title is required"), normalizeOptional(request.description()), folder, request.category());
        }
        return mapper.toItemResponse(item);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void deleteItem(UUID projectId, UUID itemId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        ProjectKnowledgeItem item = findItem(itemId);
        ensureItemBelongsToProject(item, projectId);
        itemRepository.delete(item);
        if (item.isFile()) {
            storageService.deleteQuietly(item.getStorageKey());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<ProjectKnowledgeItemResponse> searchItems(UUID projectId, UUID folderId, KnowledgeCategory category, String search, Pageable pageable, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        return itemRepository.search(projectId, folderId, category, normalizeSearch(search), normalizeItemPageable(pageable))
                .map(mapper::toItemResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public ProjectKnowledgeItemResponse getItem(UUID projectId, UUID itemId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        ProjectKnowledgeItem item = findItem(itemId);
        ensureItemBelongsToProject(item, projectId);
        return mapper.toItemResponse(item);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public Resource downloadFile(UUID projectId, UUID itemId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        ProjectKnowledgeItem item = findItem(itemId);
        ensureItemBelongsToProject(item, projectId);
        if (!item.isFile()) {
            throw new IllegalArgumentException("Project knowledge item is not a downloadable file");
        }
        recordAccess(item, principal);
        return storageService.loadAsResource(item.getStorageKey());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectLinkAccessResponse accessLink(UUID projectId, UUID itemId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        ProjectKnowledgeItem item = findItem(itemId);
        ensureItemBelongsToProject(item, projectId);
        if (!item.isLink()) {
            throw new IllegalArgumentException("Project knowledge item is not a link");
        }
        recordAccess(item, principal);
        return new ProjectLinkAccessResponse(item.getId(), item.getExternalUrl(), item.getAccessCount());
    }

    private void recordAccess(ProjectKnowledgeItem item, AuthenticatedUser principal) {
        item.incrementAccessCount();
        accessEventRepository.save(new ProjectKnowledgeAccessEvent(item, findUser(principal.getId())));
    }

    private void requireReadAccess(Project project, AuthenticatedUser principal) {
        if (project.isArchived() && !isAdmin(principal)) {
            throw new ResourceNotFoundException("Project was not found");
        }
        if (isAdmin(principal) || project.isPublic() || memberRepository.existsByProjectIdAndUserId(project.getId(), principal.getId())) {
            return;
        }
        throw new ResourceNotFoundException("Project was not found");
    }

    private void requireOwner(Project project, AuthenticatedUser principal) {
        if (isAdmin(principal) || memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), principal.getId(), ProjectRole.OWNER)) {
            return;
        }
        throw new IllegalArgumentException("Project OWNER role is required");
    }

    private void requireContributor(Project project, AuthenticatedUser principal) {
        if (isAdmin(principal)
                || memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), principal.getId(), ProjectRole.OWNER)
                || memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), principal.getId(), ProjectRole.CONTRIBUTOR)) {
            return;
        }
        throw new IllegalArgumentException("Project OWNER or CONTRIBUTOR role is required");
    }

    private boolean isAdmin(AuthenticatedUser principal) {
        return principal.getRoleNames().contains(RoleName.ADMIN);
    }

    private Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found"));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private ProjectKnowledgeFolder findFolder(UUID folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Project knowledge folder was not found"));
    }

    private ProjectKnowledgeItem findItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Project knowledge item was not found"));
    }

    private ProjectKnowledgeFolder resolveFolder(Project project, UUID folderId) {
        if (folderId == null) {
            return null;
        }
        ProjectKnowledgeFolder folder = findFolder(folderId);
        ensureFolderBelongsToProject(folder, project.getId());
        return folder;
    }

    private void ensureFolderBelongsToProject(ProjectKnowledgeFolder folder, UUID projectId) {
        if (!folder.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Project knowledge folder was not found");
        }
    }

    private void ensureItemBelongsToProject(ProjectKnowledgeItem item, UUID projectId) {
        if (!item.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Project knowledge item was not found");
        }
    }

    private void ensureUniqueFolderName(UUID projectId, String name, UUID parentId, UUID excludeId) {
        if (folderRepository.existsSiblingWithName(projectId, name, parentId, excludeId)) {
            throw new IllegalArgumentException("A folder with this name already exists at this level");
        }
    }

    private void ensureNotDescendant(UUID folderId, ProjectKnowledgeFolder proposedParent) {
        ProjectKnowledgeFolder current = proposedParent;
        while (current != null) {
            if (folderId.equals(current.getId())) {
                throw new IllegalArgumentException("Folder cannot be moved under its descendant");
            }
            current = current.getParent();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Project knowledge file is required");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("Project knowledge file exceeds the configured maximum size");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeSearch(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeUrl(String value) {
        String url = normalizeRequired(value, "External URL is required");
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException("External URL must start with http:// or https://");
        }
        return url;
    }

    private Pageable normalizeProjectPageable(Pageable pageable) {
        return normalizePageable(pageable, Set.of("id", "name", "accessType", "archived", "createdAt", "updatedAt", "createdAtUtc", "updatedAtUtc"));
    }

    private Pageable normalizeFolderPageable(Pageable pageable) {
        return normalizePageable(pageable, Set.of("id", "name", "createdAt", "updatedAt", "createdAtUtc", "updatedAtUtc"));
    }

    private Pageable normalizeItemPageable(Pageable pageable) {
        return normalizePageable(pageable, Set.of("id", "title", "category", "sourceType", "accessCount", "createdAt", "updatedAt", "createdAtUtc", "updatedAtUtc"));
    }

    private Pageable normalizePageable(Pageable pageable, Set<String> allowedProperties) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(order -> toRepositorySortOrder(order, allowedProperties))
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Sort.Order toRepositorySortOrder(Sort.Order order, Set<String> allowedProperties) {
        if (!allowedProperties.contains(order.getProperty())) {
            throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        }
        String property = switch (order.getProperty()) {
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            default -> order.getProperty();
        };
        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}

