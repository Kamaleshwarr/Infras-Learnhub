package com.company.learninghub.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void passwordEncoderUsesBCrypt() {
        SecurityConfig securityConfig = new SecurityConfig(null, null, null, null);

        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String encoded = passwordEncoder.encode("ValidPass123");

        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches("ValidPass123", encoded)).isTrue();
    }
}

