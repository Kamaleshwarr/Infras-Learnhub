package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnTechnology;
import com.company.learninghub.learn.domain.LearnTechnologyProjectLink;
import com.company.learninghub.learn.domain.TechnologyStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnTechnologyProjectLinkRepository extends JpaRepository<LearnTechnologyProjectLink, UUID> {

    boolean existsByTechnologyIdAndProjectId(UUID technologyId, UUID projectId);

    @EntityGraph(attributePaths = "project")
    List<LearnTechnologyProjectLink> findByTechnologyIdOrderByProject_NameAsc(UUID technologyId);

    @Query("""
            SELECT link
            FROM LearnTechnologyProjectLink link
            JOIN FETCH link.technology technology
            WHERE link.project.id = :projectId
              AND technology.status = :status
            ORDER BY technology.name ASC
            """)
    List<LearnTechnologyProjectLink> findPublishedTechnologiesByProjectId(
            @Param("projectId") UUID projectId,
            @Param("status") TechnologyStatus status
    );

    Optional<LearnTechnologyProjectLink> findByTechnologyIdAndProjectId(UUID technologyId, UUID projectId);

    void deleteByTechnologyIdAndProjectId(UUID technologyId, UUID projectId);
}
