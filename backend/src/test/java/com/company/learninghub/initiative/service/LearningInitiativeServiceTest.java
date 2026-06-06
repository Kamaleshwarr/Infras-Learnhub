package com.company.learninghub.initiative.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.dto.CreateInitiativeRequest;
import com.company.learninghub.initiative.dto.InitiativeResponse;
import com.company.learninghub.initiative.dto.UpdateInitiativeRequest;
import com.company.learninghub.initiative.mapper.LearningInitiativeMapper;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningInitiativeServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-06T06:00:00Z");

    @Mock
    private LearningInitiativeRepository initiativeRepository;

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
                NOW.plusSeconds(7200),
                InitiativeStatus.DRAFT
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
    void updateChangesExistingInitiativeFields() {
        LearningInitiative initiative = initiative("Initial", InitiativeStatus.DRAFT, adminUser);
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "Updated",
                "Updated description",
                "Updated reward",
                NOW.plusSeconds(1800),
                NOW.plusSeconds(3600),
                InitiativeStatus.ACTIVE
        );
        UUID initiativeId = UUID.randomUUID();
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        InitiativeResponse response = initiativeService.update(initiativeId, request);

        assertThat(response.title()).isEqualTo("Updated");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.rewardDescription()).isEqualTo("Updated reward");
        assertThat(response.status()).isEqualTo(InitiativeStatus.ACTIVE);
    }

    @Test
    void listUsesAdminQueryForAdminPrincipal() {
        PageRequest pageable = PageRequest.of(0, 20);
        LearningInitiative initiative = initiative("OpenAI Certification", InitiativeStatus.ACTIVE, adminUser);
        when(initiativeRepository.findForAdmin(eq(InitiativeStatus.ACTIVE), eq("OpenAI"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(initiative), pageable, 1));

        Page<InitiativeResponse> response = initiativeService.list(
                InitiativeStatus.ACTIVE,
                "  OpenAI  ",
                pageable,
                adminPrincipal
        );

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().title()).isEqualTo("OpenAI Certification");
        verify(initiativeRepository).findForAdmin(InitiativeStatus.ACTIVE, "OpenAI", pageable);
    }

    @Test
    void listUsesActiveEmployeeQueryForEmployeePrincipal() {
        PageRequest pageable = PageRequest.of(0, 20);
        LearningInitiative initiative = initiative("Anthropic Learning", InitiativeStatus.ACTIVE, adminUser);
        when(initiativeRepository.findActiveForEmployee(eq("Anthropic"), eq(NOW), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(initiative), pageable, 1));

        Page<InitiativeResponse> response = initiativeService.list(
                InitiativeStatus.EXPIRED,
                "Anthropic",
                pageable,
                employeePrincipal
        );

        assertThat(response.getContent()).hasSize(1);
        verify(initiativeRepository).findActiveForEmployee("Anthropic", NOW, pageable);
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
    void deleteRemovesExistingInitiative() {
        UUID initiativeId = UUID.randomUUID();
        LearningInitiative initiative = initiative("To delete", InitiativeStatus.DRAFT, adminUser);
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(initiative));

        initiativeService.delete(initiativeId);

        verify(initiativeRepository).delete(initiative);
    }

    private User user(String employeeId, String email, String fullName, RoleName roleName) {
        User user = new User(employeeId, email, fullName, "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private LearningInitiative initiative(String title, InitiativeStatus status, User createdBy) {
        return new LearningInitiative(
                title,
                title + " description",
                "Reward",
                NOW.minusSeconds(3600),
                NOW.plusSeconds(3600),
                status,
                createdBy
        );
    }
}

