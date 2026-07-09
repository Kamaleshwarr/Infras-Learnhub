package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectLinkedRepository;
import com.company.learninghub.projectknowledge.domain.RepositoryProvider;
import com.company.learninghub.projectknowledge.domain.RepositoryType;
import com.company.learninghub.projectknowledge.dto.ProjectLinkedRepositoryRequest;
import com.company.learninghub.projectknowledge.dto.ProjectLinkedRepositoryResponse;
import com.company.learninghub.projectknowledge.mapper.ProjectOperationalMapper;
import com.company.learninghub.projectknowledge.repository.ProjectLinkedRepositoryRepository;
import com.company.learninghub.projectknowledge.util.ProjectNavigationUrlValidator;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProjectLinkedRepositoryService {

    private final ProjectLinkedRepositoryRepository repositoryRepository;
    private final ProjectScopeAuthorization authorization;
    private final ProjectOperationalMapper mapper;
    private final UserRepository userRepository;

    public ProjectLinkedRepositoryService(
            ProjectLinkedRepositoryRepository repositoryRepository,
            ProjectScopeAuthorization authorization,
            ProjectOperationalMapper mapper,
            UserRepository userRepository
    ) {
        this.repositoryRepository = repositoryRepository;
        this.authorization = authorization;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<ProjectLinkedRepositoryResponse> listRepositories(
            UUID projectId,
            String search,
            RepositoryType repositoryType,
            RepositoryProvider provider,
            boolean includeInactive,
            AuthenticatedUser principal
    ) {
        authorization.requireReadableProject(projectId, principal);
        return repositoryRepository.findByProject(
                        projectId,
                        toSearchPattern(normalizeSearch(search)),
                        repositoryType,
                        provider,
                        includeInactive
                ).stream()
                .map(mapper::toRepositoryResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public ProjectLinkedRepositoryResponse getRepository(UUID projectId, UUID repositoryId, AuthenticatedUser principal) {
        return mapper.toRepositoryResponse(findRepository(projectId, repositoryId, principal));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectLinkedRepositoryResponse createRepository(
            UUID projectId,
            ProjectLinkedRepositoryRequest request,
            AuthenticatedUser principal
    ) {
        Project project = authorization.requireManageableProject(projectId, principal);
        String name = normalizeRequired(request.name(), "Repository name is required");
        ensureUniqueRepositoryName(projectId, name, null);
        String url = ProjectNavigationUrlValidator.normalizeNavigationUrl(request.repositoryUrl(), "Repository URL");
        ProjectLinkedRepository repository = repositoryRepository.save(new ProjectLinkedRepository(
                project,
                name,
                normalizeOptional(request.description()),
                request.repositoryType(),
                request.provider(),
                url,
                normalizeOptional(request.defaultBranch()),
                normalizeDisplayOrder(request.displayOrder()),
                findUser(principal.getId())
        ));
        return mapper.toRepositoryResponse(repository);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectLinkedRepositoryResponse updateRepository(
            UUID projectId,
            UUID repositoryId,
            ProjectLinkedRepositoryRequest request,
            AuthenticatedUser principal
    ) {
        authorization.requireManageableProject(projectId, principal);
        ProjectLinkedRepository repository = findRepositoryEntity(projectId, repositoryId);
        String name = normalizeRequired(request.name(), "Repository name is required");
        ensureUniqueRepositoryName(projectId, name, repositoryId);
        String url = ProjectNavigationUrlValidator.normalizeNavigationUrl(request.repositoryUrl(), "Repository URL");
        repository.updateDetails(
                name,
                normalizeOptional(request.description()),
                request.repositoryType(),
                request.provider(),
                url,
                normalizeOptional(request.defaultBranch()),
                normalizeDisplayOrder(request.displayOrder()),
                request.active() == null || request.active()
        );
        return mapper.toRepositoryResponse(repository);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void deleteRepository(UUID projectId, UUID repositoryId, AuthenticatedUser principal) {
        authorization.requireOwnerProject(projectId, principal);
        ProjectLinkedRepository repository = findRepositoryEntity(projectId, repositoryId);
        repositoryRepository.delete(repository);
    }

    public long countActiveRepositories(UUID projectId) {
        return repositoryRepository.countByProjectIdAndActiveTrue(projectId);
    }

    private ProjectLinkedRepository findRepository(UUID projectId, UUID repositoryId, AuthenticatedUser principal) {
        authorization.requireReadableProject(projectId, principal);
        return findRepositoryEntity(projectId, repositoryId);
    }

    private ProjectLinkedRepository findRepositoryEntity(UUID projectId, UUID repositoryId) {
        ProjectLinkedRepository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Project repository was not found"));
        if (!repository.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Project repository was not found");
        }
        return repository;
    }

    private void ensureUniqueRepositoryName(UUID projectId, String name, UUID excludeId) {
        if (repositoryRepository.existsByProjectIdAndNameIgnoreCase(projectId, name, excludeId)) {
            throw new IllegalArgumentException("A repository with this name already exists for this project");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
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

    private String toSearchPattern(String search) {
        return search == null ? null : "%" + search.toLowerCase(Locale.ROOT) + "%";
    }

    private int normalizeDisplayOrder(Integer displayOrder) {
        return displayOrder == null ? 0 : Math.max(0, displayOrder);
    }
}
