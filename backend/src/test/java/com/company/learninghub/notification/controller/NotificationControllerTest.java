package com.company.learninghub.notification.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.GlobalExceptionHandler;
import com.company.learninghub.notification.domain.NotificationEntityType;
import com.company.learninghub.notification.domain.NotificationType;
import com.company.learninghub.notification.dto.NotificationResponse;
import com.company.learninghub.notification.dto.UnreadCountResponse;
import com.company.learninghub.notification.service.NotificationService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificationController(notificationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        AuthenticatedUser.from(employeeUser()),
                        null,
                        AuthenticatedUser.from(employeeUser()).getAuthorities()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listReturnsPagedNotifications() throws Exception {
        UUID notificationId = UUID.randomUUID();
        NotificationResponse response = new NotificationResponse(
                notificationId,
                NotificationType.ACCOUNT_CREATED,
                "Welcome to Learning Hub",
                "Your account has been created.",
                NotificationEntityType.USER,
                UUID.randomUUID(),
                "/change-password",
                false,
                null,
                Instant.parse("2026-06-16T10:00:00Z")
        );
        Page<NotificationResponse> page = new PageImpl<>(List.of(response));
        when(notificationService.list(isNull(), isNull(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$.content[0].type").value("ACCOUNT_CREATED"))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    void unreadCountReturnsCount() throws Exception {
        when(notificationService.unreadCount(any())).thenReturn(new UnreadCountResponse(3));

        mockMvc.perform(get("/api/v1/notifications/unread-count").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void markReadReturnsUpdatedNotification() throws Exception {
        UUID notificationId = UUID.randomUUID();
        NotificationResponse response = new NotificationResponse(
                notificationId,
                NotificationType.CERTIFICATE_APPROVED,
                "Certificate approved",
                "Approved message",
                NotificationEntityType.CERTIFICATE_SUBMISSION,
                UUID.randomUUID(),
                "/submissions",
                true,
                Instant.parse("2026-06-16T11:00:00Z"),
                Instant.parse("2026-06-16T10:00:00Z")
        );
        when(notificationService.markRead(eq(notificationId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notificationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAtUtc").value("2026-06-16T11:00:00Z"));
    }

    @Test
    void markAllReadReturnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllRead(any());
    }

    private User employeeUser() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return user;
    }
}
