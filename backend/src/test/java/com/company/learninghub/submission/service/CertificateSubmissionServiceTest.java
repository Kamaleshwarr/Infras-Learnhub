package com.company.learninghub.submission.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
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
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateSubmissionServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-06T07:00:00Z");

    @Mock
    private CertificateSubmissionRepository submissionRepository;

    @Mock
    private CertificateDocumentRepository documentRepository;

    @Mock
    private LearningInitiativeRepository initiativeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CertificateFileStorageService fileStorageService;

    @Mock
    private NotificationService notificationService;

    private StorageProperties storageProperties;
    private CertificateSubmissionService submissionService;
    private User employee;
    private User anotherEmployee;
    private User admin;
    private LearningInitiative initiative;
    private AuthenticatedUser employeePrincipal;
    private AuthenticatedUser adminPrincipal;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.setMaxFileSizeBytes(1024);
        submissionService = new CertificateSubmissionService(
                submissionRepository,
                documentRepository,
                initiativeRepository,
                userRepository,
                fileStorageService,
                storageProperties,
                new CertificateSubmissionMapper(),
                notificationService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        employee = user("EMP001", "employee@learninghub.local", RoleName.EMPLOYEE);
        anotherEmployee = user("EMP002", "other@learninghub.local", RoleName.EMPLOYEE);
        admin = user("ADMIN001", "admin@learninghub.local", RoleName.ADMIN);
        initiative = initiative("AWS AI", InitiativeStatus.ACTIVE, NOW.minusSeconds(3600), NOW.plusSeconds(3600));
        employeePrincipal = AuthenticatedUser.from(employee);
        adminPrincipal = AuthenticatedUser.from(admin);
    }

    @Test
    void submitStoresCertificateAndCreatesSubmittedRecordForActiveInitiative() {
        UUID initiativeId = initiative.getId();
        MockMultipartFile file = certificateFile();
        StoredFile storedFile = new StoredFile("LOCAL", "certificates/file.pdf", "certificate.pdf", "application/pdf", file.getSize());
        CertificateDocument document = document(storedFile, employee);

        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));
        when(submissionRepository.existsByEmployeeIdAndInitiativeId(employee.getId(), initiativeId)).thenReturn(false);
        when(fileStorageService.store(file)).thenReturn(storedFile);
        when(documentRepository.save(any(CertificateDocument.class))).thenReturn(document);
        when(submissionRepository.save(any(CertificateSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CertificateSubmissionResponse response = submissionService.submit(initiativeId, file, "  passed exam  ", employeePrincipal);

        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.SUBMITTED);
        assertThat(response.submittedAtUtc()).isEqualTo(NOW);
        assertThat(response.comments()).isEqualTo("passed exam");
        assertThat(response.employee().id()).isEqualTo(employee.getId());
        verify(notificationService).notifyCertificateSubmitted(any(CertificateSubmission.class));
        assertThat(response.initiative().id()).isEqualTo(initiativeId);
        assertThat(response.certificateDocument().originalFilename()).isEqualTo("certificate.pdf");

        ArgumentCaptor<CertificateSubmission> submissionCaptor = ArgumentCaptor.forClass(CertificateSubmission.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        assertThat(submissionCaptor.getValue().getEmployee()).isEqualTo(employee);
        assertThat(submissionCaptor.getValue().getInitiative()).isEqualTo(initiative);
        verify(fileStorageService).store(file);
    }

    @Test
    void submitRejectsFutureInitiativeWithoutStoringFile() {
        LearningInitiative future = initiative("Future", InitiativeStatus.ACTIVE, NOW.plus(1, ChronoUnit.DAYS), NOW.plus(2, ChronoUnit.DAYS));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(future.getId())).thenReturn(Optional.of(future));

        assertThatThrownBy(() -> submissionService.submit(future.getId(), certificateFile(), null, employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Learning initiative was not found");

        verify(fileStorageService, never()).store(any());
    }

    @Test
    void submitRejectsExpiredInitiativeWithoutStoringFile() {
        LearningInitiative expired = initiative("Expired", InitiativeStatus.ACTIVE, NOW.minus(2, ChronoUnit.DAYS), NOW.minus(1, ChronoUnit.DAYS));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(expired.getId())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> submissionService.submit(expired.getId(), certificateFile(), null, employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Learning initiative was not found");

        verify(fileStorageService, never()).store(any());
    }

    @Test
    void submitRejectsDraftInitiativeWithoutStoringFile() {
        LearningInitiative draft = initiative("Draft", InitiativeStatus.DRAFT, NOW.minusSeconds(3600), NOW.plusSeconds(3600));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> submissionService.submit(draft.getId(), certificateFile(), null, employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Learning initiative was not found");

        verify(fileStorageService, never()).store(any());
    }

    @Test
    void submitRejectsDuplicateSubmission() {
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(initiative.getId())).thenReturn(Optional.of(initiative));
        when(submissionRepository.existsByEmployeeIdAndInitiativeId(employee.getId(), initiative.getId())).thenReturn(true);

        assertThatThrownBy(() -> submissionService.submit(initiative.getId(), certificateFile(), null, employeePrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A certificate submission already exists for this initiative");

        verify(fileStorageService, never()).store(any());
    }

    @Test
    void submitDeletesStoredFileWhenDocumentPersistenceFails() {
        MockMultipartFile file = certificateFile();
        StoredFile storedFile = new StoredFile("LOCAL", "certificates/orphan.pdf", "certificate.pdf", "application/pdf", file.getSize());
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(initiative.getId())).thenReturn(Optional.of(initiative));
        when(submissionRepository.existsByEmployeeIdAndInitiativeId(employee.getId(), initiative.getId())).thenReturn(false);
        when(fileStorageService.store(file)).thenReturn(storedFile);
        when(documentRepository.save(any(CertificateDocument.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(() -> submissionService.submit(initiative.getId(), file, null, employeePrincipal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database unavailable");

        verify(fileStorageService).deleteQuietly("certificates/orphan.pdf");
    }

    @Test
    void submitDeletesStoredFileWhenSubmissionPersistenceFails() {
        MockMultipartFile file = certificateFile();
        StoredFile storedFile = new StoredFile("LOCAL", "certificates/orphan.pdf", "certificate.pdf", "application/pdf", file.getSize());
        CertificateDocument document = document(storedFile, employee);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(initiativeRepository.findById(initiative.getId())).thenReturn(Optional.of(initiative));
        when(submissionRepository.existsByEmployeeIdAndInitiativeId(employee.getId(), initiative.getId())).thenReturn(false);
        when(fileStorageService.store(file)).thenReturn(storedFile);
        when(documentRepository.save(any(CertificateDocument.class))).thenReturn(document);
        when(submissionRepository.save(any(CertificateSubmission.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(() -> submissionService.submit(initiative.getId(), file, null, employeePrincipal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database unavailable");

        verify(fileStorageService).deleteQuietly("certificates/orphan.pdf");
    }

    @Test
    void submitRejectsInvalidFileTypeAndOversizedFile() {
        MockMultipartFile textFile = new MockMultipartFile("certificateFile", "note.txt", "text/plain", "hello".getBytes());
        assertThatThrownBy(() -> submissionService.submit(initiative.getId(), textFile, null, employeePrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Certificate file must be a PDF, JPEG, or PNG");

        MockMultipartFile largeFile = new MockMultipartFile(
                "certificateFile",
                "certificate.pdf",
                "application/pdf",
                new byte[2048]
        );
        assertThatThrownBy(() -> submissionService.submit(initiative.getId(), largeFile, null, employeePrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Certificate file exceeds the configured maximum size");
    }

    @Test
    void getByIdAllowsEmployeeToViewOnlyOwnSubmission() {
        CertificateSubmission ownSubmission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findById(ownSubmission.getId())).thenReturn(Optional.of(ownSubmission));

        CertificateSubmissionResponse response = submissionService.getById(ownSubmission.getId(), employeePrincipal);

        assertThat(response.employee().id()).isEqualTo(employee.getId());

        CertificateSubmission otherSubmission = submission(anotherEmployee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findById(otherSubmission.getId())).thenReturn(Optional.of(otherSubmission));

        assertThatThrownBy(() -> submissionService.getById(otherSubmission.getId(), employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Certificate submission was not found");
    }

    @Test
    void getCertificateContentAllowsAdminToAccessAnySubmission() {
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        ByteArrayResource resource = new ByteArrayResource("certificate-content".getBytes());
        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(fileStorageService.loadAsResource(submission.getCertificateDocument().getStorageKey())).thenReturn(resource);

        CertificateContent content = submissionService.getCertificateContent(submission.getId(), adminPrincipal);

        assertThat(content.contentType()).isEqualTo("application/pdf");
        assertThat(content.originalFilename()).isEqualTo("certificate.pdf");
        assertThat(content.resource()).isSameAs(resource);
    }

    @Test
    void getCertificateContentAllowsEmployeeToAccessOwnSubmission() {
        CertificateSubmission ownSubmission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        ByteArrayResource resource = new ByteArrayResource("certificate-content".getBytes());
        when(submissionRepository.findById(ownSubmission.getId())).thenReturn(Optional.of(ownSubmission));
        when(fileStorageService.loadAsResource(ownSubmission.getCertificateDocument().getStorageKey())).thenReturn(resource);

        CertificateContent content = submissionService.getCertificateContent(ownSubmission.getId(), employeePrincipal);

        assertThat(content.originalFilename()).isEqualTo("certificate.pdf");
    }

    @Test
    void getCertificateContentRejectsEmployeeAccessToOtherSubmission() {
        CertificateSubmission otherSubmission = submission(anotherEmployee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findById(otherSubmission.getId())).thenReturn(Optional.of(otherSubmission));

        assertThatThrownBy(() -> submissionService.getCertificateContent(otherSubmission.getId(), employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Certificate submission was not found");

        verify(fileStorageService, never()).loadAsResource(any());
    }

    @Test
    void getCertificateContentMapsMissingFileToNotFound() {
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(fileStorageService.loadAsResource(submission.getCertificateDocument().getStorageKey()))
                .thenThrow(new IllegalStateException("Certificate file is not readable"));

        assertThatThrownBy(() -> submissionService.getCertificateContent(submission.getId(), adminPrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Certificate file was not found");
    }

    @Test
    void listOwnDelegatesWithAuthenticatedEmployeeAndTranslatedSort() {
        PageRequest pageable = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("createdAtUtc")));
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findForEmployee(eq(employee.getId()), eq(ApprovalStatus.SUBMITTED), eq(initiative.getId()), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(submission), invocation.getArgument(3), 1));

        submissionService.listOwn(ApprovalStatus.SUBMITTED, initiative.getId(), pageable, employeePrincipal);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(submissionRepository).findForEmployee(eq(employee.getId()), eq(ApprovalStatus.SUBMITTED), eq(initiative.getId()), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void listAllDelegatesAdminFilters() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("submittedAtUtc"));
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findForAdmin(eq(ApprovalStatus.SUBMITTED), eq(initiative.getId()), eq(employee.getId()), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(submission), invocation.getArgument(3), 1));

        CertificateSubmissionResponse response = submissionService
                .listAll(ApprovalStatus.SUBMITTED, initiative.getId(), employee.getId(), pageable)
                .getContent()
                .getFirst();

        assertThat(response.employee().id()).isEqualTo(employee.getId());
    }

    @Test
    void listAllTranslatesAdminSortProperties() {
        PageRequest pageable = PageRequest.of(
                2,
                10,
                Sort.by(
                        Sort.Order.asc("employeeId"),
                        Sort.Order.desc("initiativeId"),
                        Sort.Order.asc("certificateDocumentId"),
                        Sort.Order.desc("updatedAtUtc")
                )
        );
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findForAdmin(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(submission), invocation.getArgument(3), 1));

        submissionService.listAll(null, null, null, pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(submissionRepository).findForAdmin(eq(null), eq(null), eq(null), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("employee.employeeId")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("initiative.id")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("certificateDocument.id")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("updatedAt")).isNotNull();
    }

    @Test
    void approveSubmittedSubmissionSetsReviewerAndReviewTimestamp() {
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        CertificateSubmissionResponse response = submissionService.approve(submission.getId(), adminPrincipal);

        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.reviewedAtUtc()).isEqualTo(NOW);
        assertThat(response.reviewedBy().id()).isEqualTo(admin.getId());
        assertThat(response.rejectionReason()).isNull();
        verify(notificationService).notifyCertificateApproved(submission);
    }

    @Test
    void rejectSubmittedSubmissionRequiresReasonAndSetsReviewMetadata() {
        CertificateSubmission submission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        UUID certificateDocumentId = submission.getCertificateDocument().getId();
        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        CertificateSubmissionResponse response = submissionService.reject(submission.getId(), "  Name mismatch  ", adminPrincipal);

        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(response.reviewedAtUtc()).isEqualTo(NOW);
        assertThat(response.reviewedBy().id()).isEqualTo(admin.getId());
        assertThat(response.rejectionReason()).isEqualTo("Name mismatch");
        assertThat(response.certificateDocumentId()).isEqualTo(certificateDocumentId);
        assertThat(response.certificateDocument().id()).isEqualTo(certificateDocumentId);
        verify(notificationService).notifyCertificateRejected(submission);

        CertificateSubmission anotherSubmission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        assertThatThrownBy(() -> submissionService.reject(anotherSubmission.getId(), " ", adminPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rejection reason is required");
    }

    @Test
    void reviewRejectsAlreadyReviewedSubmission() {
        CertificateSubmission approvedSubmission = submission(employee, initiative, ApprovalStatus.SUBMITTED);
        approvedSubmission.approve(admin, NOW.minusSeconds(60));
        when(submissionRepository.findById(approvedSubmission.getId())).thenReturn(Optional.of(approvedSubmission));

        assertThatThrownBy(() -> submissionService.approve(approvedSubmission.getId(), adminPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only submitted certificate submissions can be reviewed");
    }

    @Test
    void listRejectsUnsupportedSortProperty() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("badField"));

        assertThatThrownBy(() -> submissionService.listAll(null, null, null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported sort property: badField");
    }

    private MockMultipartFile certificateFile() {
        return new MockMultipartFile(
                "certificateFile",
                "certificate.pdf",
                "application/pdf",
                "certificate".getBytes()
        );
    }

    private User user(String employeeId, String email, RoleName roleName) {
        User user = new User(employeeId, email, roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private LearningInitiative initiative(
            String title,
            InitiativeStatus status,
            Instant startDateUtc,
            Instant expiryDateUtc
    ) {
        LearningInitiative initiative = new LearningInitiative(
                title,
                title + " description",
                "Reward",
                startDateUtc,
                expiryDateUtc,
                status,
                admin
        );
        ReflectionTestUtils.setField(initiative, "id", UUID.randomUUID());
        return initiative;
    }

    private CertificateDocument document(StoredFile storedFile, User uploadedBy) {
        CertificateDocument document = new CertificateDocument(
                storedFile.storageProvider(),
                storedFile.storageKey(),
                storedFile.originalFilename(),
                storedFile.contentType(),
                storedFile.fileSizeBytes(),
                uploadedBy
        );
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());
        return document;
    }

    private CertificateSubmission submission(User employee, LearningInitiative initiative, ApprovalStatus status) {
        CertificateDocument document = document(
                new StoredFile("LOCAL", "certificates/" + UUID.randomUUID() + ".pdf", "certificate.pdf", "application/pdf", 12),
                employee
        );
        CertificateSubmission submission = new CertificateSubmission(employee, initiative, document, "comments", NOW.minusSeconds(60));
        ReflectionTestUtils.setField(submission, "id", UUID.randomUUID());
        if (ApprovalStatus.APPROVED.equals(status)) {
            submission.approve(admin, NOW);
        } else if (ApprovalStatus.REJECTED.equals(status)) {
            submission.reject(admin, NOW, "Rejected");
        }
        return submission;
    }
}

