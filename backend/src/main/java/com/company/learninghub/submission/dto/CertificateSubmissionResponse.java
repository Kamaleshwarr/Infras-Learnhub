package com.company.learninghub.submission.dto;

import com.company.learninghub.submission.domain.ApprovalStatus;

import java.time.Instant;
import java.util.UUID;

public record CertificateSubmissionResponse(
        UUID id,
        SubmissionEmployeeResponse employee,
        SubmissionInitiativeResponse initiative,
        UUID certificateDocumentId,
        CertificateDocumentResponse certificateDocument,
        String comments,
        Instant submittedAtUtc,
        ApprovalStatus approvalStatus,
        SubmissionEmployeeResponse reviewedBy,
        Instant reviewedAtUtc,
        String rejectionReason,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
}

