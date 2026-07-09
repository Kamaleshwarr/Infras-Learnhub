package com.company.learninghub.projectknowledge.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers(disabledWithoutDocker = true)
class ProjectStatusFlywayMigrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void resetDatabase() throws SQLException {
        try (Connection connection = openConnection()) {
            connection.createStatement().execute("DROP SCHEMA public CASCADE");
            connection.createStatement().execute("CREATE SCHEMA public");
            connection.createStatement().execute("GRANT ALL ON SCHEMA public TO public");
        }
    }

    @Test
    void existingProjectRowsReceiveActiveStatusOnV17Upgrade() throws SQLException {
        migrateTo("16");

        UUID projectId;
        try (Connection connection = openConnection()) {
            UUID userId = readUuid(connection, "SELECT id FROM users WHERE email = 'admin@learninghub.local'");
            projectId = UUID.randomUUID();
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO projects (id, name, description, access_type, archived, created_by, created_at_utc, updated_at_utc)
                    VALUES (?, 'Legacy Project', 'Before status column', 'PUBLIC', FALSE, ?, NOW(), NOW())
                    """)) {
                statement.setObject(1, projectId);
                statement.setObject(2, userId);
                statement.executeUpdate();
            }
        }

        assertThatCode(() -> migrateTo("17")).doesNotThrowAnyException();

        try (Connection connection = openConnection()) {
            assertThat(readString(connection, "SELECT status FROM projects WHERE id = ?", projectId)).isEqualTo("ACTIVE");
        }
    }

    @Test
    void archivedProjectsBackfillToArchivedStatus() throws SQLException {
        migrateTo("16");

        UUID projectId;
        try (Connection connection = openConnection()) {
            UUID userId = readUuid(connection, "SELECT id FROM users WHERE email = 'admin@learninghub.local'");
            projectId = UUID.randomUUID();
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO projects (id, name, description, access_type, archived, created_by, created_at_utc, updated_at_utc)
                    VALUES (?, 'Archived Project', 'Should backfill', 'PUBLIC', TRUE, ?, NOW(), NOW())
                    """)) {
                statement.setObject(1, projectId);
                statement.setObject(2, userId);
                statement.executeUpdate();
            }
        }

        migrateTo("17");

        try (Connection connection = openConnection()) {
            assertThat(readString(connection, "SELECT status FROM projects WHERE id = ?", projectId)).isEqualTo("ARCHIVED");
        }
    }

    private void migrateTo(String version) {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target(version)
                .load()
                .migrate();
    }

    private Connection openConnection() throws SQLException {
        return java.sql.DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
    }

    private UUID readUuid(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return (UUID) resultSet.getObject("id");
        }
    }

    private String readString(Connection connection, String sql, UUID projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                return resultSet.getString(1);
            }
        }
    }
}
