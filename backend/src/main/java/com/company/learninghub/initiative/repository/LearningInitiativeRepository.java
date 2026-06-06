package com.company.learninghub.initiative.repository;

import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LearningInitiativeRepository extends JpaRepository<LearningInitiative, UUID> {

    @EntityGraph(attributePaths = "createdBy")
    @Query("""
            SELECT initiative
            FROM LearningInitiative initiative
            WHERE (:status IS NULL OR initiative.status = :status)
              AND (:search IS NULL OR LOWER(initiative.title) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<LearningInitiative> findForAdmin(
            @Param("status") InitiativeStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "createdBy")
    @Query("""
            SELECT initiative
            FROM LearningInitiative initiative
            WHERE initiative.status = 'ACTIVE'
              AND initiative.startDateUtc <= :now
              AND initiative.expiryDateUtc >= :now
              AND (:search IS NULL OR LOWER(initiative.title) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<LearningInitiative> findActiveForEmployee(
            @Param("search") String search,
            @Param("now") Instant now,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "createdBy")
    @Override
    @EntityGraph(attributePaths = "createdBy")
    Optional<LearningInitiative> findById(UUID id);
}

