package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnStageResourceOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnStageResourceOverrideRepository extends JpaRepository<LearnStageResourceOverride, UUID> {

    List<LearnStageResourceOverride> findByTechnologySlug(String technologySlug);

    List<LearnStageResourceOverride> findByTechnologySlugAndStageSlug(String technologySlug, String stageSlug);

    Optional<LearnStageResourceOverride> findByTechnologySlugAndStageSlugAndResourceSlug(
            String technologySlug,
            String stageSlug,
            String resourceSlug
    );

    Optional<LearnStageResourceOverride> findByTechnologySlugAndStageSlugAndCatalogResourceSlug(
            String technologySlug,
            String stageSlug,
            String catalogResourceSlug
    );
}
