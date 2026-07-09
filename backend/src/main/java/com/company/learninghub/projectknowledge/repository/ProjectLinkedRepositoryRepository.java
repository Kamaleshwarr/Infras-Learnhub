package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectLinkedRepository;
import com.company.learninghub.projectknowledge.domain.RepositoryProvider;
import com.company.learninghub.projectknowledge.domain.RepositoryType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectLinkedRepositoryRepository extends JpaRepository<ProjectLinkedRepository, UUID> {

    @EntityGraph(attributePaths = {"project", "createdBy"})
    Optional<ProjectLinkedRepository> findById(UUID id);

    @EntityGraph(attributePaths = {"project", "createdBy"})
    @Query("""
            SELECT repository
            FROM ProjectLinkedRepository repository
            WHERE repository.project.id = :projectId
              AND (:includeInactive = TRUE OR repository.active = TRUE)
              AND (:repositoryType IS NULL OR repository.repositoryType = :repositoryType)
              AND (:provider IS NULL OR repository.provider = :provider)
              AND (:searchPattern IS NULL
                   OR LOWER(repository.name) LIKE :searchPattern
                   OR LOWER(repository.description) LIKE :searchPattern
                   OR LOWER(repository.defaultBranch) LIKE :searchPattern)
            ORDER BY repository.displayOrder ASC, repository.name ASC
            """)
    List<ProjectLinkedRepository> findByProject(
            @Param("projectId") UUID projectId,
            @Param("searchPattern") String searchPattern,
            @Param("repositoryType") RepositoryType repositoryType,
            @Param("provider") RepositoryProvider provider,
            @Param("includeInactive") boolean includeInactive
    );

    long countByProjectIdAndActiveTrue(UUID projectId);

    @Query("""
            SELECT COUNT(repository) > 0
            FROM ProjectLinkedRepository repository
            WHERE repository.project.id = :projectId
              AND LOWER(repository.name) = LOWER(:name)
              AND (:excludeId IS NULL OR repository.id <> :excludeId)
            """)
    boolean existsByProjectIdAndNameIgnoreCase(
            @Param("projectId") UUID projectId,
            @Param("name") String name,
            @Param("excludeId") UUID excludeId
    );
}
