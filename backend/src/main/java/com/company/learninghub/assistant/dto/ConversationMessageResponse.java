package com.company.learninghub.assistant.dto;

import com.company.learninghub.assistant.domain.AssistantMessageRole;

import java.time.Instant;
import java.util.UUID;

public record ConversationMessageResponse(
        UUID id,
        AssistantMessageRole role,
        String content,
        Instant createdAt
) {
}
