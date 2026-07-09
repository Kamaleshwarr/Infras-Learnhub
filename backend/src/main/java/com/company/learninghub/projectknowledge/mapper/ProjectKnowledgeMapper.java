package com.company.learninghub.projectknowledge.mapper;

import com.company.learninghub.learn.dto.RelatedTechnologySummary;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectExternalContact;
import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeFolder;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeItem;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.dto.ProjectExternalContactResponse;
import com.company.learninghub.projectknowledge.dto.ProjectFolderResponse;
import com.company.learninghub.projectknowledge.dto.ProjectKnowledgeItemResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberCandidateResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberResponse;
import com.company.learninghub.projectknowledge.dto.ProjectResponse;
import com.company.learninghub.projectknowledge.dto.ProjectUserResponse;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectKnowledgeMapper {

    public ProjectResponse toProjectResponse(Project project) {
        return toProjectResponse(project, null, null, null, null, null, null, List.of());
    }

    public ProjectResponse toProjectResponse(
            Project project,
            ProjectUserResponse owner,
            Integer memberCount,
            Integer primaryContactCount,
            Integer environmentCount,
            Integer repositoryCount,
            ProjectRole currentMemberRole,
            List<RelatedTechnologySummary> relatedTechnologies
    ) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getAccessType(),
                project.getStatus(),
                project.isArchived(),
                toUserResponse(project.getCreatedBy()),
                owner,
                memberCount,
                primaryContactCount,
                environmentCount,
                repositoryCount,
                currentMemberRole,
                relatedTechnologies,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                toUserResponse(member.getUser()),
                member.getProjectRole(),
                member.getFunctionalRole(),
                member.getResponsibility(),
                member.isPrimaryContact(),
                member.getDisplayOrder(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    public ProjectExternalContactResponse toExternalContactResponse(ProjectExternalContact contact) {
        return new ProjectExternalContactResponse(
                contact.getId(),
                contact.getProject().getId(),
                contact.getName(),
                contact.getContactType(),
                contact.getRoleTitle(),
                contact.getOrganization(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getContactUrl(),
                contact.getNotes(),
                contact.isPrimaryContact(),
                contact.getDisplayOrder(),
                contact.isActive(),
                toUserResponse(contact.getCreatedBy()),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }

    public ProjectMemberCandidateResponse toMemberCandidateResponse(User user) {
        return new ProjectMemberCandidateResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    public ProjectFolderResponse toFolderResponse(ProjectKnowledgeFolder folder) {
        return toFolderResponse(folder, 0L, 0L);
    }

    public ProjectFolderResponse toFolderResponse(ProjectKnowledgeFolder folder, long childFolderCount, long itemCount) {
        return new ProjectFolderResponse(
                folder.getId(),
                folder.getProject().getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getParent() == null ? null : folder.getParent().getId(),
                toUserResponse(folder.getCreatedBy()),
                folder.getCreatedAt(),
                folder.getUpdatedAt(),
                childFolderCount,
                itemCount
        );
    }

    public ProjectKnowledgeItemResponse toItemResponse(ProjectKnowledgeItem item) {
        return new ProjectKnowledgeItemResponse(
                item.getId(),
                item.getProject().getId(),
                item.getFolder() == null ? null : item.getFolder().getId(),
                item.getFolder() == null ? null : item.getFolder().getName(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getSourceType(),
                item.getOriginalFilename(),
                item.getContentType(),
                item.getFileSizeBytes(),
                item.getExternalUrl(),
                item.getAccessCount(),
                toUserResponse(item.getUploadedBy()),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public ProjectUserResponse toUserResponse(User user) {
        return new ProjectUserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}
