package com.company.learninghub.leaderboard.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.leaderboard.dto.GlobalLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.InitiativeLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.LeaderboardEmployeeResponse;
import com.company.learninghub.leaderboard.dto.PersonalLeaderboardResponse;
import com.company.learninghub.leaderboard.dto.RecentApprovalResponse;
import com.company.learninghub.leaderboard.repository.LeaderboardQueryRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
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
class LeaderboardServiceTest {

    private static final Instant SUBMITTED_AT = Instant.parse("2026-06-06T07:00:00Z");
    private static final Instant APPROVED_AT = Instant.parse("2026-06-06T08:00:00Z");

    @Mock
    private LeaderboardQueryRepository leaderboardQueryRepository;

    @Mock
    private UserRepository userRepository;

    private LeaderboardService leaderboardService;
    private User employee;
    private AuthenticatedUser principal;

    @BeforeEach
    void setUp() {
        leaderboardService = new LeaderboardService(leaderboardQueryRepository, userRepository);
        employee = user(RoleName.EMPLOYEE);
        principal = AuthenticatedUser.from(employee);
    }

    @Test
    void getGlobalLeaderboardDelegatesToQueryRepository() {
        PageRequest pageable = PageRequest.of(0, 20);
        GlobalLeaderboardEntryResponse entry = globalEntry(1, employee, 3);
        when(leaderboardQueryRepository.findGlobalLeaderboard(pageable))
                .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));

        Page<GlobalLeaderboardEntryResponse> response = leaderboardService.getGlobalLeaderboard(pageable);

        assertThat(response.getContent()).containsExactly(entry);
        verify(leaderboardQueryRepository).findGlobalLeaderboard(pageable);
    }

    @Test
    void getInitiativeLeaderboardDelegatesToQueryRepository() {
        UUID initiativeId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);
        InitiativeLeaderboardEntryResponse entry = new InitiativeLeaderboardEntryResponse(
                1,
                UUID.randomUUID(),
                employeeResponse(employee),
                initiativeId,
                "AWS AI",
                SUBMITTED_AT,
                APPROVED_AT
        );
        when(leaderboardQueryRepository.findInitiativeLeaderboard(initiativeId, pageable))
                .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));

        Page<InitiativeLeaderboardEntryResponse> response = leaderboardService.getInitiativeLeaderboard(initiativeId, pageable);

        assertThat(response.getContent()).containsExactly(entry);
        verify(leaderboardQueryRepository).findInitiativeLeaderboard(initiativeId, pageable);
    }

    @Test
    void getPersonalRankingReturnsGlobalRankAndRecentApprovals() {
        GlobalLeaderboardEntryResponse globalEntry = globalEntry(2, employee, 4);
        RecentApprovalResponse recentApproval = new RecentApprovalResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "AWS AI",
                SUBMITTED_AT,
                APPROVED_AT
        );
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(leaderboardQueryRepository.findGlobalRankingForEmployee(employee.getId())).thenReturn(globalEntry);
        when(leaderboardQueryRepository.findRecentApprovalsForEmployee(eq(employee.getId()), any()))
                .thenReturn(List.of(recentApproval));

        PersonalLeaderboardResponse response = leaderboardService.getPersonalRanking(principal);

        assertThat(response.employee().id()).isEqualTo(employee.getId());
        assertThat(response.globalRank()).isEqualTo(2);
        assertThat(response.totalApprovedCertifications()).isEqualTo(4);
        assertThat(response.earliestSubmittedAtUtc()).isEqualTo(SUBMITTED_AT);
        assertThat(response.recentApprovals()).containsExactly(recentApproval);
    }

    @Test
    void getPersonalRankingReturnsEmptyRankingWhenEmployeeHasNoApprovedSubmissions() {
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(leaderboardQueryRepository.findGlobalRankingForEmployee(employee.getId())).thenReturn(null);
        when(leaderboardQueryRepository.findRecentApprovalsForEmployee(eq(employee.getId()), any()))
                .thenReturn(List.of());

        PersonalLeaderboardResponse response = leaderboardService.getPersonalRanking(principal);

        assertThat(response.globalRank()).isNull();
        assertThat(response.totalApprovedCertifications()).isZero();
        assertThat(response.earliestSubmittedAtUtc()).isNull();
        assertThat(response.recentApprovals()).isEmpty();
    }

    @Test
    void getPersonalRankingFailsWhenAuthenticatedUserCannotBeFound() {
        when(userRepository.findById(employee.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaderboardService.getPersonalRanking(principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Authenticated user was not found");
    }

    private GlobalLeaderboardEntryResponse globalEntry(long rank, User user, long totalApproved) {
        return new GlobalLeaderboardEntryResponse(
                rank,
                employeeResponse(user),
                totalApproved,
                SUBMITTED_AT,
                APPROVED_AT
        );
    }

    private LeaderboardEmployeeResponse employeeResponse(User user) {
        return new LeaderboardEmployeeResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    private User user(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }
}

