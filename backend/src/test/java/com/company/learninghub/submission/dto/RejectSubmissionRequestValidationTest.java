package com.company.learninghub.submission.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RejectSubmissionRequestValidationTest {

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
    void rejectionReasonIsRequired() {
        RejectSubmissionRequest request = new RejectSubmissionRequest(" ");

        Set<ConstraintViolation<RejectSubmissionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("must not be blank");
    }

    @Test
    void rejectionReasonIsLimitedToTwoThousandCharacters() {
        RejectSubmissionRequest request = new RejectSubmissionRequest("a".repeat(2001));

        Set<ConstraintViolation<RejectSubmissionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("size must be between 0 and 2000");
    }
}

