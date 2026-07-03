package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnRoadmapRepository extends JpaRepository<LearnRoadmap, UUID> {

    @Query("SELECT r FROM LearnRoadmap r JOIN FETCH r.technology t WHERE t.slug = :slug")
    Optional<LearnRoadmap> findByTechnologySlug(@Param("slug") String slug);

    @Query("SELECT r FROM LearnRoadmap r WHERE r.catalogPresent = true")
    List<LearnRoadmap> findByCatalogPresentTrue();

    @Query("SELECT r FROM LearnRoadmap r JOIN FETCH r.technology WHERE r.catalogPresent = true")
    List<LearnRoadmap> findCatalogPresentWithTechnology();

    boolean existsByTechnology_SlugAndCatalogPresentTrue(String slug);
}
