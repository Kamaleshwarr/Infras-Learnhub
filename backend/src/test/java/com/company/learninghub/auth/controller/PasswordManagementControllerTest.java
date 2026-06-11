package com.company.learninghub.auth.controller;

import com.company.learninghub.auth.dto.ChangePasswordRequest;
import com.company.learninghub.auth.dto.ForgotPasswordRequest;
import com.company.learninghub.auth.dto.ResetPasswordRequest;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.service.AuthenticationService;
import com.company.learninghub.auth.service.PasswordResetService;
import com.company.learninghub.auth.service.PasswordService;
import com.company.learninghub.common.exception.GlobalExceptionHandler;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordManagementControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PasswordService passwordService;
    private PasswordResetService passwordResetService;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        passwordService = mock(PasswordService.class);
        passwordResetService = mock(PasswordResetService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthenticationController(
                        mock(AuthenticationService.class),
                        passwordService,
                        passwordResetService
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void changePasswordReturnsNoContent() throws Exception {
        User user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        authenticatedUser,
                        null,
                        authenticatedUser.getAuthorities()
                )
        );
        ChangePasswordRequest request = new ChangePasswordRequest("CurrentPass1!", "NewSecure1!", "NewSecure1!");
        doNothing().when(passwordService).changePassword(any(AuthenticatedUser.class), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(passwordService).changePassword(any(AuthenticatedUser.class), any(ChangePasswordRequest.class));
    }

    @Test
    void forgotPasswordReturnsAcceptedMessage() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("employee@example.com");
        doNothing().when(passwordResetService).requestPasswordReset(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "If an account exists for that email, password reset instructions have been sent."
                ));
    }

    @Test
    void resetPasswordReturnsNoContent() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("token", "NewSecure1!", "NewSecure1!");
        doNothing().when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

}
