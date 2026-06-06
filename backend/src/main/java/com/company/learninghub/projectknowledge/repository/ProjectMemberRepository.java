package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectMember;
import com.company.learninghub.projectknowledge.domain.ProjectRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

