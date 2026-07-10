package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectExternalContact;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectExternalContactRepository extends JpaRepository<ProjectExternalContact, UUID> {

    @EntityGraph(attributePaths = {"project", "createdBy"})
    Optional<ProjectExternalContact> findById(UUID id);

    @EntityGraph(attributePaths = {"createdBy"})
    List<ProjectExternalContact> findByProjectIdAndActiveTrueOrderByDisplayOrderAscNameAsc(UUID projectId);

    long countByProjectIdAndPrimaryContactTrueAndActiveTrue(UUID projectId);
}
