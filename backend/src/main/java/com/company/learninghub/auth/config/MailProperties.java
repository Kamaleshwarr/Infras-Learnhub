package com.company.learninghub.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    /**
     * Supported modes: log (development) or smtp (production).
     */
    private String mode = "log";
    private String from = "noreply@learninghub.local";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isLogMode() {
        return "log".equalsIgnoreCase(mode);
    }
}
