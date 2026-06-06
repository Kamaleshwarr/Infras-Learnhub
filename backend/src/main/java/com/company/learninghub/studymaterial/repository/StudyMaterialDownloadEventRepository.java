package com.company.learninghub.studymaterial.repository;

import com.company.learninghub.studymaterial.domain.StudyMaterialDownloadEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudyMaterialDownloadEventRepository extends JpaRepository<StudyMaterialDownloadEvent, UUID> {
}

