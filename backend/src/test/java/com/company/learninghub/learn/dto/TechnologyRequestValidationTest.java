package com.company.learninghub.learn.dto;

import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createRequestRejectsBlankName() {
        TechnologyCreateRequest request = new TechnologyCreateRequest(
                " ",
                "AWS",
                null,
                TechnologyCategory.CLOUD,
                TechnologyDifficulty.BEGINNER
        );

        Set<ConstraintViolation<TechnologyCreateRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("name"));
    }

    @Test
    void createRequestRejectsLongShortName() {
        TechnologyCreateRequest request = new TechnologyCreateRequest(
                "AWS",
                "A".repeat(31),
                null,
                TechnologyCategory.CLOUD,
                TechnologyDifficulty.BEGINNER
        );

        Set<ConstraintViolation<TechnologyCreateRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("shortName"));
    }
}
