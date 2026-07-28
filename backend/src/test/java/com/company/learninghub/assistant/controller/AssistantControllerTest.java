package com.company.learninghub.assistant.controller;

import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.service.AssistantOrchestrationService;
import com.company.learninghub.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssistantControllerTest {

    @Test
    void statusReturnsAssistantAvailability() throws Exception {
        AssistantOrchestrationService orchestrationService = mock(AssistantOrchestrationService.class);
        when(orchestrationService.getStatus()).thenReturn(new AssistantStatusResponse(false, "mock", true));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AssistantController(orchestrationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/assistant/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.llmProvider").value("mock"))
                .andExpect(jsonPath("$.llmHealthy").value(true));
    }
}
