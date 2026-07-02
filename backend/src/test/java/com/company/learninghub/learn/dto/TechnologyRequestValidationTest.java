package com.company.learninghub.learn.dto;

import com.company.learninghub.learn.domain.TechnologyStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void curationRequestRejectsLongOrgNotes() {
        TechnologyCurationRequest request = new TechnologyCurationRequest(
                true,
                TechnologyStatus.PUBLISHED,
                "A".repeat(2001)
        );

        Set<ConstraintViolation<TechnologyCurationRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("orgNotes"));
    }

    @Test
    void curationRequestAllowsPartialUpdate() {
        TechnologyCurationRequest request = new TechnologyCurationRequest(true, null, null);

        Set<ConstraintViolation<TechnologyCurationRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
