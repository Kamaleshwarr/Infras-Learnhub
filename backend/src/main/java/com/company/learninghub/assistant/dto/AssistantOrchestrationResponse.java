package com.company.learninghub.assistant.dto;

import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.tool.ToolResult;

public record AssistantOrchestrationResponse(
        AssistantOutcomeType outcomeType,
        AssistantIntentType intentType,
        NavigationInstruction navigation,
        ToolResult toolResult,
        String toolName,
        String message
) {

    public static AssistantOrchestrationResponse disabled() {
        return new AssistantOrchestrationResponse(
                AssistantOutcomeType.DISABLED,
                null,
                null,
                null,
                null,
                "The AI assistant is not enabled in this deployment."
        );
    }

    public static AssistantOrchestrationResponse navigation(AssistantIntentType intentType, NavigationInstruction navigation) {
        return new AssistantOrchestrationResponse(
                AssistantOutcomeType.NAVIGATION,
                intentType,
                navigation,
                null,
                null,
                "Navigate to " + navigation.label() + "."
        );
    }

    public static AssistantOrchestrationResponse tool(
            AssistantIntentType intentType,
            String toolName,
            ToolResult toolResult
    ) {
        return new AssistantOrchestrationResponse(
                AssistantOutcomeType.TOOL,
                intentType,
                null,
                toolResult,
                toolName,
                toolResult.text()
        );
    }

    public static AssistantOrchestrationResponse knowledge(String message) {
        return new AssistantOrchestrationResponse(
                AssistantOutcomeType.KNOWLEDGE,
                AssistantIntentType.KNOWLEDGE,
                null,
                null,
                null,
                message
        );
    }

    public static AssistantOrchestrationResponse unknown(String message) {
        return new AssistantOrchestrationResponse(
                AssistantOutcomeType.UNKNOWN,
                AssistantIntentType.UNKNOWN,
                null,
                null,
                null,
                message
        );
    }
}
