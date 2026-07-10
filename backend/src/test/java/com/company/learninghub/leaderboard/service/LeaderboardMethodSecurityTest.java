package com.company.learninghub.leaderboard.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.initiative.domain.InitiativeStatus;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.leaderboard.repository.LeaderboardQueryRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = LeaderboardMethodSecurityTest.TestConfig.class)
class LeaderboardMethodSecurityTest {

    private static final Instant NOW = Instant.parse("2026-06-15T12:00:00Z");

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private LeaderboardQueryRepository leaderboardQueryRepository;

    @Autowired
    private LearningInitiativeRepository initiativeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "EMPLOYEE")
    void employeeCanViewGlobalAndVisibleInitiativeLeaderboards() {
        PageRequest pageable = PageRequest.of(0, 20);
        UUID initiativeId = UUID.randomUUID();
        when(leaderboardQueryRepository.findGlobalLeaderboard(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(visibleInitiative(initiativeId)));
        when(leaderboardQueryRepository.findInitiativeLeaderboard(initiativeId, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(leaderboardService.getGlobalLeaderboard(pageable).getTotalElements()).isZero();
        assertThat(leaderboardService.getInitiativeLeaderboard(
                initiativeId,
                pageable,
                AuthenticatedUser.from(user(RoleName.EMPLOYEE))
        ).getTotalElements()).isZero();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void adminCanViewGlobalAndInitiativeLeaderboards() {
        PageRequest pageable = PageRequest.of(0, 20);
        UUID initiativeId = UUID.randomUUID();
        when(leaderboardQueryRepository.findGlobalLeaderboard(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(initiativeRepository.findById(initiativeId)).thenReturn(Optional.of(draftInitiative(initiativeId)));
        when(leaderboardQueryRepository.findInitiativeLeaderboard(initiativeId, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(leaderboardService.getGlobalLeaderboard(pageable).getTotalElements()).isZero();
        assertThat(leaderboardService.getInitiativeLeaderboard(
                initiativeId,
                pageable,
                AuthenticatedUser.from(user(RoleName.ADMIN))
        ).getTotalElements()).isZero();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void adminCanViewPersonalRanking() {
        User admin = user(RoleName.ADMIN);
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(leaderboardQueryRepository.findGlobalRankingForEmployee(admin.getId())).thenReturn(null);
        when(leaderboardQueryRepository.findRecentApprovalsForEmployee(eq(admin.getId()), any()))
                .thenReturn(List.of());

        assertThat(leaderboardService.getPersonalRanking(AuthenticatedUser.from(admin)).totalApprovedCertifications())
                .isZero();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "EMPLOYEE")
    void employeeCanViewPersonalRanking() {
        User employee = user(RoleName.EMPLOYEE);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(leaderboardQueryRepository.findGlobalRankingForEmployee(employee.getId())).thenReturn(null);
        when(leaderboardQueryRepository.findRecentApprovalsForEmployee(eq(employee.getId()), any()))
                .thenReturn(List.of());

        assertThat(leaderboardService.getPersonalRanking(AuthenticatedUser.from(employee)).totalApprovedCertifications())
                .isZero();
    }

    @Test
    void unauthenticatedCallIsDenied() {
        assertThatThrownBy(() -> leaderboardService.getGlobalLeaderboard(PageRequest.of(0, 20)))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private LearningInitiative visibleInitiative(UUID initiativeId) {
        User admin = user(RoleName.ADMIN);
        LearningInitiative initiative = new LearningInitiative(
                "Visible Initiative",
                "Description",
                "Reward",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                InitiativeStatus.ACTIVE,
                admin
        );
        ReflectionTestUtils.setField(initiative, "id", initiativeId);
        return initiative;
    }

    private LearningInitiative draftInitiative(UUID initiativeId) {
        User admin = user(RoleName.ADMIN);
        LearningInitiative initiative = new LearningInitiative(
                "Draft Initiative",
                "Description",
                "Reward",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                InitiativeStatus.DRAFT,
                admin
        );
        ReflectionTestUtils.setField(initiative, "id", initiativeId);
        return initiative;
    }

    private User user(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        LeaderboardService leaderboardService(
                LeaderboardQueryRepository leaderboardQueryRepository,
                LearningInitiativeRepository initiativeRepository,
                UserRepository userRepository
        ) {
            return new LeaderboardService(
                    leaderboardQueryRepository,
                    initiativeRepository,
                    userRepository,
                    Clock.fixed(NOW, ZoneOffset.UTC)
            );
        }

        @Bean
        LeaderboardQueryRepository leaderboardQueryRepository() {
            return mock(LeaderboardQueryRepository.class);
        }

        @Bean
        LearningInitiativeRepository initiativeRepository() {
            return mock(LearningInitiativeRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }
}
