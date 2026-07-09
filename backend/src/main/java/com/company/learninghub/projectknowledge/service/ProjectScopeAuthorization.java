package com.company.learninghub.projectknowledge.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.domain.ProjectStatus;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.user.domain.RoleName;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProjectScopeAuthorization {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;

    public ProjectScopeAuthorization(ProjectRepository projectRepository, ProjectMemberRepository memberRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
    }

    public Project requireReadableProject(UUID projectId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireReadAccess(project, principal);
        return project;
    }

    public Project requireManageableProject(UUID projectId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireContributor(project, principal);
        return project;
    }

    public Project requireOwnerProject(UUID projectId, AuthenticatedUser principal) {
        Project project = findProject(projectId);
        requireOwner(project, principal);
        return project;
    }

    public void requireReadAccess(Project project, AuthenticatedUser principal) {
        if (isArchivedForDiscovery(project) && !isAdmin(principal)) {
            throw new ResourceNotFoundException("Project was not found");
        }
        if (isAdmin(principal) || project.isPublic() || memberRepository.existsByProjectIdAndUserId(project.getId(), principal.getId())) {
            return;
        }
        throw new ResourceNotFoundException("Project was not found");
    }

    public void requireContributor(Project project, AuthenticatedUser principal) {
        if (isAdmin(principal)
                || memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), principal.getId(), ProjectRole.OWNER)
                || memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), principal.getId(), ProjectRole.CONTRIBUTOR)) {
            return;
        }
        throw new IllegalArgumentException("Project OWNER or CONTRIBUTOR role is required");
    }

    public void requireOwner(Project project, AuthenticatedUser principal) {
        if (isAdmin(principal) || memberRepository.existsByProjectIdAndUserIdAndProjectRole(project.getId(), principal.getId(), ProjectRole.OWNER)) {
            return;
        }
        throw new IllegalArgumentException("Project OWNER role is required");
    }

    public Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found"));
    }

    private boolean isAdmin(AuthenticatedUser principal) {
        return principal.getRoleNames().contains(RoleName.ADMIN);
    }

    private boolean isArchivedForDiscovery(Project project) {
        return project.isArchived() || project.getStatus() == ProjectStatus.ARCHIVED;
    }
}
