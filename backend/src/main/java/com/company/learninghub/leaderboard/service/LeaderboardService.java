package com.company.learninghub.leaderboard.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.initiative.domain.LearningInitiative;
import com.company.learninghub.initiative.repository.LearningInitiativeRepository;
import com.company.learninghub.leaderboard.dto.GlobalLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.InitiativeLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.LeaderboardEmployeeResponse;
import com.company.learninghub.leaderboard.dto.PersonalLeaderboardResponse;
import com.company.learninghub.leaderboard.dto.RecentApprovalResponse;
import com.company.learninghub.leaderboard.repository.LeaderboardQueryRepository;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LeaderboardService {

    private static final int RECENT_APPROVAL_LIMIT = 5;

    private final LeaderboardQueryRepository leaderboardQueryRepository;
    private final LearningInitiativeRepository initiativeRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public LeaderboardService(
            LeaderboardQueryRepository leaderboardQueryRepository,
            LearningInitiativeRepository initiativeRepository,
            UserRepository userRepository
    ) {
        this.leaderboardQueryRepository = leaderboardQueryRepository;
        this.initiativeRepository = initiativeRepository;
        this.userRepository = userRepository;
        this.clock = Clock.systemUTC();
    }

    LeaderboardService(
            LeaderboardQueryRepository leaderboardQueryRepository,
            LearningInitiativeRepository initiativeRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.leaderboardQueryRepository = leaderboardQueryRepository;
        this.initiativeRepository = initiativeRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<GlobalLeaderboardEntryResponse> getGlobalLeaderboard(Pageable pageable) {
        return leaderboardQueryRepository.findGlobalLeaderboard(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<InitiativeLeaderboardEntryResponse> getInitiativeLeaderboard(
            UUID initiativeId,
            Pageable pageable,
            AuthenticatedUser authenticatedUser
    ) {
        assertInitiativeAccessible(initiativeId, authenticatedUser);
        return leaderboardQueryRepository.findInitiativeLeaderboard(initiativeId, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public PersonalLeaderboardResponse getPersonalRanking(AuthenticatedUser authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
        GlobalLeaderboardEntryResponse globalRanking = leaderboardQueryRepository.findGlobalRankingForEmployee(user.getId());
        List<RecentApprovalResponse> recentApprovals = leaderboardQueryRepository.findRecentApprovalsForEmployee(
                user.getId(),
                PageRequest.of(
                        0,
                        RECENT_APPROVAL_LIMIT,
                        Sort.by(
                                Sort.Order.desc("approvedAtUtc"),
                                Sort.Order.desc("submittedAtUtc"),
                                Sort.Order.asc("submissionId")
                        )
                )
        );

        return new PersonalLeaderboardResponse(
                toEmployeeResponse(user),
                globalRanking == null ? null : globalRanking.rank(),
                globalRanking == null ? 0 : globalRanking.totalApprovedCertifications(),
                globalRanking == null ? null : globalRanking.earliestSubmittedAtUtc(),
                recentApprovals
        );
    }

    private void assertInitiativeAccessible(UUID initiativeId, AuthenticatedUser authenticatedUser) {
        LearningInitiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning initiative was not found"));
        if (isAdmin(authenticatedUser) || initiative.isVisibleToEmployeesAt(Instant.now(clock))) {
            return;
        }
        throw new ResourceNotFoundException("Learning initiative was not found");
    }

    private boolean isAdmin(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.getRoleNames().contains(RoleName.ADMIN);
    }

    private LeaderboardEmployeeResponse toEmployeeResponse(User user) {
        return new LeaderboardEmployeeResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}
