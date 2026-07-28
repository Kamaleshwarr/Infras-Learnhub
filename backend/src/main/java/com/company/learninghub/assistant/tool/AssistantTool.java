package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.ResolvedIntent;

public interface AssistantTool {

    String getName();

    boolean supports(ResolvedIntent intent);

    ToolResult execute(AssistantToolContext context);
}
