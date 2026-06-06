package com.company.learninghub;

import com.company.learninghub.auth.security.JwtProperties;
import com.company.learninghub.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, StorageProperties.class})
public class LearningHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningHubApplication.class, args);
    }
}

