package com.company.learninghub.assistant.controller;

import com.company.learninghub.assistant.dto.AssistantLlmDebugResponse;
import com.company.learninghub.assistant.dto.AssistantRequest;
import com.company.learninghub.assistant.dto.AssistantResponse;
import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.dto.ConversationResponse;
import com.company.learninghub.assistant.llm.OpenAiCompatibleClient;
import com.company.learninghub.assistant.service.AssistantDisabledException;
import com.company.learninghub.assistant.service.AssistantOrchestrationService;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "AI Assistant", description = "Engineering Learning Hub AI assistant APIs")
@SecurityRequirement(name = "bearerAuth")
public class AssistantController {

    private final AssistantOrchestrationService orchestrationService;
    private final OpenAiCompatibleClient openAiCompatibleClient;

    public AssistantController(
            AssistantOrchestrationService orchestrationService,
            OpenAiCompatibleClient openAiCompatibleClient
    ) {
        this.orchestrationService = orchestrationService;
        this.openAiCompatibleClient = openAiCompatibleClient;
    }

    @GetMapping("/status")
    @Operation(summary = "Get AI assistant availability and provider status for the current deployment")
    public ResponseEntity<AssistantStatusResponse> status() {
        return ResponseEntity.ok(orchestrationService.getStatus());
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the AI assistant")
    public ResponseEntity<AssistantResponse> chat(
            @Valid @RequestBody AssistantRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(orchestrationService.chat(request, authenticatedUser));
    }

    @GetMapping("/debug")
    @Operation(summary = "Temporary diagnostic probe that calls OpenAiCompatibleClient directly")
    public ResponseEntity<AssistantLlmDebugResponse> debug() {
        return ResponseEntity.ok(openAiCompatibleClient.probeDirectLlmCall());
    }

    @GetMapping("/conversation")
    @Operation(summary = "Get the current user's assistant conversation history")
    public ResponseEntity<ConversationResponse> conversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(orchestrationService.getConversation(authenticatedUser));
    }

    @ExceptionHandler(AssistantDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAssistantDisabled(
            AssistantDisabledException exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
}
