package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.KnowledgeCategory;
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

    @EntityGraph(attributePaths = {"project", "folder", "uploadedBy"})
    @Query("""
            SELECT item
            FROM ProjectKnowledgeItem item
            WHERE item.project.id = :projectId
              AND (:folderId IS NULL OR item.folder.id = :folderId)
              AND (:category IS NULL OR item.category = :category)
              AND (:search IS NULL
                   OR LOWER(item.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(item.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<ProjectKnowledgeItem> search(
            @Param("projectId") UUID projectId,
            @Param("folderId") UUID folderId,
            @Param("category") KnowledgeCategory category,
            @Param("search") String search,
            Pageable pageable
    );
}

