package com.company.learninghub.leaderboard.repository;

import com.company.learninghub.leaderboard.dto.GlobalLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.InitiativeLeaderboardEntryResponse;
import com.company.learninghub.leaderboard.dto.LeaderboardEmployeeResponse;
import com.company.learninghub.leaderboard.dto.RecentApprovalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class LeaderboardQueryRepository {

    private static final Map<String, String> INITIATIVE_SORT_COLUMNS = Map.of(
            "rank", "rank",
            "submissionId", "submission_id",
            "employeeId", "employee_number",
            "employeeName", "employee_name",
            "submittedAtUtc", "submitted_at_utc",
            "approvedAtUtc", "approved_at_utc"
    );

    private static final Map<String, String> GLOBAL_SORT_COLUMNS = Map.of(
            "rank", "rank",
            "employeeId", "employee_number",
            "employeeName", "employee_name",
            "totalApprovedCertifications", "total_approved_certifications",
            "earliestSubmittedAtUtc", "earliest_submitted_at_utc",
            "latestApprovedAtUtc", "latest_approved_at_utc"
    );

    private static final Map<String, String> RECENT_APPROVAL_SORT_COLUMNS = Map.of(
            "submissionId", "submission_id",
            "initiativeTitle", "initiative_title",
            "submittedAtUtc", "submitted_at_utc",
            "approvedAtUtc", "approved_at_utc"
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LeaderboardQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<InitiativeLeaderboardEntryResponse> findInitiativeLeaderboard(UUID initiativeId, Pageable pageable) {
        String sql = """
                SELECT *
                FROM (
                    SELECT
                        ROW_NUMBER() OVER (
                            ORDER BY
                                cs.submitted_at_utc ASC,
                                cs.reviewed_at_utc ASC,
                                cs.id ASC
                        ) AS rank,
                        cs.id AS submission_id,
                        u.id AS employee_user_id,
                        u.employee_id AS employee_number,
                        u.full_name AS employee_name,
                        u.email AS employee_email,
                        li.id AS initiative_id,
                        li.title AS initiative_title,
                        cs.submitted_at_utc,
                        cs.reviewed_at_utc AS approved_at_utc
                    FROM certificate_submissions cs
                    JOIN users u ON u.id = cs.employee_id
                    JOIN learning_initiatives li ON li.id = cs.initiative_id
                    WHERE cs.approval_status = 'APPROVED'
                      AND cs.initiative_id = :initiativeId
                ) ranked
                %s
                LIMIT :limit OFFSET :offset
                """.formatted(orderBy(pageable, INITIATIVE_SORT_COLUMNS, "rank ASC"));
        String countSql = """
                SELECT COUNT(*)
                FROM certificate_submissions cs
                WHERE cs.approval_status = 'APPROVED'
                  AND cs.initiative_id = :initiativeId
                """;
        MapSqlParameterSource parameters = paginationParameters(pageable)
                .addValue("initiativeId", initiativeId);

        List<InitiativeLeaderboardEntryResponse> content = jdbcTemplate.query(
                sql,
                parameters,
                this::mapInitiativeLeaderboardEntry
        );
        long total = jdbcTemplate.queryForObject(countSql, parameters, Long.class);
        return new PageImpl<>(content, pageable, total);
    }

    public Page<GlobalLeaderboardEntryResponse> findGlobalLeaderboard(Pageable pageable) {
        String sql = """
                WITH employee_totals AS (
                    SELECT
                        u.id AS employee_user_id,
                        u.employee_id AS employee_number,
                        u.full_name AS employee_name,
                        u.email AS employee_email,
                        COUNT(cs.id) AS total_approved_certifications,
                        MIN(cs.submitted_at_utc) AS earliest_submitted_at_utc,
                        MAX(cs.reviewed_at_utc) AS latest_approved_at_utc
                    FROM certificate_submissions cs
                    JOIN users u ON u.id = cs.employee_id
                    WHERE cs.approval_status = 'APPROVED'
                    GROUP BY u.id, u.employee_id, u.full_name, u.email
                )
                SELECT *
                FROM (
                    SELECT
                        ROW_NUMBER() OVER (
                            ORDER BY
                                total_approved_certifications DESC,
                                earliest_submitted_at_utc ASC,
                                employee_user_id ASC
                        ) AS rank,
                        employee_user_id,
                        employee_number,
                        employee_name,
                        employee_email,
                        total_approved_certifications,
                        earliest_submitted_at_utc,
                        latest_approved_at_utc
                    FROM employee_totals
                ) ranked
                %s
                LIMIT :limit OFFSET :offset
                """.formatted(orderBy(pageable, GLOBAL_SORT_COLUMNS, "rank ASC"));
        String countSql = """
                SELECT COUNT(DISTINCT cs.employee_id)
                FROM certificate_submissions cs
                WHERE cs.approval_status = 'APPROVED'
                """;
        MapSqlParameterSource parameters = paginationParameters(pageable);

        List<GlobalLeaderboardEntryResponse> content = jdbcTemplate.query(
                sql,
                parameters,
                this::mapGlobalLeaderboardEntry
        );
        long total = jdbcTemplate.queryForObject(countSql, parameters, Long.class);
        return new PageImpl<>(content, pageable, total);
    }

    public GlobalLeaderboardEntryResponse findGlobalRankingForEmployee(UUID employeeId) {
        String sql = """
                WITH employee_totals AS (
                    SELECT
                        u.id AS employee_user_id,
                        u.employee_id AS employee_number,
                        u.full_name AS employee_name,
                        u.email AS employee_email,
                        COUNT(cs.id) AS total_approved_certifications,
                        MIN(cs.submitted_at_utc) AS earliest_submitted_at_utc,
                        MAX(cs.reviewed_at_utc) AS latest_approved_at_utc
                    FROM certificate_submissions cs
                    JOIN users u ON u.id = cs.employee_id
                    WHERE cs.approval_status = 'APPROVED'
                    GROUP BY u.id, u.employee_id, u.full_name, u.email
                ),
                ranked AS (
                    SELECT
                        ROW_NUMBER() OVER (
                            ORDER BY
                                total_approved_certifications DESC,
                                earliest_submitted_at_utc ASC,
                                employee_user_id ASC
                        ) AS rank,
                        employee_user_id,
                        employee_number,
                        employee_name,
                        employee_email,
                        total_approved_certifications,
                        earliest_submitted_at_utc,
                        latest_approved_at_utc
                    FROM employee_totals
                )
                SELECT *
                FROM ranked
                WHERE employee_user_id = :employeeId
                """;
        List<GlobalLeaderboardEntryResponse> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("employeeId", employeeId),
                this::mapGlobalLeaderboardEntry
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public List<RecentApprovalResponse> findRecentApprovalsForEmployee(UUID employeeId, Pageable pageable) {
        String sql = """
                SELECT
                    cs.id AS submission_id,
                    li.id AS initiative_id,
                    li.title AS initiative_title,
                    cs.submitted_at_utc,
                    cs.reviewed_at_utc AS approved_at_utc
                FROM certificate_submissions cs
                JOIN learning_initiatives li ON li.id = cs.initiative_id
                WHERE cs.approval_status = 'APPROVED'
                  AND cs.employee_id = :employeeId
                %s
                LIMIT :limit OFFSET :offset
                """.formatted(orderBy(pageable, RECENT_APPROVAL_SORT_COLUMNS, "approved_at_utc DESC, submitted_at_utc DESC, submission_id ASC"));
        MapSqlParameterSource parameters = paginationParameters(pageable)
                .addValue("employeeId", employeeId);
        return jdbcTemplate.query(sql, parameters, this::mapRecentApproval);
    }

    private MapSqlParameterSource paginationParameters(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new MapSqlParameterSource()
                    .addValue("limit", Integer.MAX_VALUE)
                    .addValue("offset", 0);
        }
        return new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());
    }

    private String orderBy(Pageable pageable, Map<String, String> allowedColumns, String defaultOrderBy) {
        if (pageable.isUnpaged() || pageable.getSort().isUnsorted()) {
            return "ORDER BY " + defaultOrderBy;
        }

        String orderBy = pageable.getSort().stream()
                .map(order -> toOrderBy(order, allowedColumns))
                .reduce((left, right) -> left + ", " + right)
                .orElse(defaultOrderBy);
        return "ORDER BY " + orderBy;
    }

    private String toOrderBy(Sort.Order order, Map<String, String> allowedColumns) {
        String column = allowedColumns.get(order.getProperty());
        if (column == null) {
            throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        }
        return column + " " + order.getDirection().name();
    }

    private InitiativeLeaderboardEntryResponse mapInitiativeLeaderboardEntry(ResultSet rs, int rowNum) throws SQLException {
        return new InitiativeLeaderboardEntryResponse(
                rs.getLong("rank"),
                rs.getObject("submission_id", UUID.class),
                mapEmployee(rs),
                rs.getObject("initiative_id", UUID.class),
                rs.getString("initiative_title"),
                instant(rs, "submitted_at_utc"),
                instant(rs, "approved_at_utc")
        );
    }

    private GlobalLeaderboardEntryResponse mapGlobalLeaderboardEntry(ResultSet rs, int rowNum) throws SQLException {
        return new GlobalLeaderboardEntryResponse(
                rs.getLong("rank"),
                mapEmployee(rs),
                rs.getLong("total_approved_certifications"),
                instant(rs, "earliest_submitted_at_utc"),
                instant(rs, "latest_approved_at_utc")
        );
    }

    private RecentApprovalResponse mapRecentApproval(ResultSet rs, int rowNum) throws SQLException {
        return new RecentApprovalResponse(
                rs.getObject("submission_id", UUID.class),
                rs.getObject("initiative_id", UUID.class),
                rs.getString("initiative_title"),
                instant(rs, "submitted_at_utc"),
                instant(rs, "approved_at_utc")
        );
    }

    private LeaderboardEmployeeResponse mapEmployee(ResultSet rs) throws SQLException {
        return new LeaderboardEmployeeResponse(
                rs.getObject("employee_user_id", UUID.class),
                rs.getString("employee_number"),
                rs.getString("employee_name"),
                rs.getString("employee_email")
        );
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }
}

