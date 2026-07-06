package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    @EntityGraph(attributePaths = {"project", "user"})
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    @EntityGraph(attributePaths = {"project", "user"})
    List<ProjectMember> findByProjectId(UUID projectId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserIdAndProjectRole(UUID projectId, UUID userId, ProjectRole role);

    long countByProjectId(UUID projectId);

    @EntityGraph(attributePaths = {"user", "project"})
    List<ProjectMember> findByProjectIdInAndUserId(Collection<UUID> projectIds, UUID userId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            SELECT member
            FROM ProjectMember member
            WHERE member.project.id IN :projectIds
              AND member.projectRole = :role
            ORDER BY member.createdAt ASC
            """)
    List<ProjectMember> findOwnersByProjectIds(
            @Param("projectIds") Collection<UUID> projectIds,
            @Param("role") ProjectRole role
    );
}

