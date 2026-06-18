package com.company.learninghub.submission.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.GlobalExceptionHandler;
import com.company.learninghub.submission.dto.CertificateContent;
import com.company.learninghub.submission.service.CertificateSubmissionService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CertificateSubmissionControllerTest {

    private CertificateSubmissionService submissionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        submissionService = mock(CertificateSubmissionService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CertificateSubmissionController(submissionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCertificateReturnsAttachmentResponse() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedUser(RoleName.ADMIN);
        authenticate(authenticatedUser);
        UUID submissionId = UUID.randomUUID();
        ByteArrayResource resource = new ByteArrayResource("certificate-content".getBytes());
        when(submissionService.getCertificateContent(eq(submissionId), eq(authenticatedUser)))
                .thenReturn(new CertificateContent(resource, "application/pdf", "certificate.pdf"));

        mockMvc.perform(get("/api/v1/submissions/{submissionId}/certificate", submissionId))
                .andExpect(status().isOk())
                .andExpect(content().bytes("certificate-content".getBytes()))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-cache, no-store, must-revalidate"));

        verify(submissionService).getCertificateContent(submissionId, authenticatedUser);
    }

    @Test
    void getCertificateReturnsInlineResponse() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedUser(RoleName.ADMIN);
        authenticate(authenticatedUser);
        UUID submissionId = UUID.randomUUID();
        ByteArrayResource resource = new ByteArrayResource("certificate-content".getBytes());
        when(submissionService.getCertificateContent(eq(submissionId), eq(authenticatedUser)))
                .thenReturn(new CertificateContent(resource, "image/png", "certificate.png"));

        mockMvc.perform(get("/api/v1/submissions/{submissionId}/certificate", submissionId)
                        .param("disposition", "inline"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"));
    }

    @Test
    void getCertificateRejectsInvalidDisposition() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedUser(RoleName.ADMIN);
        authenticate(authenticatedUser);
        UUID submissionId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/submissions/{submissionId}/certificate", submissionId)
                        .param("disposition", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("disposition must be inline or attachment"));
    }

    private void authenticate(AuthenticatedUser authenticatedUser) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        authenticatedUser,
                        null,
                        authenticatedUser.getAuthorities()
                )
        );
    }

    private AuthenticatedUser authenticatedUser(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return AuthenticatedUser.from(user);
    }
}
