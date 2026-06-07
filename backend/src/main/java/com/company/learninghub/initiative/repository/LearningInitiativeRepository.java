package com.company.learninghub.initiative.repository;

import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LearningInitiativeRepository extends JpaRepository<LearningInitiative, UUID>,
        JpaSpecificationExecutor<LearningInitiative> {

    @Override
    @EntityGraph(attributePaths = "createdBy")
    Optional<LearningInitiative> findById(UUID id);
}

