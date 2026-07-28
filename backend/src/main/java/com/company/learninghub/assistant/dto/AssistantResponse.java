package com.company.learninghub.assistant.dto;

import com.company.learninghub.assistant.intent.AssistantIntentType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AssistantResponse(
        String response,
        UUID conversationId,
        AssistantIntentType intentType,
        String toolUsed,
        List<AssistantSourceResponse> sources,
        Map<String, Object> metadata
) {
}
