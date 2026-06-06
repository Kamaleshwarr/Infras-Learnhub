package com.company.learninghub.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String BASE64_SECRET =
            "VGhpcy1pcy1vbmx5LWEtbG9jYWwtZGV2ZWxvcG1lbnQtand0LXNlY3JldC0zMi1ieXRlcyE=";

    @Test
    void generateTokenCreatesValidTokenForUser() {
        JwtService jwtService = new JwtService(jwtProperties(Duration.ofMinutes(30)), fixedClock());
        UserDetails user = User.withUsername("employee@example.com")
                .password("not-used")
                .roles("EMPLOYEE")
                .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("employee@example.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
        assertThat(jwtService.expirationSeconds()).isEqualTo(1800);
    }

    @Test
    void isTokenValidRejectsTokenForDifferentUser() {
        JwtService jwtService = new JwtService(jwtProperties(Duration.ofMinutes(30)), fixedClock());
        UserDetails tokenOwner = User.withUsername("employee@example.com")
                .password("not-used")
                .roles("EMPLOYEE")
                .build();
        UserDetails anotherUser = User.withUsername("other@example.com")
                .password("not-used")
                .roles("EMPLOYEE")
                .build();

        String token = jwtService.generateToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    private JwtProperties jwtProperties(Duration expiration) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(BASE64_SECRET);
        properties.setIssuer("engineering-learning-hub-test");
        properties.setExpiration(expiration);
        return properties;
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-06-06T05:00:00Z"), ZoneOffset.UTC);
    }
}

