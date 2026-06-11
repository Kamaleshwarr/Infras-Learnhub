package com.company.learninghub.auth.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    void acceptsValidPassword() {
        validator.validate("ValidPass1!", "user@example.com");
    }

    @Test
    void rejectsPasswordMatchingEmail() {
        assertThatThrownBy(() -> validator.validate("user@example.com", "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void collectsMultipleViolations() {
        List<String> violations = validator.collectViolations("short", "user@example.com");
        assertThat(violations).isNotEmpty();
    }
}
