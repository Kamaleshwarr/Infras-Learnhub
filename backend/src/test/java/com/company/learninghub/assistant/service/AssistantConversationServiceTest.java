package com.company.learninghub.assistant.service;

import com.company.learninghub.assistant.domain.AssistantConversation;
import com.company.learninghub.assistant.domain.AssistantMessage;
import com.company.learninghub.assistant.domain.AssistantMessageRole;
import com.company.learninghub.assistant.repository.AssistantConversationRepository;
import com.company.learninghub.assistant.repository.AssistantMessageRepository;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantConversationServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-07-28T10:00:00Z");

    @Mock
    private AssistantConversationRepository conversationRepository;

    @Mock
    private AssistantMessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    private AssistantConversationService conversationService;
    private AuthenticatedUser authenticatedUser;
    private User user;

    @BeforeEach
    void setUp() {
        conversationService = new AssistantConversationService(
                conversationRepository,
                messageRepository,
                userRepository,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
        user = employeeUser();
        authenticatedUser = AuthenticatedUser.from(user);
    }

    @Test
    void getOrCreateConversationCreatesSingleConversationPerUser() {
        when(conversationRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(conversationRepository.save(any(AssistantConversation.class))).thenAnswer(invocation -> {
            AssistantConversation conversation = invocation.getArgument(0);
            return conversation;
        });

        AssistantConversation conversation = conversationService.getOrCreateConversation(authenticatedUser);

        assertThat(conversation.getUser()).isSameAs(user);
        assertThat(conversation.getCreatedAt()).isEqualTo(FIXED_NOW);
        assertThat(conversation.getUpdatedAt()).isEqualTo(FIXED_NOW);

        ArgumentCaptor<AssistantConversation> captor = ArgumentCaptor.forClass(AssistantConversation.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void listMessagesReturnsEmptyWhenConversationDoesNotExist() {
        when(conversationRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        List<AssistantMessage> messages = conversationService.listMessages(authenticatedUser);

        assertThat(messages).isEmpty();
    }

    @Test
    void appendMessagePersistsMessageForUserConversation() {
        AssistantConversation conversation = new AssistantConversation(user, FIXED_NOW, FIXED_NOW);
        when(conversationRepository.findByUserId(user.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(AssistantMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssistantMessage message = conversationService.appendMessage(
                authenticatedUser,
                AssistantMessageRole.USER,
                "Hello"
        );

        assertThat(message.getRole()).isEqualTo(AssistantMessageRole.USER);
        assertThat(message.getContent()).isEqualTo("Hello");
        assertThat(message.getCreatedAt()).isEqualTo(FIXED_NOW);
        assertThat(conversation.getUpdatedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void resolveConversationCreatesConversationWhenIdMissing() {
        when(conversationRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(conversationRepository.save(any(AssistantConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssistantConversation conversation = conversationService.resolveConversation(authenticatedUser, null);

        assertThat(conversation.getUser()).isSameAs(user);
    }

    @Test
    void resolveConversationReturnsExistingConversationWhenIdMatches() {
        UUID conversationId = UUID.randomUUID();
        AssistantConversation conversation = new AssistantConversation(user, FIXED_NOW, FIXED_NOW);
        ReflectionTestUtils.setField(conversation, "id", conversationId);
        when(conversationRepository.findByUserId(user.getId())).thenReturn(Optional.of(conversation));

        AssistantConversation resolved = conversationService.resolveConversation(authenticatedUser, conversationId);

        assertThat(resolved).isSameAs(conversation);
    }

    @Test
    void resolveConversationThrowsWhenIdDoesNotMatch() {
        AssistantConversation conversation = new AssistantConversation(user, FIXED_NOW, FIXED_NOW);
        ReflectionTestUtils.setField(conversation, "id", UUID.randomUUID());
        when(conversationRepository.findByUserId(user.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.resolveConversation(authenticatedUser, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static User employeeUser() {
        User employee = new User("EMP-001", "employee@learninghub.local", "Employee User", "$2a$12$hash");
        employee.assignRole(new Role(RoleName.EMPLOYEE));
        return employee;
    }
}
