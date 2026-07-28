package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.llm.LlmClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssistantOrchestrationServiceTest {

    @Test
    void getStatusReflectsFeatureFlagAndProviderHealth() {
        AssistantProperties properties = new AssistantProperties();
        properties.setEnabled(false);

        LlmClient llmClient = mock(LlmClient.class);
        when(llmClient.providerName()).thenReturn("mock");
        when(llmClient.isHealthy()).thenReturn(true);

        AssistantOrchestrationService service = new AssistantOrchestrationService(properties, llmClient);

        AssistantStatusResponse status = service.getStatus();

        assertThat(status.enabled()).isFalse();
        assertThat(status.llmProvider()).isEqualTo("mock");
        assertThat(status.llmHealthy()).isTrue();
    }
}
