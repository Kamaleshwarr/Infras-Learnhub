package com.company.learninghub.assistant.tool;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.leaderboard.dto.LeaderboardEmployeeResponse;
import com.company.learninghub.leaderboard.dto.PersonalLeaderboardResponse;
import com.company.learninghub.leaderboard.service.LeaderboardService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyLeaderboardRankToolTest {

    @Mock
    private LeaderboardService leaderboardService;

    private MyLeaderboardRankTool tool;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        tool = new MyLeaderboardRankTool(leaderboardService);
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        authenticatedUser = AuthenticatedUser.from(user);
    }

    @Test
    void returnsRankWhenPresent() {
        PersonalLeaderboardResponse ranking = new PersonalLeaderboardResponse(
                new LeaderboardEmployeeResponse(UUID.randomUUID(), "EMP001", "Jane Doe", "jane.doe@company.com"),
                3L,
                2L,
                null,
                List.of()
        );
        when(leaderboardService.getPersonalRanking(authenticatedUser)).thenReturn(ranking);

        ToolResult result = tool.execute(new AssistantToolContext(authenticatedUser, "my rank"));

        assertThat(result.text()).contains("#3");
        assertThat(result.structuredData()).isEqualTo(ranking);
    }

    @Test
    void returnsNoRankMessageWhenUnranked() {
        PersonalLeaderboardResponse ranking = new PersonalLeaderboardResponse(
                new LeaderboardEmployeeResponse(UUID.randomUUID(), "EMP001", "Jane Doe", "jane.doe@company.com"),
                null,
                0L,
                null,
                List.of()
        );
        when(leaderboardService.getPersonalRanking(authenticatedUser)).thenReturn(ranking);

        ToolResult result = tool.execute(new AssistantToolContext(authenticatedUser, "my rank"));

        assertThat(result.text()).contains("do not have an approved certification rank");
    }
}
