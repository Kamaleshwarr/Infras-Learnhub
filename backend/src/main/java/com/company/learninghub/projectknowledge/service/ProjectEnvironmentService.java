package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectEnvironment;
import com.company.learninghub.projectknowledge.domain.ProjectEnvironmentReference;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentReferenceRequest;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentReferenceResponse;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentRequest;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentResponse;
import com.company.learninghub.projectknowledge.mapper.ProjectOperationalMapper;
import com.company.learninghub.projectknowledge.repository.ProjectEnvironmentReferenceRepository;
import com.company.learninghub.projectknowledge.repository.ProjectEnvironmentRepository;
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
public class ProjectEnvironmentService {

    private final ProjectEnvironmentRepository environmentRepository;
    private final ProjectEnvironmentReferenceRepository referenceRepository;
    private final ProjectScopeAuthorization authorization;
    private final ProjectOperationalMapper mapper;
    private final UserRepository userRepository;

    public ProjectEnvironmentService(
            ProjectEnvironmentRepository environmentRepository,
            ProjectEnvironmentReferenceRepository referenceRepository,
            ProjectScopeAuthorization authorization,
            ProjectOperationalMapper mapper,
            UserRepository userRepository
    ) {
        this.environmentRepository = environmentRepository;
        this.referenceRepository = referenceRepository;
        this.authorization = authorization;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<ProjectEnvironmentResponse> listEnvironments(
            UUID projectId,
            String search,
            boolean includeInactive,
            AuthenticatedUser principal
    ) {
        authorization.requireReadableProject(projectId, principal);
        String pattern = toSearchPattern(normalizeSearch(search));
        return environmentRepository.findByProject(projectId, pattern, includeInactive).stream()
                .map(environment -> toEnvironmentResponse(environment, pattern, includeInactive))
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public ProjectEnvironmentResponse getEnvironment(UUID projectId, UUID environmentId, AuthenticatedUser principal) {
        ProjectEnvironment environment = findEnvironment(projectId, environmentId, principal);
        return toEnvironmentResponse(environment);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectEnvironmentResponse createEnvironment(
            UUID projectId,
            ProjectEnvironmentRequest request,
            AuthenticatedUser principal
    ) {
        Project project = authorization.requireManageableProject(projectId, principal);
        String name = normalizeRequired(request.name(), "Environment name is required");
        ensureUniqueEnvironmentName(projectId, name, null);
        ProjectEnvironment environment = environmentRepository.save(new ProjectEnvironment(
                project,
                name,
                normalizeOptional(request.description()),
                normalizeDisplayOrder(request.displayOrder()),
                findUser(principal.getId())
        ));
        return toEnvironmentResponse(environment);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectEnvironmentResponse updateEnvironment(
            UUID projectId,
            UUID environmentId,
            ProjectEnvironmentRequest request,
            AuthenticatedUser principal
    ) {
        authorization.requireManageableProject(projectId, principal);
        ProjectEnvironment environment = findEnvironmentEntity(projectId, environmentId);
        String name = normalizeRequired(request.name(), "Environment name is required");
        ensureUniqueEnvironmentName(projectId, name, environmentId);
        environment.updateDetails(
                name,
                normalizeOptional(request.description()),
                normalizeDisplayOrder(request.displayOrder()),
                request.active() == null || request.active()
        );
        return toEnvironmentResponse(environment);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void deleteEnvironment(UUID projectId, UUID environmentId, AuthenticatedUser principal) {
        authorization.requireOwnerProject(projectId, principal);
        ProjectEnvironment environment = findEnvironmentEntity(projectId, environmentId);
        if (referenceRepository.existsByEnvironmentId(environmentId)) {
            throw new IllegalArgumentException("Environment must be empty before deletion");
        }
        environmentRepository.delete(environment);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectEnvironmentReferenceResponse createReference(
            UUID projectId,
            UUID environmentId,
            ProjectEnvironmentReferenceRequest request,
            AuthenticatedUser principal
    ) {
        authorization.requireManageableProject(projectId, principal);
        ProjectEnvironment environment = findEnvironmentEntity(projectId, environmentId);
        String url = ProjectNavigationUrlValidator.normalizeNavigationUrl(request.url(), "Reference URL");
        ProjectEnvironmentReference reference = referenceRepository.save(new ProjectEnvironmentReference(
                environment,
                normalizeRequired(request.name(), "Reference name is required"),
                request.referenceType(),
                url,
                normalizeOptional(request.description()),
                normalizeDisplayOrder(request.displayOrder())
        ));
        return mapper.toReferenceResponse(reference);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public ProjectEnvironmentReferenceResponse updateReference(
            UUID projectId,
            UUID environmentId,
            UUID referenceId,
            ProjectEnvironmentReferenceRequest request,
            AuthenticatedUser principal
    ) {
        authorization.requireManageableProject(projectId, principal);
        ProjectEnvironmentReference reference = findReference(projectId, environmentId, referenceId);
        String url = ProjectNavigationUrlValidator.normalizeNavigationUrl(request.url(), "Reference URL");
        reference.updateDetails(
                normalizeRequired(request.name(), "Reference name is required"),
                request.referenceType(),
                url,
                normalizeOptional(request.description()),
                normalizeDisplayOrder(request.displayOrder()),
                request.active() == null || request.active()
        );
        return mapper.toReferenceResponse(reference);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public void deleteReference(
            UUID projectId,
            UUID environmentId,
            UUID referenceId,
            AuthenticatedUser principal
    ) {
        authorization.requireManageableProject(projectId, principal);
        ProjectEnvironmentReference reference = findReference(projectId, environmentId, referenceId);
        referenceRepository.delete(reference);
    }

    public long countActiveEnvironments(UUID projectId) {
        return environmentRepository.countByProjectIdAndActiveTrue(projectId);
    }

    private ProjectEnvironmentResponse toEnvironmentResponse(
            ProjectEnvironment environment,
            String searchPattern,
            boolean includeInactive
    ) {
        List<ProjectEnvironmentReferenceResponse> references = referenceRepository
                .findByEnvironment(environment.getId(), searchPattern, includeInactive)
                .stream()
                .map(mapper::toReferenceResponse)
                .toList();
        return mapper.toEnvironmentResponse(environment, references.size(), references);
    }

    private ProjectEnvironmentResponse toEnvironmentResponse(ProjectEnvironment environment) {
        return toEnvironmentResponse(environment, null, false);
    }

    private ProjectEnvironment findEnvironment(UUID projectId, UUID environmentId, AuthenticatedUser principal) {
        authorization.requireReadableProject(projectId, principal);
        return findEnvironmentEntity(projectId, environmentId);
    }

    private ProjectEnvironment findEnvironmentEntity(UUID projectId, UUID environmentId) {
        ProjectEnvironment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Project environment was not found"));
        if (!environment.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Project environment was not found");
        }
        return environment;
    }

    private ProjectEnvironmentReference findReference(UUID projectId, UUID environmentId, UUID referenceId) {
        ProjectEnvironmentReference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Environment reference was not found"));
        if (!reference.getEnvironment().getId().equals(environmentId)
                || !reference.getEnvironment().getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Environment reference was not found");
        }
        return reference;
    }

    private void ensureUniqueEnvironmentName(UUID projectId, String name, UUID excludeId) {
        if (environmentRepository.existsByProjectIdAndNameIgnoreCase(projectId, name, excludeId)) {
            throw new IllegalArgumentException("An environment with this name already exists for this project");
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
