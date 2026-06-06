package com.company.learninghub.leaderboard.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private LeaderboardQueryRepository leaderboardQueryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanViewGlobalAndInitiativeLeaderboards() {
        PageRequest pageable = PageRequest.of(0, 20);
        UUID initiativeId = UUID.randomUUID();
        when(leaderboardQueryRepository.findGlobalLeaderboard(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(leaderboardQueryRepository.findInitiativeLeaderboard(initiativeId, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(leaderboardService.getGlobalLeaderboard(pageable).getTotalElements()).isZero();
        assertThat(leaderboardService.getInitiativeLeaderboard(initiativeId, pageable).getTotalElements()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
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
    void unauthenticatedCallIsDenied() {
        assertThatThrownBy(() -> leaderboardService.getGlobalLeaderboard(PageRequest.of(0, 20)))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
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
                UserRepository userRepository
        ) {
            return new LeaderboardService(leaderboardQueryRepository, userRepository);
        }

        @Bean
        LeaderboardQueryRepository leaderboardQueryRepository() {
            return mock(LeaderboardQueryRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }
}

