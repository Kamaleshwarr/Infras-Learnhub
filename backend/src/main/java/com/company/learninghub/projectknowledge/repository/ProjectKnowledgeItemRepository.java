package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
import com.company.learninghub.projectknowledge.domain.KnowledgeSourceType;
import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectKnowledgeItemRepository extends JpaRepository<ProjectKnowledgeItem, UUID> {

    @Override
    @EntityGraph(attributePaths = {"project", "folder", "uploadedBy"})
    Optional<ProjectKnowledgeItem> findById(UUID id);

    boolean existsByFolderId(UUID folderId);

    @Query("""
            SELECT COUNT(item)
            FROM ProjectKnowledgeItem item
            WHERE item.folder.id = :folderId
            """)
    long countByFolderId(@Param("folderId") UUID folderId);

    @EntityGraph(attributePaths = {"project", "folder", "uploadedBy"})
    @Query("""
            SELECT item
            FROM ProjectKnowledgeItem item
            LEFT JOIN item.folder folder
            WHERE item.project.id = :projectId
              AND (:folderId IS NULL OR item.folder.id = :folderId)
              AND (:category IS NULL OR item.category = :category)
              AND (:sourceType IS NULL OR item.sourceType = :sourceType)
              AND (:searchPattern IS NULL
                   OR LOWER(item.title) LIKE :searchPattern
                   OR LOWER(item.description) LIKE :searchPattern
                   OR LOWER(folder.name) LIKE :searchPattern)
            """)
    Page<ProjectKnowledgeItem> search(
            @Param("projectId") UUID projectId,
            @Param("folderId") UUID folderId,
            @Param("category") KnowledgeCategory category,
            @Param("sourceType") KnowledgeSourceType sourceType,
            @Param("searchPattern") String searchPattern,
            Pageable pageable
    );
}

