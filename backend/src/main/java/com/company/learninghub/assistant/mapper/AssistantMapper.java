package com.company.learninghub.assistant.mapper;

import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.dto.ConversationMessageResponse;
import com.company.learninghub.assistant.dto.ConversationResponse;

import java.util.List;

public final class AssistantMapper {

    private AssistantMapper() {
    }

    public static ConversationResponse toConversationResponse(
            AssistantConversation conversation,
            List<AssistantMessage> messages
    ) {
        if (conversation == null) {
            return new ConversationResponse(null, List.of());
        }
        return new ConversationResponse(
                conversation.getId(),
                messages.stream().map(AssistantMapper::toMessageResponse).toList()
        );
    }

    public static ConversationMessageResponse toMessageResponse(AssistantMessage message) {
        return new ConversationMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
