package com.company.learninghub.learn.repository;

import com.company.learninghub.learn.domain.LearnTechnology;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LearnTechnologyRepository extends JpaRepository<LearnTechnology, UUID>,
        JpaSpecificationExecutor<LearnTechnology> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Override
    @EntityGraph(attributePaths = {"createdBy", "projectLinks", "projectLinks.project"})
    Optional<LearnTechnology> findById(UUID id);
}
