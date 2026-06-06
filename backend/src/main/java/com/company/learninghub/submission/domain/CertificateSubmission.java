package com.company.learninghub.submission.domain;

import com.company.learninghub.common.domain.AuditableEntity;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "certificate_submissions")
public class CertificateSubmission extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, updatable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiative_id", nullable = false, updatable = false)
    private LearningInitiative initiative;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certificate_document_id", nullable = false, updatable = false)
    private CertificateDocument certificateDocument;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "submitted_at_utc", nullable = false, updatable = false)
    private Instant submittedAtUtc;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at_utc")
    private Instant reviewedAtUtc;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    protected CertificateSubmission() {
    }

    public CertificateSubmission(
            User employee,
            LearningInitiative initiative,
            CertificateDocument certificateDocument,
            String comments,
            Instant submittedAtUtc
    ) {
        this.employee = employee;
        this.initiative = initiative;
        this.certificateDocument = certificateDocument;
        this.comments = comments;
        this.submittedAtUtc = submittedAtUtc;
        this.approvalStatus = ApprovalStatus.SUBMITTED;
    }

    public UUID getId() {
        return id;
    }

    public User getEmployee() {
        return employee;
    }

    public LearningInitiative getInitiative() {
        return initiative;
    }

    public CertificateDocument getCertificateDocument() {
        return certificateDocument;
    }

    public String getComments() {
        return comments;
    }

    public Instant getSubmittedAtUtc() {
        return submittedAtUtc;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAtUtc() {
        return reviewedAtUtc;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void approve(User reviewer, Instant reviewedAtUtc) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAtUtc = reviewedAtUtc;
        this.rejectionReason = null;
    }

    public void reject(User reviewer, Instant reviewedAtUtc, String rejectionReason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAtUtc = reviewedAtUtc;
        this.rejectionReason = rejectionReason;
    }

    public boolean belongsTo(UUID userId) {
        return employee != null && Objects.equals(employee.getId(), userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CertificateSubmission that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

