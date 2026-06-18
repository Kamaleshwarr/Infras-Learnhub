package com.company.learninghub.submission.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.dto.CertificateContent;
import com.company.learninghub.submission.dto.CertificateContentDisposition;
import com.company.learninghub.submission.dto.CertificateSubmissionResponse;
import com.company.learninghub.submission.dto.RejectSubmissionRequest;
import com.company.learninghub.submission.service.CertificateSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Certificate Submissions", description = "Submit and review learning initiative certificates")
@SecurityRequirement(name = "bearerAuth")
public class CertificateSubmissionController {

    private final CertificateSubmissionService submissionService;

    public CertificateSubmissionController(CertificateSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping(
            value = "/initiatives/{initiativeId}/submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Submit a certificate for an initiative",
            description = "Employee only. The initiative must be active and within its UTC start/expiry window."
    )
    public ResponseEntity<CertificateSubmissionResponse> submit(
            @PathVariable UUID initiativeId,
            @RequestPart("certificateFile") MultipartFile certificateFile,
            @RequestParam(required = false) @Size(max = 2000) String comments,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        CertificateSubmissionResponse response = submissionService.submit(
                initiativeId,
                certificateFile,
                comments,
                authenticatedUser
        );
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/submissions/{submissionId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/me/submissions")
    @Operation(summary = "List current employee certificate submissions", description = "Employee only.")
    public ResponseEntity<PageResponse<CertificateSubmissionResponse>> listOwn(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) UUID initiativeId,
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields include submittedAtUtc, approvalStatus, reviewedAtUtc, createdAtUtc, updatedAtUtc, initiativeId, and certificateDocumentId."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "submittedAtUtc", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(PageResponse.from(
                submissionService.listOwn(status, initiativeId, pageable, authenticatedUser)
        ));
    }

    @GetMapping("/submissions")
    @Operation(summary = "List all certificate submissions", description = "Admin only.")
    public ResponseEntity<PageResponse<CertificateSubmissionResponse>> listAll(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) UUID initiativeId,
            @RequestParam(required = false) UUID employeeId,
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields include submittedAtUtc, approvalStatus, reviewedAtUtc, createdAtUtc, updatedAtUtc, employeeId, initiativeId, and certificateDocumentId."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "submittedAtUtc", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
                submissionService.listAll(status, initiativeId, employeeId, pageable)
        ));
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(
            summary = "View a certificate submission",
            description = "Admins can view all submissions. Employees can view only their own submissions."
    )
    public ResponseEntity<CertificateSubmissionResponse> getById(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(submissionService.getById(submissionId, authenticatedUser));
    }

    @GetMapping("/submissions/{submissionId}/certificate")
    @Operation(
            summary = "Download or preview a certificate file",
            description = """
                    Streams the certificate document for a submission.
                    Admins may access any submission. Employees may access only their own.
                    Use disposition=inline for browser preview and disposition=attachment for download.
                    """
    )
    public ResponseEntity<Resource> getCertificate(
            @PathVariable UUID submissionId,
            @RequestParam(defaultValue = "attachment") String disposition,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        CertificateContentDisposition contentDisposition = CertificateContentDisposition.fromQueryValue(disposition);
        CertificateContent certificateContent = submissionService.getCertificateContent(submissionId, authenticatedUser);
        MediaType mediaType = MediaType.parseMediaType(
                certificateContent.contentType() == null
                        ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                        : certificateContent.contentType()
        );
        String filename = sanitizeContentDispositionFilename(certificateContent.originalFilename());
        String contentDispositionHeader = contentDisposition == CertificateContentDisposition.INLINE
                ? "inline"
                : "attachment; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, no-cache, no-store, must-revalidate")
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeader)
                .body(certificateContent.resource());
    }

    @PostMapping("/submissions/{submissionId}/approve")
    @Operation(summary = "Approve a certificate submission", description = "Admin only.")
    public ResponseEntity<CertificateSubmissionResponse> approve(
            @PathVariable UUID submissionId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(submissionService.approve(submissionId, authenticatedUser));
    }

    @PostMapping("/submissions/{submissionId}/reject")
    @Operation(summary = "Reject a certificate submission", description = "Admin only. Rejection reason is required.")
    public ResponseEntity<CertificateSubmissionResponse> reject(
            @PathVariable UUID submissionId,
            @Valid @RequestBody RejectSubmissionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(submissionService.reject(
                submissionId,
                request.rejectionReason(),
                authenticatedUser
        ));
    }

    private String sanitizeContentDispositionFilename(String filename) {
        String sanitized = filename == null || filename.isBlank() ? "certificate" : filename.trim();
        return sanitized.replace("\"", "");
    }
}

