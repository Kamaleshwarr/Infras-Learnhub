package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.dto.AssistantOrchestrationResponse;
import com.company.learninghub.assistant.dto.AssistantOutcomeType;
import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantResponse;
import com.company.learninghub.assistant.dto.AssistantSourceConfidence;
import com.company.learninghub.assistant.dto.AssistantSourceResponse;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.dto.ConversationResponse;
import com.company.learninghub.assistant.dto.NavigationInstruction;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.IntentResolver;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.assistant.llm.LlmClient;
import com.company.learninghub.assistant.llm.LlmCompletionRequest;
import com.company.learninghub.assistant.llm.LlmCompletionResult;
import com.company.learninghub.assistant.llm.PromptOrchestrator;
import com.company.learninghub.assistant.tool.AssistantToolContext;
import com.company.learninghub.assistant.tool.AssistantToolNames;
import com.company.learninghub.assistant.tool.AssistantToolRegistry;
import com.company.learninghub.assistant.tool.ToolResult;
import com.company.learninghub.auth.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates assistant requests including chat pipeline, intent resolution, tool execution,
 * prompt orchestration, and LLM responses.
 */
@Service
public class AssistantOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AssistantOrchestrationService.class);

    private static final String LLM_FALLBACK_MESSAGE =
            "I don't currently have enough information to answer this. "
                    + "Future versions will support broader AI knowledge.";

    private static final Map<String, String> TOOL_SERVICE_NAMES = Map.of(
            AssistantToolNames.MY_PROFILE, "ProfileService",
            AssistantToolNames.MY_LEADERBOARD_RANK, "LeaderboardService",
            AssistantToolNames.MY_CERTIFICATIONS, "CertificateSubmissionService",
            AssistantToolNames.AVAILABLE_LEARNING_INITIATIVES, "LearningInitiativeService"
    );

    private final AssistantProperties assistantProperties;
    private final LlmClient llmClient;
    private final PromptOrchestrator promptOrchestrator;
    private final IntentResolver intentResolver;
    private final AssistantToolRegistry toolRegistry;
    private final AssistantConversationService conversationService;

    public AssistantOrchestrationService(
            AssistantProperties assistantProperties,
            LlmClient llmClient,
            PromptOrchestrator promptOrchestrator,
            IntentResolver intentResolver,
            AssistantToolRegistry toolRegistry,
            AssistantConversationService conversationService
    ) {
        this.assistantProperties = assistantProperties;
        this.llmClient = llmClient;
        this.promptOrchestrator = promptOrchestrator;
        this.intentResolver = intentResolver;
        this.toolRegistry = toolRegistry;
        this.conversationService = conversationService;
    }

    public AssistantStatusResponse getStatus() {
        return new AssistantStatusResponse(
                assistantProperties.isEnabled(),
                llmClient.providerName(),
                llmClient.isHealthy()
        );
    }

    @Transactional
    public AssistantResponse chat(AssistantRequest request, AuthenticatedUser authenticatedUser) {
        requireEnabled();

        AssistantConversation conversation = conversationService.resolveConversation(
                authenticatedUser,
                request.conversationId()
        );
        List<AssistantMessage> historyBeforeCurrentMessage = conversationService.listMessages(authenticatedUser);
        conversationService.appendMessage(conversation, AssistantMessageRole.USER, request.message());

        AssistantOrchestrationResponse orchestration = processRequest(
                request,
                authenticatedUser,
                historyBeforeCurrentMessage
        );
        conversationService.appendMessage(conversation, AssistantMessageRole.ASSISTANT, orchestration.message());

        return toAssistantResponse(conversation, orchestration);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(AuthenticatedUser authenticatedUser) {
        requireEnabled();
        return conversationService.getConversationResponse(authenticatedUser);
    }

    public AssistantOrchestrationResponse processRequest(
            AssistantRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        return processRequest(request, authenticatedUser, List.of());
    }

    public AssistantOrchestrationResponse processRequest(
            AssistantRequest request,
            AuthenticatedUser authenticatedUser,
            List<AssistantMessage> conversationHistory
    ) {
        if (!assistantProperties.isEnabled()) {
            return AssistantOrchestrationResponse.disabled();
        }

        List<AssistantMessage> history = conversationHistory == null ? List.of() : conversationHistory;
        ResolvedIntent intent = intentResolver.resolve(request.message());
        log.debug("Resolved assistant intent: type={}, normalizedMessage={}", intent.type(), intent.normalizedMessage());
        return switch (intent.type()) {
            case NAVIGATION -> AssistantOrchestrationResponse.navigation(
                    intent.type(),
                    new NavigationInstruction(
                            intent.navigationTarget().path(),
                            intent.navigationTarget().label()
                    )
            );
            case TOOL -> {
                ToolResult toolResult = toolRegistry.execute(
                        intent,
                        new AssistantToolContext(authenticatedUser, request.message())
                );
                String message = completeWithGroundedTool(request.message(), intent.toolName(), toolResult, history);
                yield AssistantOrchestrationResponse.tool(intent.type(), intent.toolName(), toolResult, message);
            }
            case KNOWLEDGE -> AssistantOrchestrationResponse.knowledge(
                    completeWithLlm(request.message(), history)
            );
            case UNKNOWN -> AssistantOrchestrationResponse.unknown(
                    completeWithLlm(request.message(), history)
            );
        };
    }

    private String completeWithLlm(String message, List<AssistantMessage> conversationHistory) {
        LlmCompletionRequest request = promptOrchestrator.buildKnowledgeRequest(message, conversationHistory);
        return resolveLlmContent(request, LLM_FALLBACK_MESSAGE);
    }

    private String completeWithGroundedTool(
            String userMessage,
            String toolName,
            ToolResult toolResult,
            List<AssistantMessage> conversationHistory
    ) {
        LlmCompletionRequest request = promptOrchestrator.buildToolGroundedRequest(
                userMessage,
                toolName,
                toolResult,
                conversationHistory
        );
        String fallback = StringUtils.hasText(toolResult.text()) ? toolResult.text() : LLM_FALLBACK_MESSAGE;
        return resolveLlmContent(request, fallback);
    }

    private String resolveLlmContent(LlmCompletionRequest request, String fallbackMessage) {
        LlmCompletionResult result = llmClient.complete(request);
        if (!result.success() || result.content() == null) {
            log.warn(
                    "LLM completion unavailable; using fallback response. provider={}, success={}, error={}",
                    llmClient.providerName(),
                    result.success(),
                    result.errorMessage()
            );
            return fallbackMessage;
        }
        return result.content();
    }

    private AssistantResponse toAssistantResponse(
            AssistantConversation conversation,
            AssistantOrchestrationResponse orchestration
    ) {
        return new AssistantResponse(
                orchestration.message(),
                conversation.getId(),
                orchestration.intentType(),
                resolveToolUsed(orchestration),
                resolveSources(orchestration),
                resolveConfidence(orchestration),
                resolveMetadata(orchestration)
        );
    }

    private String resolveToolUsed(AssistantOrchestrationResponse orchestration) {
        if (orchestration.outcomeType() != AssistantOutcomeType.TOOL) {
            return null;
        }
        return orchestration.toolName();
    }

    private AssistantSourceConfidence resolveConfidence(AssistantOrchestrationResponse orchestration) {
        return switch (orchestration.outcomeType()) {
            case TOOL -> AssistantSourceConfidence.HIGH;
            case KNOWLEDGE, UNKNOWN -> AssistantSourceConfidence.LOW;
            default -> null;
        };
    }

    private List<AssistantSourceResponse> resolveSources(AssistantOrchestrationResponse orchestration) {
        return switch (orchestration.outcomeType()) {
            case TOOL -> {
                String toolName = resolveToolUsed(orchestration);
                if (toolName == null || toolName.isBlank()) {
                    yield List.of();
                }
                yield List.of(new AssistantSourceResponse(
                        TOOL_SERVICE_NAMES.getOrDefault(toolName, "AssistantTool"),
                        toolName,
                        AssistantSourceConfidence.HIGH
                ));
            }
            case KNOWLEDGE, UNKNOWN -> List.of(new AssistantSourceResponse(
                    llmClient.providerName(),
                    null,
                    AssistantSourceConfidence.LOW
            ));
            default -> List.of();
        };
    }

    private Map<String, Object> resolveMetadata(AssistantOrchestrationResponse orchestration) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        if (orchestration.outcomeType() == AssistantOutcomeType.NAVIGATION && orchestration.navigation() != null) {
            metadata.put("navigation", Map.of(
                    "path", orchestration.navigation().path(),
                    "label", orchestration.navigation().label()
            ));
        }

        if (orchestration.outcomeType() == AssistantOutcomeType.TOOL && orchestration.toolResult() != null) {
            ToolResult toolResult = orchestration.toolResult();
            metadata.put("grounding", buildToolGroundingMetadata(orchestration.toolName(), toolResult));
            if (toolResult.structuredData() != null) {
                metadata.put("structuredData", toolResult.structuredData());
            }
            if (!toolResult.metadata().isEmpty()) {
                metadata.put("toolMetadata", toolResult.metadata());
            }
        }

        if (orchestration.outcomeType() == AssistantOutcomeType.KNOWLEDGE
                || orchestration.outcomeType() == AssistantOutcomeType.UNKNOWN) {
            metadata.put("llmProvider", llmClient.providerName());
        }

        return metadata;
    }

    private Map<String, Object> buildToolGroundingMetadata(String toolName, ToolResult toolResult) {
        Map<String, Object> grounding = new LinkedHashMap<>();
        grounding.put("toolName", toolName);
        grounding.put("source", TOOL_SERVICE_NAMES.getOrDefault(toolName, "AssistantTool"));
        grounding.put("confidence", AssistantSourceConfidence.HIGH.name());
        grounding.put("summary", toolResult.text());
        if (toolResult.structuredData() != null) {
            grounding.put("structuredData", toolResult.structuredData());
        }
        grounding.put("authoritative", true);
        return grounding;
    }

    private void requireEnabled() {
        if (!assistantProperties.isEnabled()) {
            throw new AssistantDisabledException("The AI assistant is not enabled in this deployment.");
        }
    }
}
