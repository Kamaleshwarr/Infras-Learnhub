package com.company.learninghub.initiative.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.BusinessConflictException;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.dto.ReactivateInitiativeRequest;
import com.company.learninghub.initiative.dto.UpdateInitiativeRequest;
import com.company.learninghub.initiative.mapper.LearningInitiativeMapper;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class LearningInitiativeServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-06T06:00:00Z");

    @Mock
    private LearningInitiativeRepository initiativeRepository;

    @Mock
    private CertificateSubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    private LearningInitiativeService initiativeService;

    private User adminUser;
    private User employeeUser;
    private AuthenticatedUser adminPrincipal;
    private AuthenticatedUser employeePrincipal;

    @BeforeEach
    void setUp() {
        initiativeService = new LearningInitiativeService(
                initiativeRepository,
                submissionRepository,
                userRepository,
                new LearningInitiativeMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        adminUser = user("ADMIN001", "admin@learninghub.local", "Learning Hub Admin", RoleName.ADMIN);
        employeeUser = user("EMP001", "employee@learninghub.local", "Learning Hub Employee", RoleName.EMPLOYEE);
        adminPrincipal = AuthenticatedUser.from(adminUser);
        employeePrincipal = AuthenticatedUser.from(employeeUser);
    }

    @Test
    void createPersistsInitiativeWithAuthenticatedAdminAsCreator() {
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "AWS AI Certification",
                "Complete the AWS AI certification.",
                "Recognition",
                NOW.plusSeconds(3600),
                NOW.plusSeconds(7200)
        );
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));
        when(initiativeRepository.save(any(LearningInitiative.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitiativeResponse response = initiativeService.create(request, adminPrincipal);

        assertThat(response.title()).isEqualTo("AWS AI Certification");
        assertThat(response.status()).isEqualTo(InitiativeStatus.DRAFT);
        assertThat(response.createdBy().email()).isEqualTo("admin@learninghub.local");

        ArgumentCaptor<LearningInitiative> captor = ArgumentCaptor.forClass(LearningInitiative.class);
        verify(initiativeRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(adminUser);
    }

    @Test
    void createAllowsOneDayInitiativeWhenExpiryMatchesStartDate() {
        Instant sameDay = Instant.parse("2026-06-19T00:00:00.000Z");
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "One-day Workshop",
                "Single-day learning event.",
                null,
                sameDay,
                sameDay
        );
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));
        when(initiativeRepository.save(any(LearningInitiative.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitiativeResponse response = initiativeService.create(request, adminPrincipal);

        assertThat(response.startDateUtc()).isEqualTo(sameDay);
        assertThat(response.expiryDateUtc()).isEqualTo(sameDay);
    }

    @Test
    void updateChangesExistingInitiativeFields() {
        LearningInitiative initiative = initiative("Initial", InitiativeStatus.DRAFT, adminUser);
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "Updated",
                "Updated description",
                "Updated reward",
                NOW.plusSeconds(1800),
                NOW.plusSeconds(3600)
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.update(initiativeId, request);

        assertThat(response.title()).isEqualTo("Updated");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.rewardDescription()).isEqualTo("Updated reward");
        assertThat(response.status()).isEqualTo(InitiativeStatus.DRAFT);
    }

    @Test
    void publishTransitionsDraftToActive() {
        LearningInitiative initiative = initiative("Draft", InitiativeStatus.DRAFT, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.publish(initiativeId);

        assertThat(response.status()).isEqualTo(InitiativeStatus.ACTIVE);
    }

    @Test
    void publishRejectsInvalidMetadata() {
        LearningInitiative initiative = new LearningInitiative(
                "",
                "Description",
                "Reward",
                NOW.plusSeconds(3600),
                NOW.plusSeconds(7200),
                InitiativeStatus.DRAFT,
                adminUser
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.publish(initiativeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title is required");
    }

    @Test
    void publishRejectsInvalidTransition() {
        LearningInitiative initiative = initiative("Active", InitiativeStatus.ACTIVE, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.publish(initiativeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transition initiative from ACTIVE to ACTIVE");
    }

    @Test
    void returnToDraftTransitionsActiveToDraft() {
        LearningInitiative initiative = initiative("Active", InitiativeStatus.ACTIVE, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.returnToDraft(initiativeId);

        assertThat(response.status()).isEqualTo(InitiativeStatus.DRAFT);
    }

    @Test
    void returnToDraftRejectsInvalidTransition() {
        LearningInitiative initiative = initiative("Draft", InitiativeStatus.DRAFT, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.returnToDraft(initiativeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transition initiative from DRAFT to DRAFT");
    }

    @Test
    void markExpiredSetsExpiryToToday() {
        LearningInitiative initiative = initiative("Active", InitiativeStatus.ACTIVE, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.markExpired(initiativeId);

        assertThat(response.status()).isEqualTo(InitiativeStatus.EXPIRED);
        assertThat(response.expiryDateUtc()).isEqualTo(Instant.parse("2026-06-06T00:00:00.000Z"));
    }

    @Test
    void markExpiredRejectsInvalidTransition() {
        LearningInitiative initiative = initiative("Draft", InitiativeStatus.DRAFT, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.markExpired(initiativeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transition initiative from DRAFT to EXPIRED");
    }

    @Test
    void reactivateTransitionsExpiredToActiveWithNewExpiry() {
        LearningInitiative initiative = initiative(
                "Expired",
                InitiativeStatus.EXPIRED,
                NOW.minusSeconds(86_400),
                Instant.parse("2026-06-05T00:00:00.000Z"),
                adminUser
        );
        UUID initiativeId = UUID.randomUUID();
        Instant newExpiry = Instant.parse("2026-12-31T00:00:00.000Z");
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.reactivate(
                initiativeId,
                new ReactivateInitiativeRequest(newExpiry)
        );

        assertThat(response.status()).isEqualTo(InitiativeStatus.ACTIVE);
        assertThat(response.expiryDateUtc()).isEqualTo(newExpiry);
    }

    @Test
    void reactivateRejectsExpiryBeforeToday() {
        LearningInitiative initiative = initiative(
                "Expired",
                InitiativeStatus.EXPIRED,
                Instant.parse("2026-06-01T00:00:00.000Z"),
                Instant.parse("2026-06-05T00:00:00.000Z"),
                adminUser
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.reactivate(
                initiativeId,
                new ReactivateInitiativeRequest(Instant.parse("2026-06-05T00:00:00.000Z"))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("expiryDateUtc cannot be earlier than today (UTC)");
    }

    @Test
    void reactivateRejectsInvalidTransition() {
        LearningInitiative initiative = initiative("Active", InitiativeStatus.ACTIVE, adminUser);
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.reactivate(
                initiativeId,
                new ReactivateInitiativeRequest(Instant.parse("2026-12-31T00:00:00.000Z"))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transition initiative from ACTIVE to ACTIVE");
    }

    @Test
    void createRejectsStartDateBeforeToday() {
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "AWS AI Certification",
                "Complete the AWS AI certification.",
                "Recognition",
                NOW.minusSeconds(86_400),
                NOW.plusSeconds(7200)
        );
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> initiativeService.create(request, adminPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDateUtc cannot be earlier than today (UTC)");
    }

    @Test
    void updateRejectsModifiedStartDateBeforeToday() {
        Instant storedStart = Instant.parse("2026-06-06T05:00:00.000Z");
        LearningInitiative initiative = initiative(
                "Active",
                InitiativeStatus.ACTIVE,
                storedStart,
                NOW.plusSeconds(3600),
                adminUser
        );
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "Active",
                "Updated description",
                "Reward",
                NOW.minusSeconds(86_400),
                NOW.plusSeconds(3600)
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.update(initiativeId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDateUtc cannot be earlier than today (UTC)");
    }

    @Test
    void updateAllowsUnchangedPastStartDateWhenEditingOtherFields() {
        Instant pastStart = Instant.parse("2026-06-01T00:00:00.000Z");
        Instant expiry = Instant.parse("2026-12-31T00:00:00.000Z");
        LearningInitiative initiative = initiative(
                "Active",
                InitiativeStatus.ACTIVE,
                pastStart,
                expiry,
                adminUser
        );
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "Updated title",
                "Updated description",
                "Updated reward",
                pastStart,
                expiry
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.update(initiativeId, request);

        assertThat(response.title()).isEqualTo("Updated title");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.startDateUtc()).isEqualTo(pastStart);
    }

    @Test
    void updateAllowsModifiedStartDateOnOrAfterToday() {
        Instant pastStart = Instant.parse("2026-06-01T00:00:00.000Z");
        Instant futureStart = Instant.parse("2026-06-19T00:00:00.000Z");
        LearningInitiative initiative = initiative(
                "Active",
                InitiativeStatus.ACTIVE,
                pastStart,
                Instant.parse("2026-12-31T00:00:00.000Z"),
                adminUser
        );
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "Active",
                "Updated description",
                "Reward",
                futureStart,
                Instant.parse("2026-12-31T00:00:00.000Z")
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.update(initiativeId, request);

        assertThat(response.startDateUtc()).isEqualTo(futureStart);
    }

    @Test
    void listUsesAdminQueryForAdminPrincipal() {
        PageRequest pageable = PageRequest.of(0, 20);
        LearningInitiative initiative = initiative("OpenAI Certification", InitiativeStatus.ACTIVE, adminUser);
        when(initiativeRepository.findAll(anySpecification(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(initiative), pageable, 1));

        Page<InitiativeResponse> response = initiativeService.list(
                InitiativeStatus.ACTIVE,
                "  OpenAI  ",
                pageable,
                adminPrincipal
        );

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().title()).isEqualTo("OpenAI Certification");
        verify(initiativeRepository).findAll(anySpecification(), eq(pageable));
    }

    @Test
    void listTranslatesPublicUtcSortFieldsToEntitySortFields() {
        PageRequest pageable = PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("createdAtUtc"), Sort.Order.asc("updatedAtUtc"))
        );
        LearningInitiative initiative = initiative("Sorted", InitiativeStatus.ACTIVE, adminUser);
        when(initiativeRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(initiative), invocation.getArgument(1), 11));

        initiativeService.list(null, "   ", pageable, adminPrincipal);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(initiativeRepository).findAll(anySpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("updatedAt")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("updatedAt").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void listRejectsUnsupportedSortProperty() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("unsupportedField"));

        assertThatThrownBy(() -> initiativeService.list(null, null, pageable, adminPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported sort property: unsupportedField");
    }

    @Test
    void listUsesActiveEmployeeQueryForEmployeePrincipal() {
        PageRequest pageable = PageRequest.of(0, 20);
        LearningInitiative initiative = initiative("Anthropic Learning", InitiativeStatus.ACTIVE, adminUser);
        when(initiativeRepository.findAll(anySpecification(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(initiative), pageable, 1));

        Page<InitiativeResponse> response = initiativeService.list(
                InitiativeStatus.EXPIRED,
                "Anthropic",
                pageable,
                employeePrincipal
        );

        assertThat(response.getContent()).hasSize(1);
        verify(initiativeRepository).findAll(anySpecification(), eq(pageable));
    }

    @Test
    void getByIdHidesInactiveInitiativeFromEmployee() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative("Draft", InitiativeStatus.DRAFT, adminUser);
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.getById(initiativeId, employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Learning initiative was not found");
    }

    @Test
    void getByIdShowsActiveInitiativeToEmployeeAtStartAndExpiryBoundaries() {
        UUID startBoundaryId = UUID.randomUUID();
        LearningInitiative startsNow = initiative(
                "Starts now",
                InitiativeStatus.ACTIVE,
                NOW,
                NOW.plusSeconds(3600),
                adminUser
        );
        when(initiativeRepository.findById(startBoundaryId)).thenReturn(Optional.of(startsNow));

        InitiativeResponse startBoundaryResponse = initiativeService.getById(startBoundaryId, employeePrincipal);

        assertThat(startBoundaryResponse.title()).isEqualTo("Starts now");

        UUID expiryBoundaryId = UUID.randomUUID();
        LearningInitiative expiresNow = initiative(
                "Expires now",
                InitiativeStatus.ACTIVE,
                NOW.minusSeconds(3600),
                NOW,
                adminUser
        );
        when(initiativeRepository.findById(expiryBoundaryId)).thenReturn(Optional.of(expiresNow));

        InitiativeResponse expiryBoundaryResponse = initiativeService.getById(expiryBoundaryId, employeePrincipal);

        assertThat(expiryBoundaryResponse.title()).isEqualTo("Expires now");
    }

    @Test
    void getByIdHidesExpiredActiveInitiativeFromEmployee() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative(
                "Expired active",
                InitiativeStatus.ACTIVE,
                NOW.minusSeconds(72_000),
                NOW.minusSeconds(86_400),
                adminUser
        );
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.getById(initiativeId, employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Learning initiative was not found");
    }

    @Test
    void getByIdHidesFutureActiveInitiativeFromEmployee() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative(
                "Future active",
                InitiativeStatus.ACTIVE,
                Instant.parse("2026-06-07T00:00:00.000Z"),
                NOW.plusSeconds(7200),
                adminUser
        );
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        assertThatThrownBy(() -> initiativeService.getById(initiativeId, employeePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Learning initiative was not found");
    }

    @Test
    void deleteRemovesExistingInitiativeWhenNoSubmissionsExist() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative("To delete", InitiativeStatus.DRAFT, adminUser);
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));
        when(submissionRepository.countByInitiativeId(initiativeId)).thenReturn(0L);

        initiativeService.delete(initiativeId, adminPrincipal);

        verify(initiativeRepository).delete(initiative);
    }

    @Test
    void deleteRejectsInitiativeWithSubmissions() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative("Blocked", InitiativeStatus.ACTIVE, adminUser);
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));
        when(submissionRepository.countByInitiativeId(initiativeId)).thenReturn(2L);

        assertThatThrownBy(() -> initiativeService.delete(initiativeId, adminPrincipal))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage(LearningInitiativeService.INITIATIVE_DELETE_BLOCKED_MESSAGE);

        verify(initiativeRepository, never()).delete(initiative);
    }

    @Test
    void countSubmissionsReturnsRepositoryCount() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative("Counted", InitiativeStatus.DRAFT, adminUser);
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));
        when(submissionRepository.countByInitiativeId(initiativeId)).thenReturn(3L);

        assertThat(initiativeService.countSubmissions(initiativeId)).isEqualTo(3L);
    }

    private User user(String employeeId, String email, String fullName, RoleName roleName) {
        User user = new User(employeeId, email, fullName, "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private LearningInitiative initiative(String title, InitiativeStatus status, User createdBy) {
        return initiative(title, status, NOW.minusSeconds(3600), NOW.plusSeconds(3600), createdBy);
    }

    private LearningInitiative initiative(
            String title,
            InitiativeStatus status,
            Instant startDateUtc,
            Instant expiryDateUtc,
            User createdBy
    ) {
        return new LearningInitiative(
                title,
                title + " description",
                "Reward",
                startDateUtc,
                expiryDateUtc,
                status,
                createdBy
        );
    }

    private Specification<LearningInitiative> anySpecification() {
        return any();
    }
}

