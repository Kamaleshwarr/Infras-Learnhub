package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectEnvironment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectEnvironmentRepository extends JpaRepository<ProjectEnvironment, UUID> {

    @EntityGraph(attributePaths = {"project", "createdBy"})
    Optional<ProjectEnvironment> findById(UUID id);

    @EntityGraph(attributePaths = {"project", "createdBy"})
    @Query("""
            SELECT DISTINCT environment
            FROM ProjectEnvironment environment
            LEFT JOIN ProjectEnvironmentReference reference ON reference.environment = environment
            WHERE environment.project.id = :projectId
              AND (:includeInactive = TRUE OR environment.active = TRUE)
              AND (:searchPattern IS NULL
                   OR LOWER(environment.name) LIKE :searchPattern
                   OR LOWER(environment.description) LIKE :searchPattern
                   OR LOWER(reference.name) LIKE :searchPattern
                   OR LOWER(reference.description) LIKE :searchPattern
                   OR LOWER(reference.url) LIKE :searchPattern)
            ORDER BY environment.displayOrder ASC, environment.name ASC
            """)
    List<ProjectEnvironment> findByProject(
            @Param("projectId") UUID projectId,
            @Param("searchPattern") String searchPattern,
            @Param("includeInactive") boolean includeInactive
    );

    long countByProjectIdAndActiveTrue(UUID projectId);

    @Query("""
            SELECT COUNT(environment) > 0
            FROM ProjectEnvironment environment
            WHERE environment.project.id = :projectId
              AND LOWER(environment.name) = LOWER(:name)
              AND (:excludeId IS NULL OR environment.id <> :excludeId)
            """)
    boolean existsByProjectIdAndNameIgnoreCase(
            @Param("projectId") UUID projectId,
            @Param("name") String name,
            @Param("excludeId") UUID excludeId
    );
}
