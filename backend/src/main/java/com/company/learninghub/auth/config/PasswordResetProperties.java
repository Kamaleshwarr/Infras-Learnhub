package com.company.learninghub.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {

    private Duration expiration = Duration.ofHours(1);
    private String frontendResetUrl = "http://localhost:5173/reset-password";

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public String getFrontendResetUrl() {
        return frontendResetUrl;
    }

    public void setFrontendResetUrl(String frontendResetUrl) {
        this.frontendResetUrl = frontendResetUrl;
    }
}
