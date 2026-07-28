package com.company.learninghub.assistant.intent;

public record ResolvedIntent(
        AssistantIntentType type,
        NavigationTarget navigationTarget,
        String toolName,
        String normalizedMessage
) {

    public static ResolvedIntent navigation(NavigationTarget navigationTarget, String normalizedMessage) {
        return new ResolvedIntent(AssistantIntentType.NAVIGATION, navigationTarget, null, normalizedMessage);
    }

    public static ResolvedIntent tool(String toolName, String normalizedMessage) {
        return new ResolvedIntent(AssistantIntentType.TOOL, null, toolName, normalizedMessage);
    }

    public static ResolvedIntent knowledge(String normalizedMessage) {
        return new ResolvedIntent(AssistantIntentType.KNOWLEDGE, null, null, normalizedMessage);
    }

    public static ResolvedIntent unknown(String normalizedMessage) {
        return new ResolvedIntent(AssistantIntentType.UNKNOWN, null, null, normalizedMessage);
    }
}
