package com.company.learninghub.submission.mapper;

import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.submission.domain.CertificateDocument;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.submission.dto.CertificateDocumentResponse;
import com.company.learninghub.submission.dto.CertificateSubmissionResponse;
import com.company.learninghub.submission.dto.SubmissionEmployeeResponse;
import com.company.learninghub.submission.dto.SubmissionInitiativeResponse;
import com.company.learninghub.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class CertificateSubmissionMapper {

    public CertificateSubmissionResponse toResponse(CertificateSubmission submission) {
        CertificateDocument document = submission.getCertificateDocument();
        return new CertificateSubmissionResponse(
                submission.getId(),
                toEmployeeResponse(submission.getEmployee()),
                toInitiativeResponse(submission.getInitiative()),
                document.getId(),
                toDocumentResponse(document),
                submission.getComments(),
                submission.getSubmittedAtUtc(),
                submission.getApprovalStatus(),
                toEmployeeResponse(submission.getReviewedBy()),
                submission.getReviewedAtUtc(),
                submission.getRejectionReason(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }

    private SubmissionEmployeeResponse toEmployeeResponse(User user) {
        if (user == null) {
            return null;
        }
        return new SubmissionEmployeeResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    private SubmissionInitiativeResponse toInitiativeResponse(LearningInitiative initiative) {
        return new SubmissionInitiativeResponse(
                initiative.getId(),
                initiative.getTitle(),
                initiative.getStatus()
        );
    }

    private CertificateDocumentResponse toDocumentResponse(CertificateDocument document) {
        return new CertificateDocumentResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSizeBytes()
        );
    }
}

