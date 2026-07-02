package com.company.learninghub.learn.migration;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers(disabledWithoutDocker = true)
class LearnCatalogFlywayMigrationTest {

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
    void cleanDatabaseMigratesThroughV13() {
        assertThatCode(this::migrateToLatest).doesNotThrowAnyException();

        try (Connection connection = openConnection()) {
            assertThat(readString(connection,
                    "SELECT status FROM learn_technologies WHERE slug = 'kubernetes'"
            )).isEqualTo("HIDDEN");
            assertThat(statusConstraintAllowsHidden(connection)).isTrue();
            assertThat(tableExists(connection, "learn_catalog_imports")).isTrue();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify clean database migration", exception);
        }
    }

    @Test
    void existingV12DatabaseMigratesToV13() {
        assertThatCode(this::migrateToV12).doesNotThrowAnyException();

        try (Connection connection = openConnection()) {
            assertThat(readString(connection,
                    "SELECT status FROM learn_technologies WHERE short_name = 'K8s'"
            )).isEqualTo("DRAFT");
            assertThat(readString(connection,
                    "SELECT category FROM learn_technologies WHERE short_name = 'Spring Boot'"
            )).isEqualTo("LANGUAGES");
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify V12 baseline", exception);
        }

        assertThatCode(this::migrateToLatest).doesNotThrowAnyException();

        try (Connection connection = openConnection()) {
            assertThat(readString(connection,
                    "SELECT status FROM learn_technologies WHERE slug = 'kubernetes'"
            )).isEqualTo("HIDDEN");
            assertThat(readString(connection,
                    "SELECT category FROM learn_technologies WHERE slug = 'spring-boot'"
            )).isEqualTo("BACKEND");
            assertThat(readString(connection,
                    "SELECT status FROM learn_technologies WHERE slug = 'aws'"
            )).isEqualTo("PUBLISHED");
            assertThat(statusConstraintAllowsHidden(connection)).isTrue();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify V13 migration", exception);
        }
    }

    @Test
    void v13SlugColumnRemainsTextNotBytea() throws SQLException {
        migrateToLatest();

        try (Connection connection = openConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("""
                     SELECT data_type
                     FROM information_schema.columns
                     WHERE table_schema = 'public'
                       AND table_name = 'learn_technologies'
                       AND column_name = 'slug'
                     """)) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("data_type")).isEqualTo("character varying");
        }
    }

    private void migrateToV12() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target("12")
                .load()
                .migrate();
    }

    private void migrateToLatest() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
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

    private String readString(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getString(1);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT to_regclass(?) IS NOT NULL"
        )) {
            statement.setString(1, "public." + tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }
    }

    private boolean statusConstraintAllowsHidden(Connection connection) throws SQLException {
        String constraintDefinition = readString(connection, """
                SELECT pg_get_constraintdef(oid)
                FROM pg_constraint
                WHERE conrelid = 'learn_technologies'::regclass
                  AND conname = 'chk_learn_technologies_status'
                """);
        return constraintDefinition.contains("'HIDDEN'");
    }
}
