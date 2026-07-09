package com.company.learninghub.projectknowledge.mapper;

import com.company.learninghub.learn.dto.RelatedTechnologySummary;
import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeFolder;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeItem;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import com.company.learninghub.projectknowledge.dto.ProjectFolderResponse;
import com.company.learninghub.projectknowledge.dto.ProjectKnowledgeItemResponse;
import com.company.learninghub.projectknowledge.dto.ProjectMemberResponse;
import com.company.learninghub.projectknowledge.dto.ProjectResponse;
import com.company.learninghub.projectknowledge.dto.ProjectUserResponse;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectKnowledgeMapper {

    public ProjectResponse toProjectResponse(Project project) {
        return toProjectResponse(project, null, null, null, List.of());
    }

    public ProjectResponse toProjectResponse(
            Project project,
            ProjectUserResponse owner,
            Integer memberCount,
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
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    public ProjectFolderResponse toFolderResponse(ProjectKnowledgeFolder folder) {
        return new ProjectFolderResponse(
                folder.getId(),
                folder.getProject().getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getParent() == null ? null : folder.getParent().getId(),
                toUserResponse(folder.getCreatedBy()),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
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
