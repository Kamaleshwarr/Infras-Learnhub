package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.Project;
import com.company.learninghub.projectknowledge.domain.ProjectAccessType;
import com.company.learninghub.projectknowledge.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Override
    @EntityGraph(attributePaths = "createdBy")
    Optional<Project> findById(UUID id);

    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "createdBy")
    @Query("""
            SELECT project
            FROM Project project
            WHERE (:includeArchived = TRUE OR (project.archived = FALSE AND project.status <> 'ARCHIVED'))
              AND (:status IS NULL OR project.status = :status)
              AND (:accessType IS NULL OR project.accessType = :accessType)
              AND (:assignedOnly = FALSE OR EXISTS (
                        SELECT member.id
                        FROM ProjectMember member
                        WHERE member.project = project
                          AND member.user.id = :userId
                    ))
              AND (
                    :assignedOnly = TRUE
                    OR :admin = TRUE
                    OR project.accessType = 'PUBLIC'
                    OR EXISTS (
                        SELECT member.id
                        FROM ProjectMember member
                        WHERE member.project = project
                          AND member.user.id = :userId
                    )
                  )
              AND (:searchPattern IS NULL
                   OR LOWER(project.name) LIKE :searchPattern
                   OR LOWER(project.description) LIKE :searchPattern)
            """)
    Page<Project> search(
            @Param("searchPattern") String searchPattern,
            @Param("accessType") ProjectAccessType accessType,
            @Param("status") ProjectStatus status,
            @Param("assignedOnly") boolean assignedOnly,
            @Param("includeArchived") boolean includeArchived,
            @Param("userId") UUID userId,
            @Param("admin") boolean admin,
            Pageable pageable
    );
}

