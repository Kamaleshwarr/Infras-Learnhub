package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.dto.ConversationResponse;
import com.company.learninghub.assistant.mapper.AssistantMapper;
import com.company.learninghub.assistant.repository.AssistantConversationRepository;
import com.company.learninghub.assistant.repository.AssistantMessageRepository;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AssistantConversationService {

    private final AssistantConversationRepository conversationRepository;
    private final AssistantMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public AssistantConversationService(
            AssistantConversationRepository conversationRepository,
            AssistantMessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this(conversationRepository, messageRepository, userRepository, Clock.systemUTC());
    }

    AssistantConversationService(
            AssistantConversationRepository conversationRepository,
            AssistantMessageRepository messageRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public AssistantConversation getConversationForUser(AuthenticatedUser authenticatedUser) {
        return conversationRepository.findByUserId(authenticatedUser.getId()).orElse(null);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<AssistantMessage> listMessages(AuthenticatedUser authenticatedUser) {
        AssistantConversation conversation = getConversationForUser(authenticatedUser);
        if (conversation == null) {
            return List.of();
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AssistantConversation getOrCreateConversation(AuthenticatedUser authenticatedUser) {
        return conversationRepository.findByUserId(authenticatedUser.getId())
                .orElseGet(() -> createConversation(authenticatedUser.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ConversationResponse getConversationResponse(AuthenticatedUser authenticatedUser) {
        AssistantConversation conversation = getConversationForUser(authenticatedUser);
        if (conversation == null) {
            return new ConversationResponse(null, List.of());
        }
        List<AssistantMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        return AssistantMapper.toConversationResponse(conversation, messages);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AssistantConversation resolveConversation(AuthenticatedUser authenticatedUser, UUID conversationId) {
        if (conversationId == null) {
            return getOrCreateConversation(authenticatedUser);
        }

        AssistantConversation conversation = conversationRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getId().equals(conversationId)) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        return conversation;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AssistantMessage appendMessage(
            AssistantConversation conversation,
            AssistantMessageRole role,
            String content
    ) {
        Instant now = clock.instant();
        conversation.touch(now);
        AssistantMessage message = new AssistantMessage(conversation, role, content, now);
        return messageRepository.save(message);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AssistantMessage appendMessage(
            AuthenticatedUser authenticatedUser,
            AssistantMessageRole role,
            String content
    ) {
        AssistantConversation conversation = getOrCreateConversation(authenticatedUser);
        return appendMessage(conversation, role, content);
    }

    private AssistantConversation createConversation(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
        Instant now = clock.instant();
        return conversationRepository.save(new AssistantConversation(user, now, now));
    }
}
