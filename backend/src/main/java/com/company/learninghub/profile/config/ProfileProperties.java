package com.company.learninghub.profile.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.profile")
public class ProfileProperties {

    @Min(1)
    private long avatarMaxSizeBytes = 2_097_152L;

    public long getAvatarMaxSizeBytes() {
        return avatarMaxSizeBytes;
    }

    public void setAvatarMaxSizeBytes(long avatarMaxSizeBytes) {
        this.avatarMaxSizeBytes = avatarMaxSizeBytes;
    }
}
