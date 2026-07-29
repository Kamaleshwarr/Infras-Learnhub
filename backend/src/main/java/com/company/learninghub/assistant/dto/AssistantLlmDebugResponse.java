package com.company.learninghub.assistant.dto;

public record AssistantLlmDebugResponse(
        String provider,
        long elapsedMs,
        boolean success,
        Integer httpStatus,
        String httpUri,
        String rawResponse,
        String parsedContent,
        String parsedReasoning,
        String finalText,
        String error
) {
}
