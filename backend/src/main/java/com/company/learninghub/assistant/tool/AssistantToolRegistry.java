package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.ResolvedIntent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssistantToolRegistry {

    private final List<AssistantTool> tools;

    public AssistantToolRegistry(List<AssistantTool> tools) {
        this.tools = List.copyOf(tools);
    }

    public Optional<AssistantTool> findTool(ResolvedIntent intent) {
        return tools.stream()
                .filter(tool -> tool.supports(intent))
                .findFirst();
    }

    public ToolResult execute(ResolvedIntent intent, AssistantToolContext context) {
        return findTool(intent)
                .map(tool -> tool.execute(context))
                .orElseThrow(() -> new IllegalStateException("No assistant tool registered for intent: " + intent.toolName()));
    }

    public List<AssistantTool> getTools() {
        return tools;
    }
}
