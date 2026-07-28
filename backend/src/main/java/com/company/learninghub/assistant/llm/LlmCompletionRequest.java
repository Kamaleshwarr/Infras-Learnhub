package com.company.learninghub.assistant.llm;

import java.util.List;

public record LlmCompletionRequest(
        String systemPrompt,
        List<LlmMessage> messages
) {
    public record LlmMessage(String role, String content) {
    }
}
