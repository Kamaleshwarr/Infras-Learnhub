package com.company.learninghub.initiative.domain;

import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LearningInitiativeEmployeeVisibilityTest {

    private static final Instant NOW = Instant.parse("2026-06-16T12:00:00Z");

    @Test
    void activeInitiativeWithinUtcCalendarWindowIsVisible() {
        LearningInitiative initiative = initiative(
                InitiativeStatus.ACTIVE,
                Instant.parse("2026-06-16T00:00:00Z"),
                Instant.parse("2026-06-16T23:59:59Z")
        );

        assertThat(initiative.isVisibleToEmployeesAt(NOW)).isTrue();
        assertThat(initiative.employeeExclusionReasonsAt(NOW)).isEmpty();
    }

    @Test
    void activeInitiativeWithFutureStartDateIsExcluded() {
        LearningInitiative initiative = initiative(
                InitiativeStatus.ACTIVE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z")
        );

        assertThat(initiative.isVisibleToEmployeesAt(NOW)).isFalse();
        assertThat(initiative.employeeExclusionReasonsAt(NOW)).containsExactly("start_date_after_today_utc");
    }

    @Test
    void activeInitiativeWithPastExpiryDateIsExcluded() {
        LearningInitiative initiative = initiative(
                InitiativeStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-06-15T23:59:59Z")
        );

        assertThat(initiative.isVisibleToEmployeesAt(NOW)).isFalse();
        assertThat(initiative.employeeExclusionReasonsAt(NOW)).containsExactly("expiry_date_before_today_utc");
    }

    @Test
    void draftInitiativeIsExcludedEvenWhenDatesAreInWindow() {
        LearningInitiative initiative = initiative(
                InitiativeStatus.DRAFT,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z")
        );

        assertThat(initiative.isVisibleToEmployeesAt(NOW)).isFalse();
        assertThat(initiative.employeeExclusionReasonsAt(NOW)).containsExactly("status_not_active");
    }

    private LearningInitiative initiative(InitiativeStatus status, Instant startDateUtc, Instant expiryDateUtc) {
        User creator = new User("ADMIN001", "admin@learninghub.local", "Admin", "hash");
        return new LearningInitiative(
                "Investigation Initiative",
                "Description",
                "Reward",
                startDateUtc,
                expiryDateUtc,
                status,
                creator
        );
    }
}
