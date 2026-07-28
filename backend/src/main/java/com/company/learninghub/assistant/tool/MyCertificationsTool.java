package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.submission.dto.CertificateSubmissionResponse;
import com.company.learninghub.submission.service.CertificateSubmissionService;
import com.company.learninghub.user.domain.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class MyCertificationsTool implements AssistantTool {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CertificateSubmissionService certificateSubmissionService;

    public MyCertificationsTool(CertificateSubmissionService certificateSubmissionService) {
        this.certificateSubmissionService = certificateSubmissionService;
    }

    @Override
    public String getName() {
        return AssistantToolNames.MY_CERTIFICATIONS;
    }

    @Override
    public boolean supports(ResolvedIntent intent) {
        return intent.type() == AssistantIntentType.TOOL
                && AssistantToolNames.MY_CERTIFICATIONS.equals(intent.toolName());
    }

    @Override
    public ToolResult execute(AssistantToolContext context) {
        Page<CertificateSubmissionResponse> submissions = listCertifications(context);
        String text = submissions.getTotalElements() == 0
                ? "You do not have any certification submissions yet."
                : "Found %d certification submission(s).".formatted(submissions.getTotalElements());
        return ToolResult.structured(text, PageResponse.from(submissions));
    }

    private Page<CertificateSubmissionResponse> listCertifications(AssistantToolContext context) {
        PageRequest pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        if (context.authenticatedUser().getRoleNames().contains(RoleName.EMPLOYEE)) {
            return certificateSubmissionService.listOwn(null, null, pageable, context.authenticatedUser());
        }
        return certificateSubmissionService.listAll(
                null,
                null,
                context.authenticatedUser().getId(),
                pageable
        );
    }
}
