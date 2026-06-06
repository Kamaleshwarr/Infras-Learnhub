package com.company.learninghub.studymaterial.mapper;

import com.company.learninghub.studymaterial.domain.StudyMaterial;
import com.company.learninghub.studymaterial.domain.StudyMaterialFolder;
import com.company.learninghub.studymaterial.dto.StudyMaterialFolderResponse;
import com.company.learninghub.studymaterial.dto.StudyMaterialResponse;
import com.company.learninghub.studymaterial.dto.StudyMaterialUserResponse;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class StudyMaterialMapper {

    public StudyMaterialFolderResponse toFolderResponse(StudyMaterialFolder folder) {
        return new StudyMaterialFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getParent() == null ? null : folder.getParent().getId(),
                toUserResponse(folder.getCreatedBy()),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }

    public StudyMaterialResponse toMaterialResponse(StudyMaterial material) {
        return new StudyMaterialResponse(
                material.getId(),
                material.getFolder() == null ? null : material.getFolder().getId(),
                material.getFolder() == null ? null : material.getFolder().getName(),
                material.getTitle(),
                material.getDescription(),
                material.getMaterialType(),
                material.getSourceType(),
                material.getOriginalFilename(),
                material.getContentType(),
                material.getFileSizeBytes(),
                material.getExternalUrl(),
                material.getDownloadCount(),
                toUserResponse(material.getUploadedBy()),
                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }

    private StudyMaterialUserResponse toUserResponse(User user) {
        return new StudyMaterialUserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}

