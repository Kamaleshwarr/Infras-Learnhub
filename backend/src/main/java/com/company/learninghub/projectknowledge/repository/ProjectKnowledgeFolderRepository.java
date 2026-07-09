package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectKnowledgeFolderRepository extends JpaRepository<ProjectKnowledgeFolder, UUID> {

    @Override
    @EntityGraph(attributePaths = {"project", "parent", "createdBy"})
    Optional<ProjectKnowledgeFolder> findById(UUID id);

    @EntityGraph(attributePaths = {"project", "parent", "createdBy"})
    @Query("""
            SELECT folder
            FROM ProjectKnowledgeFolder folder
            WHERE folder.project.id = :projectId
              AND ((:parentId IS NULL AND folder.parent IS NULL)
                   OR (:parentId IS NOT NULL AND folder.parent.id = :parentId))
              AND (:searchPattern IS NULL
                   OR LOWER(folder.name) LIKE :searchPattern
                   OR LOWER(folder.description) LIKE :searchPattern)
            """)
    Page<ProjectKnowledgeFolder> findByProjectAndParent(
            @Param("projectId") UUID projectId,
            @Param("parentId") UUID parentId,
            @Param("searchPattern") String searchPattern,
            Pageable pageable
    );

    boolean existsByParentId(UUID parentId);

    long countByParentId(UUID parentId);

    @Query("""
            SELECT COUNT(folder) > 0
            FROM ProjectKnowledgeFolder folder
            WHERE folder.project.id = :projectId
              AND LOWER(folder.name) = LOWER(:name)
              AND ((:parentId IS NULL AND folder.parent IS NULL)
                   OR (:parentId IS NOT NULL AND folder.parent.id = :parentId))
              AND (:excludeId IS NULL OR folder.id <> :excludeId)
            """)
    boolean existsSiblingWithName(
            @Param("projectId") UUID projectId,
            @Param("name") String name,
            @Param("parentId") UUID parentId,
            @Param("excludeId") UUID excludeId
    );

    @EntityGraph(attributePaths = {"parent"})
    java.util.List<ProjectKnowledgeFolder> findByProjectId(UUID projectId);
}

