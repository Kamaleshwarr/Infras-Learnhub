package com.company.learninghub.projectknowledge.repository;

import com.company.learninghub.projectknowledge.domain.ProjectKnowledgeAccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectKnowledgeAccessEventRepository extends JpaRepository<ProjectKnowledgeAccessEvent, UUID> {
}

