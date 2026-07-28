package com.company.learninghub.assistant.controller;

import com.company.learninghub.assistant.dto.AssistantStatusResponse;
import com.company.learninghub.assistant.service.AssistantOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "AI Assistant", description = "Engineering Learning Hub AI assistant APIs")
@SecurityRequirement(name = "bearerAuth")
public class AssistantController {

    private final AssistantOrchestrationService orchestrationService;

    public AssistantController(AssistantOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get AI assistant availability and provider status for the current deployment")
    public ResponseEntity<AssistantStatusResponse> status() {
        return ResponseEntity.ok(orchestrationService.getStatus());
    }
}
