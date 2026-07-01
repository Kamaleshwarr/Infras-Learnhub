package com.company.learninghub.initiative.dto;

import com.company.learninghub.initiative.domain.InitiativeStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InitiativeRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void createRequestRejectsExpiryBeforeStart() {
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "AI Certification",
                "Complete the certification.",
                "Recognition",
                Instant.parse("2026-06-10T00:00:00Z"),
                Instant.parse("2026-06-09T00:00:00Z"),
                InitiativeStatus.ACTIVE
        );

        Set<ConstraintViolation<CreateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("expiryDateUtc must be on or after startDateUtc");
    }

    @Test
    void createRequestAllowsExpiryOnSameDayAsStart() {
        Instant sameDay = Instant.parse("2026-06-10T00:00:00Z");
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "One-day Workshop",
                "Single-day learning event.",
                null,
                sameDay,
                sameDay,
                InitiativeStatus.DRAFT
        );

        Set<ConstraintViolation<CreateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateRequestRejectsExpiryBeforeStart() {
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "AI Certification",
                "Complete the certification.",
                "Recognition",
                Instant.parse("2026-06-10T00:00:00Z"),
                Instant.parse("2026-06-09T00:00:00Z"),
                InitiativeStatus.ACTIVE
        );

        Set<ConstraintViolation<UpdateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("expiryDateUtc must be on or after startDateUtc");
    }

    @Test
    void updateRequestAllowsExpiryOnSameDayAsStart() {
        Instant sameDay = Instant.parse("2026-06-10T00:00:00Z");
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(
                "One-day Workshop",
                "Single-day learning event.",
                null,
                sameDay,
                sameDay,
                InitiativeStatus.DRAFT
        );

        Set<ConstraintViolation<UpdateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateRequestRequiresTitleDescriptionDatesAndStatus() {
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(null, "", null, null, null, null);

        Set<ConstraintViolation<UpdateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "description", "startDateUtc", "expiryDateUtc", "status");
    }

    @Test
    void createRequestRejectsTextFieldsLongerThanAllowedLimits() {
        Instant sameDay = Instant.parse("2026-06-10T00:00:00Z");
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "t".repeat(101),
                "d".repeat(2001),
                "r".repeat(501),
                sameDay,
                sameDay,
                InitiativeStatus.DRAFT
        );

        Set<ConstraintViolation<CreateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "description", "rewardDescription");
    }

    @Test
    void createRequestAcceptsTextFieldsAtMaximumAllowedLimits() {
        Instant sameDay = Instant.parse("2026-06-10T00:00:00Z");
        CreateInitiativeRequest request = new CreateInitiativeRequest(
                "t".repeat(100),
                "d".repeat(2000),
                "r".repeat(500),
                sameDay,
                sameDay,
                InitiativeStatus.DRAFT
        );

        Set<ConstraintViolation<CreateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
