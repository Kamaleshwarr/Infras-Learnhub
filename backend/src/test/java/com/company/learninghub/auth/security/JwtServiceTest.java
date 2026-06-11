package com.company.learninghub.auth.security;

import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String BASE64_SECRET =
            "VGhpcy1pcy1vbmx5LWEtbG9jYWwtZGV2ZWxvcG1lbnQtand0LXNlY3JldC0zMi1ieXRlcyE=";

    @Test
    void generateTokenCreatesValidTokenForUser() {
        JwtService jwtService = new JwtService(jwtProperties(Duration.ofMinutes(30)), fixedClock());
        UserDetails user = org.springframework.security.core.userdetails.User.withUsername("employee@example.com")
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
        UserDetails tokenOwner = org.springframework.security.core.userdetails.User.withUsername("employee@example.com")
                .password("not-used")
                .roles("EMPLOYEE")
                .build();
        UserDetails anotherUser = org.springframework.security.core.userdetails.User.withUsername("other@example.com")
                .password("not-used")
                .roles("EMPLOYEE")
                .build();

        String token = jwtService.generateToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    void isTokenValidRejectsTokenIssuedBeforePasswordChange() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-06T05:00:00Z"), ZoneOffset.UTC);
        JwtService jwtService = new JwtService(jwtProperties(Duration.ofMinutes(30)), clock);
        User user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setPasswordChangedAt(Instant.parse("2026-06-06T05:30:00Z"));
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);

        String token = jwtService.generateToken(authenticatedUser);

        assertThat(jwtService.isTokenValid(token, authenticatedUser)).isFalse();
    }

    @Test
    void isTokenValidRejectsDisabledUser() {
        JwtService jwtService = new JwtService(jwtProperties(Duration.ofMinutes(30)), fixedClock());
        User user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        user.setActive(false);
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);

        String token = jwtService.generateToken(authenticatedUser);

        assertThat(jwtService.isTokenValid(token, authenticatedUser)).isFalse();
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

