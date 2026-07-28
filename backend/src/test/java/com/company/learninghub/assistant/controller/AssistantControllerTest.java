package com.company.learninghub.assistant.controller;

import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantResponse;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.dto.ConversationMessageResponse;
import com.company.learninghub.assistant.dto.ConversationResponse;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.service.AssistantDisabledException;
import com.company.learninghub.assistant.service.AssistantOrchestrationService;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.GlobalExceptionHandler;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssistantControllerTest {

    private AssistantOrchestrationService orchestrationService;
    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private ObjectMapper objectMapper;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        orchestrationService = mock(AssistantOrchestrationService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AssistantController(orchestrationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        authenticatedUser = AuthenticatedUser.from(user);
        objectMapper = new ObjectMapper();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null, authenticatedUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        validator.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void statusReturnsAssistantAvailability() throws Exception {
        when(orchestrationService.getStatus()).thenReturn(new AssistantStatusResponse(false, "mock", true));

        mockMvc.perform(get("/api/v1/assistant/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.llmProvider").value("mock"))
                .andExpect(jsonPath("$.llmHealthy").value(true));
    }

    @Test
    void chatReturnsAssistantResponse() throws Exception {
        UUID conversationId = UUID.randomUUID();
        when(orchestrationService.chat(any(AssistantRequest.class), eq(authenticatedUser)))
                .thenReturn(new AssistantResponse(
                        "Navigate to Projects.",
                        conversationId,
                        AssistantIntentType.NAVIGATION,
                        null,
                        List.of(),
                        Map.of("navigation", Map.of("path", "/projects", "label", "Projects"))
                ));

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssistantRequest("open projects", null)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Navigate to Projects."))
                .andExpect(jsonPath("$.conversationId").value(conversationId.toString()))
                .andExpect(jsonPath("$.intentType").value("NAVIGATION"));
    }

    @Test
    void chatReturnsServiceUnavailableWhenAssistantDisabled() throws Exception {
        when(orchestrationService.chat(any(AssistantRequest.class), eq(authenticatedUser)))
                .thenThrow(new AssistantDisabledException("The AI assistant is not enabled in this deployment."));

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssistantRequest("hello", null)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("The AI assistant is not enabled in this deployment."));
    }

    @Test
    void conversationReturnsMessages() throws Exception {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        when(orchestrationService.getConversation(authenticatedUser))
                .thenReturn(new ConversationResponse(
                        conversationId,
                        List.of(new ConversationMessageResponse(
                                messageId,
                                AssistantMessageRole.USER,
                                "hello",
                                Instant.parse("2026-07-28T10:00:00Z")
                        ))
                ));

        mockMvc.perform(get("/api/v1/assistant/conversation")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId.toString()))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[0].content").value("hello"));
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
