package com.company.learninghub.projectknowledge.mapper;

import com.company.learninghub.projectknowledge.domain.ProjectEnvironment;
import com.company.learninghub.projectknowledge.domain.ProjectEnvironmentReference;
import com.company.learninghub.projectknowledge.domain.ProjectLinkedRepository;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentReferenceResponse;
import com.company.learninghub.projectknowledge.dto.ProjectEnvironmentResponse;
import com.company.learninghub.projectknowledge.dto.ProjectLinkedRepositoryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectOperationalMapper {

    private final ProjectKnowledgeMapper projectKnowledgeMapper;

    public ProjectOperationalMapper(ProjectKnowledgeMapper projectKnowledgeMapper) {
        this.projectKnowledgeMapper = projectKnowledgeMapper;
    }

    public ProjectEnvironmentResponse toEnvironmentResponse(
            ProjectEnvironment environment,
            long referenceCount,
            List<ProjectEnvironmentReferenceResponse> references
    ) {
        return new ProjectEnvironmentResponse(
                environment.getId(),
                environment.getProject().getId(),
                environment.getName(),
                environment.getDescription(),
                environment.getDisplayOrder(),
                environment.isActive(),
                projectKnowledgeMapper.toUserResponse(environment.getCreatedBy()),
                environment.getCreatedAt(),
                environment.getUpdatedAt(),
                referenceCount,
                references
        );
    }

    public ProjectEnvironmentReferenceResponse toReferenceResponse(ProjectEnvironmentReference reference) {
        return new ProjectEnvironmentReferenceResponse(
                reference.getId(),
                reference.getEnvironment().getId(),
                reference.getName(),
                reference.getReferenceType(),
                reference.getUrl(),
                reference.getDescription(),
                reference.getDisplayOrder(),
                reference.isActive(),
                reference.getCreatedAt(),
                reference.getUpdatedAt()
        );
    }

    public ProjectLinkedRepositoryResponse toRepositoryResponse(ProjectLinkedRepository repository) {
        return new ProjectLinkedRepositoryResponse(
                repository.getId(),
                repository.getProject().getId(),
                repository.getName(),
                repository.getDescription(),
                repository.getRepositoryType(),
                repository.getProvider(),
                repository.getRepositoryUrl(),
                repository.getDefaultBranch(),
                repository.getDisplayOrder(),
                repository.isActive(),
                projectKnowledgeMapper.toUserResponse(repository.getCreatedBy()),
                repository.getCreatedAt(),
                repository.getUpdatedAt()
        );
    }
}
