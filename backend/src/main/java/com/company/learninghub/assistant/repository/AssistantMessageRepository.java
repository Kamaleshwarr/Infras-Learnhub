package com.company.learninghub.assistant.repository;

import com.company.learninghub.assistant.domain.AssistantMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssistantMessageRepository extends JpaRepository<AssistantMessage, UUID> {

    List<AssistantMessage> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
