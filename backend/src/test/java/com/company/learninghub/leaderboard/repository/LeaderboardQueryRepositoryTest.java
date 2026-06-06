package com.company.learninghub.leaderboard.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardQueryRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private LeaderboardQueryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LeaderboardQueryRepository(jdbcTemplate);
    }

    @Test
    void initiativeLeaderboardUsesApprovedEligibilityAndSubmittedAtRankingRules() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        Page<?> page = repository.findInitiativeLeaderboard(UUID.randomUUID(), PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isZero();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(jdbcTemplate).query(sqlCaptor.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        String sql = normalized(sqlCaptor.getValue());
        assertThat(sql).contains("WHERE cs.approval_status = 'APPROVED'");
        assertThat(sql).contains("ORDER BY cs.submitted_at_utc ASC, cs.reviewed_at_utc ASC, cs.id ASC");
        assertThat(sql).contains("ROW_NUMBER() OVER");
        assertThat(sql).contains("ORDER BY rank ASC");
    }

    @Test
    void globalLeaderboardUsesApprovedTotalsAndEarliestSubmissionTieBreaker() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        repository.findGlobalLeaderboard(PageRequest.of(0, 20));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(jdbcTemplate).query(sqlCaptor.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        String sql = normalized(sqlCaptor.getValue());
        assertThat(sql).contains("WHERE cs.approval_status = 'APPROVED'");
        assertThat(sql).contains("ORDER BY total_approved_certifications DESC, earliest_submitted_at_utc ASC, employee_user_id ASC");
        assertThat(sql).contains("ROW_NUMBER() OVER");
        assertThat(sql).contains("ORDER BY rank ASC");
    }

    @Test
    void repositoryAppliesWhitelistedSortAndRejectsUnsupportedSort() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        repository.findGlobalLeaderboard(PageRequest.of(
                0,
                20,
                Sort.by(Sort.Order.desc("totalApprovedCertifications"), Sort.Order.asc("earliestSubmittedAtUtc"))
        ));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(jdbcTemplate).query(sqlCaptor.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        String sql = normalized(sqlCaptor.getValue());
        assertThat(sql).contains("ORDER BY total_approved_certifications DESC, earliest_submitted_at_utc ASC");

        assertThatThrownBy(() -> repository.findGlobalLeaderboard(PageRequest.of(0, 20, Sort.by("badField"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported sort property: badField");
    }

    private String normalized(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}

