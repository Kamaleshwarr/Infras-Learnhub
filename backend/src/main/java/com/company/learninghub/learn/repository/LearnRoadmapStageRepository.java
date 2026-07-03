package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnRoadmapStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LearnRoadmapStageRepository extends JpaRepository<LearnRoadmapStage, UUID> {

    @Query("SELECT s FROM LearnRoadmapStage s WHERE s.roadmap.id = :roadmapId ORDER BY s.stageOrder ASC")
    List<LearnRoadmapStage> findByRoadmapIdOrderByStageOrder(@Param("roadmapId") UUID roadmapId);
}
