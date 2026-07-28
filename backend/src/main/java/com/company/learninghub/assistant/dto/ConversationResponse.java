package com.company.learninghub.assistant.dto;

import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID conversationId,
        List<ConversationMessageResponse> messages
) {
}
