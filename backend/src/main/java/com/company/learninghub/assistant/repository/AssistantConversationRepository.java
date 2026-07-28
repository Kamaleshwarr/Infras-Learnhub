package com.company.learninghub.assistant.repository;

import com.company.learninghub.assistant.domain.AssistantConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssistantConversationRepository extends JpaRepository<AssistantConversation, UUID> {

    Optional<AssistantConversation> findByUserId(UUID userId);
}
