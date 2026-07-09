package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectEnvironmentReference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectEnvironmentReferenceRepository extends JpaRepository<ProjectEnvironmentReference, UUID> {

    @EntityGraph(attributePaths = {"environment", "environment.project"})
    Optional<ProjectEnvironmentReference> findById(UUID id);

    @EntityGraph(attributePaths = {"environment"})
    @Query("""
            SELECT reference
            FROM ProjectEnvironmentReference reference
            WHERE reference.environment.id = :environmentId
              AND (:includeInactive = TRUE OR reference.active = TRUE)
              AND (:searchPattern IS NULL
                   OR LOWER(reference.name) LIKE :searchPattern
                   OR LOWER(reference.description) LIKE :searchPattern
                   OR LOWER(reference.url) LIKE :searchPattern)
            ORDER BY reference.displayOrder ASC, reference.name ASC
            """)
    List<ProjectEnvironmentReference> findByEnvironment(
            @Param("environmentId") UUID environmentId,
            @Param("searchPattern") String searchPattern,
            @Param("includeInactive") boolean includeInactive
    );

    boolean existsByEnvironmentId(UUID environmentId);

    long countByEnvironmentId(UUID environmentId);
}
