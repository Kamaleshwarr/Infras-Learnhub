package com.company.learninghub.studymaterial.repository;

import com.company.learninghub.studymaterial.domain.MaterialType;
import com.company.learninghub.studymaterial.domain.StudyMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, UUID> {

    @Override
    @EntityGraph(attributePaths = {"folder", "uploadedBy"})
    Optional<StudyMaterial> findById(UUID id);

    boolean existsByFolderId(UUID folderId);

    @EntityGraph(attributePaths = {"folder", "uploadedBy"})
    @Query("""
            SELECT material
            FROM StudyMaterial material
            WHERE (:folderId IS NULL OR material.folder.id = :folderId)
              AND (:materialType IS NULL OR material.materialType = :materialType)
              AND (
                    :search IS NULL
                    OR LOWER(material.title) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(material.description) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<StudyMaterial> search(
            @Param("folderId") UUID folderId,
            @Param("materialType") MaterialType materialType,
            @Param("search") String search,
            Pageable pageable
    );
}

