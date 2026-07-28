package com.company.learninghub.assistant.dto;

public record AssistantStatusResponse(
        boolean enabled,
        String llmProvider,
        boolean llmHealthy
) {
}
