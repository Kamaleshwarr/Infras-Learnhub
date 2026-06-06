package com.company.learninghub.studymaterial.repository;

import com.company.learninghub.studymaterial.domain.StudyMaterialFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StudyMaterialFolderRepository extends JpaRepository<StudyMaterialFolder, UUID> {

    @Override
    @EntityGraph(attributePaths = {"parent", "createdBy"})
    Optional<StudyMaterialFolder> findById(UUID id);

    @EntityGraph(attributePaths = {"parent", "createdBy"})
    @Query("""
            SELECT folder
            FROM StudyMaterialFolder folder
            WHERE (:parentId IS NULL AND folder.parent IS NULL)
               OR (:parentId IS NOT NULL AND folder.parent.id = :parentId)
            """)
    Page<StudyMaterialFolder> findByParentId(@Param("parentId") UUID parentId, Pageable pageable);

    boolean existsByParentId(UUID parentId);

    @Query("""
            SELECT COUNT(folder) > 0
            FROM StudyMaterialFolder folder
            WHERE LOWER(folder.name) = LOWER(:name)
              AND (
                    (:parentId IS NULL AND folder.parent IS NULL)
                    OR (:parentId IS NOT NULL AND folder.parent.id = :parentId)
                  )
              AND (:excludeId IS NULL OR folder.id <> :excludeId)
            """)
    boolean existsSiblingWithName(
            @Param("name") String name,
            @Param("parentId") UUID parentId,
            @Param("excludeId") UUID excludeId
    );
}

