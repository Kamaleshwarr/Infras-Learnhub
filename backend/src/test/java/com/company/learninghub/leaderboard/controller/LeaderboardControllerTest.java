package com.company.learninghub.leaderboard.controller;

import com.company.learninghub.leaderboard.dto.GlobalLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.LeaderboardEmployeeResponse;
import com.company.learninghub.leaderboard.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeaderboardControllerTest {

    @Test
    void globalLeaderboardWrapsServicePageResponse() {
        LeaderboardService leaderboardService = mock(LeaderboardService.class);
        LeaderboardController controller = new LeaderboardController(leaderboardService);
        PageRequest pageable = PageRequest.of(0, 20);
        GlobalLeaderboardEntryResponse entry = new GlobalLeaderboardEntryResponse(
                1,
                new LeaderboardEmployeeResponse(
                        UUID.randomUUID(),
                        "EMP001",
                        "Employee One",
                        "employee@learninghub.local"
                ),
                2,
                Instant.parse("2026-06-06T07:00:00Z"),
                Instant.parse("2026-06-06T08:00:00Z")
        );
        when(leaderboardService.getGlobalLeaderboard(pageable))
                .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));

        ResponseEntity<?> response = controller.getGlobalLeaderboard(pageable);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("totalElements", 1L);
        verify(leaderboardService).getGlobalLeaderboard(pageable);
    }
}

