package com.company.learninghub.assistant.dto;

public record AssistantSourceResponse(
        String serviceName,
        String toolName,
        AssistantSourceConfidence confidence
) {
}
