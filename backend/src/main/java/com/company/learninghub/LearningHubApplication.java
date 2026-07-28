package com.company.learninghub;

import com.company.learninghub.assistant.config.AssistantProperties;
import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.auth.config.PasswordResetProperties;
import com.company.learninghub.auth.security.JwtProperties;
import com.company.learninghub.learn.catalog.CatalogImportProperties;
import com.company.learninghub.profile.config.ProfileProperties;
import com.company.learninghub.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        StorageProperties.class,
        PasswordResetProperties.class,
        ProfileProperties.class,
        CatalogImportProperties.class,
        CommunicationProperties.class,
        AssistantProperties.class
})
public class LearningHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningHubApplication.class, args);
    }
}

