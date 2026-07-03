package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnRoadmapStageResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LearnRoadmapStageResourceRepository extends JpaRepository<LearnRoadmapStageResource, UUID> {

    @Query("""
            SELECT r FROM LearnRoadmapStageResource r
            WHERE r.stage.id IN :stageIds
            ORDER BY r.resourceKind ASC, r.resourceOrder ASC, r.title ASC
            """)
    List<LearnRoadmapStageResource> findByStageIdIn(@Param("stageIds") List<UUID> stageIds);
}
