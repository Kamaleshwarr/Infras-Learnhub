package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnTechnology;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnTechnologyRepository extends JpaRepository<LearnTechnology, UUID>,
        JpaSpecificationExecutor<LearnTechnology> {

    @Override
    @EntityGraph(attributePaths = {"createdBy", "projectLinks", "projectLinks.project"})
    Optional<LearnTechnology> findById(UUID id);

    @EntityGraph(attributePaths = {"createdBy", "projectLinks", "projectLinks.project"})
    Optional<LearnTechnology> findBySlug(String slug);

    List<LearnTechnology> findBySlugIn(Collection<String> slugs);

    List<LearnTechnology> findByCatalogPresentTrue();

    @EntityGraph(attributePaths = "projectLinks")
    @Query("SELECT t FROM LearnTechnology t WHERE t.catalogPresent = true")
    List<LearnTechnology> findCatalogPresentWithProjectLinks();
}
