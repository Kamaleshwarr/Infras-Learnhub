package com.company.learninghub.assistant.tool;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.dto.CertificateSubmissionResponse;
import com.company.learninghub.submission.dto.SubmissionEmployeeResponse;
import com.company.learninghub.submission.dto.SubmissionInitiativeResponse;
import com.company.learninghub.submission.service.CertificateSubmissionService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyCertificationsToolTest {

    @Mock
    private CertificateSubmissionService certificateSubmissionService;

    private MyCertificationsTool tool;

    @BeforeEach
    void setUp() {
        tool = new MyCertificationsTool(certificateSubmissionService);
    }

    @Test
    void usesListOwnForEmployee() {
        AuthenticatedUser employee = employeePrincipal();
        CertificateSubmissionResponse submission = sampleSubmission();
        Page<CertificateSubmissionResponse> page = new PageImpl<>(List.of(submission), PageRequest.of(0, 20), 1);
        when(certificateSubmissionService.listOwn(isNull(), isNull(), any(), eq(employee))).thenReturn(page);

        ToolResult result = tool.execute(new AssistantToolContext(employee, "my certifications"));

        assertThat(result.text()).contains("1 certification submission");
        assertThat(result.structuredData()).isInstanceOf(PageResponse.class);
        verify(certificateSubmissionService).listOwn(isNull(), isNull(), any(), eq(employee));
    }

    @Test
    void usesListAllForAdmin() {
        AuthenticatedUser admin = adminPrincipal();
        Page<CertificateSubmissionResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(certificateSubmissionService.listAll(isNull(), isNull(), eq(admin.getId()), any())).thenReturn(page);

        ToolResult result = tool.execute(new AssistantToolContext(admin, "my certifications"));

        assertThat(result.text()).contains("do not have any certification submissions");
        verify(certificateSubmissionService).listAll(isNull(), isNull(), eq(admin.getId()), any());
    }

    private AuthenticatedUser employeePrincipal() {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return AuthenticatedUser.from(user);
    }

    private AuthenticatedUser adminPrincipal() {
        User user = new User("ADMIN001", "admin@learninghub.local", "Admin", "hash");
        user.assignRole(new Role(RoleName.ADMIN));
        return AuthenticatedUser.from(user);
    }

    private CertificateSubmissionResponse sampleSubmission() {
        UUID id = UUID.randomUUID();
        return new CertificateSubmissionResponse(
                id,
                new SubmissionEmployeeResponse(id, "EMP001", "Employee", "employee@learninghub.local"),
                new SubmissionInitiativeResponse(UUID.randomUUID(), "Initiative", com.company.learninghub.initiative.domain.InitiativeStatus.ACTIVE),
                UUID.randomUUID(),
                null,
                "comments",
                Instant.parse("2026-01-01T00:00:00Z"),
                ApprovalStatus.APPROVED,
                null,
                null,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
