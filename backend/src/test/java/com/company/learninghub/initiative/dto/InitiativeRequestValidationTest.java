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
                .contains("expiryDateUtc must be after startDateUtc");
    }

    @Test
    void updateRequestRequiresTitleDescriptionDatesAndStatus() {
        UpdateInitiativeRequest request = new UpdateInitiativeRequest(null, "", null, null, null, null);

        Set<ConstraintViolation<UpdateInitiativeRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "description", "startDateUtc", "expiryDateUtc", "status");
    }
}

