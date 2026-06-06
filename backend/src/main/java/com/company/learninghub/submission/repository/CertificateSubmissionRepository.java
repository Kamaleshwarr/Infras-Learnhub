package com.company.learninghub.submission.repository;

import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.domain.CertificateSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CertificateSubmissionRepository extends JpaRepository<CertificateSubmission, UUID> {

    boolean existsByEmployeeIdAndInitiativeId(UUID employeeId, UUID initiativeId);

    @Override
    @EntityGraph(attributePaths = {
            "employee",
            "initiative",
            "certificateDocument",
            "reviewedBy"
    })
    Optional<CertificateSubmission> findById(UUID id);

    @EntityGraph(attributePaths = {
            "employee",
            "initiative",
            "certificateDocument",
            "reviewedBy"
    })
    @Query("""
            SELECT submission
            FROM CertificateSubmission submission
            WHERE (:status IS NULL OR submission.approvalStatus = :status)
              AND (:initiativeId IS NULL OR submission.initiative.id = :initiativeId)
              AND (:employeeId IS NULL OR submission.employee.id = :employeeId)
            """)
    Page<CertificateSubmission> findForAdmin(
            @Param("status") ApprovalStatus status,
            @Param("initiativeId") UUID initiativeId,
            @Param("employeeId") UUID employeeId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "employee",
            "initiative",
            "certificateDocument",
            "reviewedBy"
    })
    @Query("""
            SELECT submission
            FROM CertificateSubmission submission
            WHERE submission.employee.id = :employeeId
              AND (:status IS NULL OR submission.approvalStatus = :status)
              AND (:initiativeId IS NULL OR submission.initiative.id = :initiativeId)
            """)
    Page<CertificateSubmission> findForEmployee(
            @Param("employeeId") UUID employeeId,
            @Param("status") ApprovalStatus status,
            @Param("initiativeId") UUID initiativeId,
            Pageable pageable
    );
}

