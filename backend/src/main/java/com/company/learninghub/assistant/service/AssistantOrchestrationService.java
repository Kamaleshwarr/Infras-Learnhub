package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.llm.LlmClient;
import org.springframework.stereotype.Service;

/**
 * Orchestrates assistant requests. Phase 1 provides status reporting only;
 * chat orchestration will be added in a later phase.
 */
@Service
public class AssistantOrchestrationService {

    private final AssistantProperties assistantProperties;
    private final LlmClient llmClient;

    public AssistantOrchestrationService(AssistantProperties assistantProperties, LlmClient llmClient) {
        this.assistantProperties = assistantProperties;
        this.llmClient = llmClient;
    }

    public AssistantStatusResponse getStatus() {
        return new AssistantStatusResponse(
                assistantProperties.isEnabled(),
                llmClient.providerName(),
                llmClient.isHealthy()
        );
    }
}
