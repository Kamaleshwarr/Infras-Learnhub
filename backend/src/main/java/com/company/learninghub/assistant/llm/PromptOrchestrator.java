package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.dto.AssistantSourceConfidence;
import com.company.learninghub.assistant.tool.ToolResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromptOrchestrator {

    private static final String BASE_SYSTEM_PROMPT =
            "You are the Engineering Learning Hub assistant. Provide concise, helpful responses about "
                    + "platform workflows and general engineering concepts. "
                    + "Never invent user-specific platform data such as certifications, projects, rankings, "
                    + "or profile details unless it is explicitly provided in authoritative tool data.";

    private static final String KNOWLEDGE_SYSTEM_PROMPT = BASE_SYSTEM_PROMPT
            + " Answer general knowledge questions using widely accepted engineering concepts. "
            + "If you lack sufficient information, say so clearly instead of guessing.";

    private static final String UNKNOWN_SYSTEM_PROMPT = BASE_SYSTEM_PROMPT
            + " The user's request may be unclear or outside your scope. "
            + "Politely explain what you can help with and avoid fabricating facts.";

    private static final String TOOL_GROUNDING_INSTRUCTION =
            "The following tool data is authoritative and was retrieved from Engineering Learning Hub services. "
                    + "Base your answer only on this data. Do not add, change, or invent user-specific values.";

    private final ObjectMapper objectMapper;

    public PromptOrchestrator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LlmCompletionRequest buildKnowledgeRequest(String userMessage, List<AssistantMessage> conversationHistory) {
        return new LlmCompletionRequest(
                KNOWLEDGE_SYSTEM_PROMPT,
                buildMessages(conversationHistory, userMessage)
        );
    }

    public LlmCompletionRequest buildUnknownRequest(String userMessage, List<AssistantMessage> conversationHistory) {
        return new LlmCompletionRequest(
                UNKNOWN_SYSTEM_PROMPT,
                buildMessages(conversationHistory, userMessage)
        );
    }

    public LlmCompletionRequest buildToolGroundedRequest(
            String userMessage,
            String toolName,
            String sourceService,
            AssistantSourceConfidence confidence,
            ToolResult toolResult,
            List<AssistantMessage> conversationHistory
    ) {
        String systemPrompt = BASE_SYSTEM_PROMPT
                + " Respond using the authoritative tool data provided below."
                + "\n\n"
                + TOOL_GROUNDING_INSTRUCTION
                + "\n\n"
                + formatToolGrounding(toolName, sourceService, confidence, toolResult);

        return new LlmCompletionRequest(
                systemPrompt,
                buildMessages(conversationHistory, userMessage)
        );
    }

    public PromptContextType resolveContextType(com.company.learninghub.assistant.intent.AssistantIntentType intentType) {
        return switch (intentType) {
            case KNOWLEDGE -> PromptContextType.KNOWLEDGE;
            case TOOL -> PromptContextType.TOOL;
            case UNKNOWN -> PromptContextType.UNKNOWN;
            default -> PromptContextType.UNKNOWN;
        };
    }

    private List<LlmCompletionRequest.LlmMessage> buildMessages(
            List<AssistantMessage> conversationHistory,
            String currentUserMessage
    ) {
        List<LlmCompletionRequest.LlmMessage> messages = new ArrayList<>();
        if (conversationHistory != null) {
            for (AssistantMessage message : conversationHistory) {
                String role = mapRole(message.getRole());
                if (role != null && StringUtils.hasText(message.getContent())) {
                    messages.add(new LlmCompletionRequest.LlmMessage(role, message.getContent()));
                }
            }
        }
        if (StringUtils.hasText(currentUserMessage)) {
            if (messages.isEmpty()
                    || !"user".equals(messages.getLast().role())
                    || !currentUserMessage.equals(messages.getLast().content())) {
                messages.add(new LlmCompletionRequest.LlmMessage("user", currentUserMessage));
            }
        }
        return List.copyOf(messages);
    }

    private String mapRole(AssistantMessageRole role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> null;
        };
    }

    private String formatToolGrounding(
            String toolName,
            String sourceService,
            AssistantSourceConfidence confidence,
            ToolResult toolResult
    ) {
        Map<String, Object> grounding = new LinkedHashMap<>();
        grounding.put("toolName", toolName);
        grounding.put("source", sourceService);
        grounding.put("confidence", confidence == null ? AssistantSourceConfidence.HIGH.name() : confidence.name());
        grounding.put("summary", toolResult.text());
        if (toolResult.structuredData() != null) {
            grounding.put("structuredData", toolResult.structuredData());
        }
        if (!toolResult.metadata().isEmpty()) {
            grounding.put("metadata", toolResult.metadata());
        }
        return "Authoritative tool data:\n" + toJson(grounding);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }
}
