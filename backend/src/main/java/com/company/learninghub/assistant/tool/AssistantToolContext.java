package com.company.learninghub.assistant.tool;

import com.company.learninghub.auth.security.AuthenticatedUser;

public record AssistantToolContext(
        AuthenticatedUser authenticatedUser,
        String message
) {
}
