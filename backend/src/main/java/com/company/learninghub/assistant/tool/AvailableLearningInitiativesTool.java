package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.service.LearningInitiativeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AvailableLearningInitiativesTool implements AssistantTool {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final LearningInitiativeService learningInitiativeService;

    public AvailableLearningInitiativesTool(LearningInitiativeService learningInitiativeService) {
        this.learningInitiativeService = learningInitiativeService;
    }

    @Override
    public String getName() {
        return AssistantToolNames.AVAILABLE_LEARNING_INITIATIVES;
    }

    @Override
    public boolean supports(ResolvedIntent intent) {
        return intent.type() == AssistantIntentType.TOOL
                && AssistantToolNames.AVAILABLE_LEARNING_INITIATIVES.equals(intent.toolName());
    }

    @Override
    public ToolResult execute(AssistantToolContext context) {
        Page<InitiativeResponse> initiatives = learningInitiativeService.list(
                null,
                null,
                PageRequest.of(0, DEFAULT_PAGE_SIZE),
                context.authenticatedUser()
        );
        String text = initiatives.getTotalElements() == 0
                ? "No learning initiatives are currently available."
                : "Found %d learning initiative(s).".formatted(initiatives.getTotalElements());
        return ToolResult.structured(text, PageResponse.from(initiatives));
    }
}
