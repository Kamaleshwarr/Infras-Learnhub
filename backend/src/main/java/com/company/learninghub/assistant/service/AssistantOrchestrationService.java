package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.dto.AssistantOrchestrationResponse;
import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.dto.NavigationInstruction;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.IntentResolver;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.tool.AssistantToolContext;
import com.company.learninghub.assistant.tool.AssistantToolRegistry;
import com.company.learninghub.assistant.tool.ToolResult;
import com.company.learninghub.auth.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

/**
 * Orchestrates assistant requests. Phase 2 adds intent resolution and read-only tool execution.
 * Chat endpoints and LLM orchestration remain deferred to later phases.
 */
@Service
public class AssistantOrchestrationService {

    private static final String KNOWLEDGE_PLACEHOLDER_MESSAGE =
            "Knowledge answers are not available yet. This capability will be added in a future release.";

    private static final String UNKNOWN_MESSAGE =
            "I did not understand that request. Try asking about your profile, certifications, leaderboard rank, "
                    + "available learning initiatives, or navigation commands such as open projects or go to learn.";

    private final AssistantProperties assistantProperties;
    private final LlmClient llmClient;
    private final IntentResolver intentResolver;
    private final AssistantToolRegistry toolRegistry;

    public AssistantOrchestrationService(
            AssistantProperties assistantProperties,
            LlmClient llmClient,
            IntentResolver intentResolver,
            AssistantToolRegistry toolRegistry
    ) {
        this.assistantProperties = assistantProperties;
        this.llmClient = llmClient;
        this.intentResolver = intentResolver;
        this.toolRegistry = toolRegistry;
    }

    public AssistantStatusResponse getStatus() {
        return new AssistantStatusResponse(
                assistantProperties.isEnabled(),
                llmClient.providerName(),
                llmClient.isHealthy()
        );
    }

    public AssistantOrchestrationResponse processRequest(AssistantRequest request, AuthenticatedUser authenticatedUser) {
        if (!assistantProperties.isEnabled()) {
            return AssistantOrchestrationResponse.disabled();
        }

        ResolvedIntent intent = intentResolver.resolve(request.message());
        return switch (intent.type()) {
            case NAVIGATION -> AssistantOrchestrationResponse.navigation(
                    intent.type(),
                    new NavigationInstruction(
                            intent.navigationTarget().path(),
                            intent.navigationTarget().label()
                    )
            );
            case TOOL -> {
                ToolResult toolResult = toolRegistry.execute(intent, new AssistantToolContext(authenticatedUser, request.message()));
                yield AssistantOrchestrationResponse.tool(intent.type(), toolResult);
            }
            case KNOWLEDGE -> AssistantOrchestrationResponse.knowledgePlaceholder(KNOWLEDGE_PLACEHOLDER_MESSAGE);
            case UNKNOWN -> AssistantOrchestrationResponse.unknown(UNKNOWN_MESSAGE);
        };
    }
}
