package com.company.learninghub.assistant.llm;

import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.tool.ToolResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class PromptOrchestrator {

    public static final String TOOL_CONTEXT_MARKER = "AUTHORITATIVE_TOOL_DATA:";

    static final String BASE_SYSTEM_INSTRUCTIONS =
            "You are the Engineering Learning Hub assistant. "
                    + "Provide concise, helpful responses grounded in Engineering Learning Hub workflows. "
                    + "Never invent user-specific platform data such as certifications, projects, rankings, "
                    + "or profile details unless it is explicitly provided in the conversation or tool data.";

    static final String KNOWLEDGE_INSTRUCTIONS =
            " Answer general technology and platform workflow questions. "
                    + "If you lack enough information, say so clearly instead of guessing.";

    static final String TOOL_GROUNDING_INSTRUCTIONS =
            " The user asked a question that was answered using live platform data. "
                    + "Use only the authoritative tool data provided below. "
                    + "Do not add, change, or invent any platform facts beyond that data. "
                    + "You may rephrase for clarity and suggest relevant next steps within the platform.";

    private final ObjectMapper objectMapper;

    public PromptOrchestrator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LlmCompletionRequest buildKnowledgeRequest(
            String userMessage,
            List<AssistantMessage> conversationHistory
    ) {
        return new LlmCompletionRequest(
                BASE_SYSTEM_INSTRUCTIONS + KNOWLEDGE_INSTRUCTIONS,
                buildMessageList(conversationHistory, userMessage)
        );
    }

    public LlmCompletionRequest buildToolGroundedRequest(
            String userMessage,
            String toolName,
            ToolResult toolResult,
            List<AssistantMessage> conversationHistory
    ) {
        String groundedUserMessage = """
                User question: %s

                %s
                Tool name: %s
                %s
                """.formatted(
                userMessage.trim(),
                TOOL_CONTEXT_MARKER,
                toolName,
                formatToolData(toolResult)
        );

        return new LlmCompletionRequest(
                BASE_SYSTEM_INSTRUCTIONS + TOOL_GROUNDING_INSTRUCTIONS,
                buildMessageList(conversationHistory, groundedUserMessage)
        );
    }

    public List<LlmCompletionRequest.LlmMessage> toLlmMessages(List<AssistantMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return List.of();
        }

        List<LlmCompletionRequest.LlmMessage> messages = new ArrayList<>();
        for (AssistantMessage message : conversationHistory) {
            String role = toLlmRole(message.getRole());
            if (role != null && StringUtils.hasText(message.getContent())) {
                messages.add(new LlmCompletionRequest.LlmMessage(role, message.getContent()));
            }
        }
        return List.copyOf(messages);
    }

    private List<LlmCompletionRequest.LlmMessage> buildMessageList(
            List<AssistantMessage> conversationHistory,
            String currentUserMessage
    ) {
        List<LlmCompletionRequest.LlmMessage> messages = new ArrayList<>(toLlmMessages(conversationHistory));
        messages.add(new LlmCompletionRequest.LlmMessage("user", currentUserMessage));
        return List.copyOf(messages);
    }

    private String formatToolData(ToolResult toolResult) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(toolResult.text())) {
            builder.append("Summary: ").append(toolResult.text().trim());
        }
        if (toolResult.structuredData() != null) {
            builder.append(System.lineSeparator())
                    .append("Structured data: ")
                    .append(serializeStructuredData(toolResult.structuredData()));
        }
        if (StringUtils.hasText(toolResult.markdown())) {
            builder.append(System.lineSeparator())
                    .append("Markdown: ")
                    .append(toolResult.markdown().trim());
        }
        if (!toolResult.metadata().isEmpty()) {
            builder.append(System.lineSeparator())
                    .append("Metadata: ")
                    .append(serializeStructuredData(toolResult.metadata()));
        }
        return builder.toString().trim();
    }

    private String serializeStructuredData(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    private String toLlmRole(AssistantMessageRole role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
        };
    }

    public static boolean containsToolContext(String content) {
        return StringUtils.hasText(content)
                && content.contains(TOOL_CONTEXT_MARKER);
    }

    public static String extractToolSummary(String content) {
        if (!containsToolContext(content)) {
            return null;
        }
        int summaryIndex = content.toLowerCase(Locale.ROOT).indexOf("summary:");
        if (summaryIndex < 0) {
            return null;
        }
        String summarySection = content.substring(summaryIndex + "summary:".length());
        int structuredIndex = summarySection.toLowerCase(Locale.ROOT).indexOf("structured data:");
        if (structuredIndex >= 0) {
            summarySection = summarySection.substring(0, structuredIndex);
        }
        return summarySection.trim();
    }
}
