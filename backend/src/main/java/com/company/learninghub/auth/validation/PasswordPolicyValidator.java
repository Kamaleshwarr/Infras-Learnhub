package com.company.learninghub.auth.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class PasswordPolicyValidator {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public void validate(String password, String email) {
        List<String> violations = collectViolations(password, email);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", violations));
        }
    }

    public List<String> collectViolations(String password, String email) {
        List<String> violations = new ArrayList<>();
        if (!StringUtils.hasText(password)) {
            violations.add("Password is required.");
            return violations;
        }
        if (password.length() < 8) {
            violations.add("Password must be at least 8 characters.");
        }
        if (password.length() > 128) {
            violations.add("Password must be at most 128 characters.");
        }
        if (!UPPERCASE.matcher(password).find()) {
            violations.add("Password must contain at least one uppercase letter.");
        }
        if (!LOWERCASE.matcher(password).find()) {
            violations.add("Password must contain at least one lowercase letter.");
        }
        if (!DIGIT.matcher(password).find()) {
            violations.add("Password must contain at least one digit.");
        }
        if (!SPECIAL.matcher(password).find()) {
            violations.add("Password must contain at least one special character.");
        }
        if (StringUtils.hasText(email) && password.equalsIgnoreCase(email.trim())) {
            violations.add("Password must not match the email address.");
        }
        return violations;
    }
}
