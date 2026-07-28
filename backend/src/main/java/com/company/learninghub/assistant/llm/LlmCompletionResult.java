package com.company.learninghub.assistant.llm;

public record LlmCompletionResult(
        boolean success,
        String content,
        String providerReference,
        String errorMessage
) {
    public static LlmCompletionResult success(String content, String providerReference) {
        return new LlmCompletionResult(true, content, providerReference, null);
    }

    public static LlmCompletionResult failure(String errorMessage) {
        return new LlmCompletionResult(false, null, null, errorMessage);
    }
}
