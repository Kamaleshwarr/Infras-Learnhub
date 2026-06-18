package com.company.learninghub.submission.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.notification.service.NotificationService;
import com.company.learninghub.storage.CertificateFileStorageService;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.storage.StoredFile;
import com.company.learninghub.submission.domain.ApprovalStatus;
import com.company.learninghub.submission.domain.CertificateDocument;
import com.company.learninghub.submission.domain.CertificateSubmission;
import com.company.learninghub.submission.dto.CertificateContent;
import com.company.learninghub.submission.dto.CertificateSubmissionResponse;
import com.company.learninghub.submission.mapper.CertificateSubmissionMapper;
import com.company.learninghub.submission.repository.CertificateDocumentRepository;
import com.company.learninghub.submission.repository.CertificateSubmissionRepository;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CertificateSubmissionService {

    private static final int MAX_COMMENTS_LENGTH = 2000;
    private static final Set<String> ALLOWED_CERTIFICATE_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final CertificateSubmissionRepository submissionRepository;
    private final CertificateDocumentRepository documentRepository;
    private final LearningInitiativeRepository initiativeRepository;
    private final UserRepository userRepository;
    private final CertificateFileStorageService fileStorageService;
    private final StorageProperties storageProperties;
    private final CertificateSubmissionMapper submissionMapper;
    private final NotificationService notificationService;
    private final Clock clock;

    @Autowired
    public CertificateSubmissionService(
            CertificateSubmissionRepository submissionRepository,
            CertificateDocumentRepository documentRepository,
            LearningInitiativeRepository initiativeRepository,
            UserRepository userRepository,
            CertificateFileStorageService fileStorageService,
            StorageProperties storageProperties,
            CertificateSubmissionMapper submissionMapper,
            NotificationService notificationService
    ) {
        this(
                submissionRepository,
                documentRepository,
                initiativeRepository,
                userRepository,
                fileStorageService,
                storageProperties,
                submissionMapper,
                notificationService,
                Clock.systemUTC()
        );
    }

    CertificateSubmissionService(
            CertificateSubmissionRepository submissionRepository,
            CertificateDocumentRepository documentRepository,
            LearningInitiativeRepository initiativeRepository,
            UserRepository userRepository,
            CertificateFileStorageService fileStorageService,
            StorageProperties storageProperties,
            CertificateSubmissionMapper submissionMapper,
            NotificationService notificationService,
            Clock clock
    ) {
        this.submissionRepository = submissionRepository;
        this.documentRepository = documentRepository;
        this.initiativeRepository = initiativeRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.storageProperties = storageProperties;
        this.submissionMapper = submissionMapper;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @Transactional
    public CertificateSubmissionResponse submit(
            UUID initiativeId,
            MultipartFile certificateFile,
            String comments,
            AuthenticatedUser authenticatedUser
    ) {
        validateFile(certificateFile);
        String normalizedComments = normalizeComments(comments);
        User employee = findUserOrThrow(authenticatedUser.getId());
        LearningInitiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning initiative was not found"));

        if (!initiative.isVisibleToEmployeesAt(clock.instant())) {
            throw new ResourceNotFoundException("Learning initiative was not found");
        }
        if (submissionRepository.existsByEmployeeIdAndInitiativeId(employee.getId(), initiativeId)) {
            throw new IllegalArgumentException("A certificate submission already exists for this initiative");
        }

        StoredFile storedFile = fileStorageService.store(certificateFile);
        try {
            CertificateDocument document = documentRepository.save(new CertificateDocument(
                    storedFile.storageProvider(),
                    storedFile.storageKey(),
                    storedFile.originalFilename(),
                    storedFile.contentType(),
                    storedFile.fileSizeBytes(),
                    employee
            ));
            CertificateSubmission submission = new CertificateSubmission(
                    employee,
                    initiative,
                    document,
                    normalizedComments,
                    Instant.now(clock)
            );
            CertificateSubmission savedSubmission = submissionRepository.save(submission);
            notificationService.notifyCertificateSubmitted(savedSubmission);
            return submissionMapper.toResponse(savedSubmission);
        } catch (RuntimeException ex) {
            if (storedFile != null) {
                fileStorageService.deleteQuietly(storedFile.storageKey());
            }
            throw ex;
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public CertificateSubmissionResponse getById(UUID submissionId, AuthenticatedUser authenticatedUser) {
        CertificateSubmission submission = findAccessibleSubmissionOrThrow(submissionId, authenticatedUser);
        return submissionMapper.toResponse(submission);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public CertificateContent getCertificateContent(UUID submissionId, AuthenticatedUser authenticatedUser) {
        CertificateSubmission submission = findAccessibleSubmissionOrThrow(submissionId, authenticatedUser);
        CertificateDocument document = submission.getCertificateDocument();
        try {
            Resource resource = fileStorageService.loadAsResource(document.getStorageKey());
            return new CertificateContent(
                    resource,
                    document.getContentType(),
                    document.getOriginalFilename()
            );
        } catch (IllegalStateException ex) {
            throw new ResourceNotFoundException("Certificate file was not found");
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<CertificateSubmissionResponse> listOwn(
            ApprovalStatus status,
            UUID initiativeId,
            Pageable pageable,
            AuthenticatedUser authenticatedUser
    ) {
        return submissionRepository.findForEmployee(
                        authenticatedUser.getId(),
                        status,
                        initiativeId,
                        normalizePageable(pageable)
                )
                .map(submissionMapper::toResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<CertificateSubmissionResponse> listAll(
            ApprovalStatus status,
            UUID initiativeId,
            UUID employeeId,
            Pageable pageable
    ) {
        return submissionRepository.findForAdmin(status, initiativeId, employeeId, normalizePageable(pageable))
                .map(submissionMapper::toResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CertificateSubmissionResponse approve(UUID submissionId, AuthenticatedUser authenticatedUser) {
        CertificateSubmission submission = findSubmissionOrThrow(submissionId);
        ensureSubmitted(submission);
        submission.approve(findUserOrThrow(authenticatedUser.getId()), Instant.now(clock));
        notificationService.notifyCertificateApproved(submission);
        return submissionMapper.toResponse(submission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CertificateSubmissionResponse reject(
            UUID submissionId,
            String rejectionReason,
            AuthenticatedUser authenticatedUser
    ) {
        String normalizedReason = normalizeRejectionReason(rejectionReason);
        CertificateSubmission submission = findSubmissionOrThrow(submissionId);
        ensureSubmitted(submission);
        submission.reject(findUserOrThrow(authenticatedUser.getId()), Instant.now(clock), normalizedReason);
        notificationService.notifyCertificateRejected(submission);
        return submissionMapper.toResponse(submission);
    }

    private CertificateSubmission findSubmissionOrThrow(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate submission was not found"));
    }

    private CertificateSubmission findAccessibleSubmissionOrThrow(UUID submissionId, AuthenticatedUser authenticatedUser) {
        CertificateSubmission submission = findSubmissionOrThrow(submissionId);
        if (isAdmin(authenticatedUser) || submission.belongsTo(authenticatedUser.getId())) {
            return submission;
        }
        throw new ResourceNotFoundException("Certificate submission was not found");
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private boolean isAdmin(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.getRoleNames().contains(RoleName.ADMIN);
    }

    private void ensureSubmitted(CertificateSubmission submission) {
        if (!ApprovalStatus.SUBMITTED.equals(submission.getApprovalStatus())) {
            throw new IllegalArgumentException("Only submitted certificate submissions can be reviewed");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Certificate file is required");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("Certificate file exceeds the configured maximum size");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CERTIFICATE_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Certificate file must be a PDF, JPEG, or PNG");
        }
    }

    private String normalizeComments(String comments) {
        if (!StringUtils.hasText(comments)) {
            return null;
        }
        String normalized = comments.trim();
        if (normalized.length() > MAX_COMMENTS_LENGTH) {
            throw new IllegalArgumentException("Comments must be 2000 characters or fewer");
        }
        return normalized;
    }

    private String normalizeRejectionReason(String rejectionReason) {
        if (!StringUtils.hasText(rejectionReason)) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        String normalized = rejectionReason.trim();
        if (normalized.length() > MAX_COMMENTS_LENGTH) {
            throw new IllegalArgumentException("Rejection reason must be 2000 characters or fewer");
        }
        return normalized;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(this::toRepositorySortOrder)
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Sort.Order toRepositorySortOrder(Sort.Order order) {
        String property = switch (order.getProperty()) {
            case "id", "approvalStatus", "submittedAtUtc", "reviewedAtUtc", "createdAt", "updatedAt" ->
                    order.getProperty();
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            case "employeeId" -> "employee.employeeId";
            case "initiativeId" -> "initiative.id";
            case "certificateDocumentId" -> "certificateDocument.id";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        };

        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}

