package com.company.learninghub.learn.service;

import com.company.learninghub.learn.dto.CreateResourceOverrideRequest;
import com.company.learninghub.learn.mapper.LearnResourceOverrideMapper;
import com.company.learninghub.learn.repository.LearnRoadmapRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageRepository;
import com.company.learninghub.learn.repository.LearnRoadmapStageResourceRepository;
import com.company.learninghub.learn.repository.LearnStageResourceOverrideRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = LearnResourceOverrideMethodSecurityTest.TestConfig.class)
class LearnResourceOverrideMethodSecurityTest {

    @Autowired
    private LearnResourceOverrideService overrideService;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotCreateOverride() {
        assertThatThrownBy(() -> overrideService.createOverride(
                UUID.randomUUID(),
                new CreateResourceOverrideRequest(
                        "introduction",
                        "oracle-docs",
                        "oracle-docs",
                        "LEARNING",
                        false,
                        "https://internal.example.com/java",
                        false,
                        true,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanListEnabledOverrides() {
        overrideService.listEnabledOverrides("java");
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        ResourceOverrideResolver resourceOverrideResolver() {
            return new ResourceOverrideResolver();
        }

        @Bean
        LearnResourceOverrideMapper learnResourceOverrideMapper(ResourceOverrideResolver resolver) {
            return new LearnResourceOverrideMapper(resolver);
        }

        @Bean
        LearnResourceOverrideService learnResourceOverrideService(
                LearnTechnologyRepository technologyRepository,
                LearnRoadmapRepository roadmapRepository,
                LearnRoadmapStageRepository stageRepository,
                LearnRoadmapStageResourceRepository resourceRepository,
                LearnStageResourceOverrideRepository overrideRepository,
                ResourceOverrideResolver resolver,
                LearnResourceOverrideMapper mapper
        ) {
            return new LearnResourceOverrideService(
                    technologyRepository,
                    roadmapRepository,
                    stageRepository,
                    resourceRepository,
                    overrideRepository,
                    resolver,
                    mapper
            );
        }

        @Bean
        LearnTechnologyRepository learnTechnologyRepository() {
            return mock(LearnTechnologyRepository.class);
        }

        @Bean
        LearnRoadmapRepository learnRoadmapRepository() {
            return mock(LearnRoadmapRepository.class);
        }

        @Bean
        LearnRoadmapStageRepository learnRoadmapStageRepository() {
            return mock(LearnRoadmapStageRepository.class);
        }

        @Bean
        LearnRoadmapStageResourceRepository learnRoadmapStageResourceRepository() {
            return mock(LearnRoadmapStageResourceRepository.class);
        }

        @Bean
        LearnStageResourceOverrideRepository learnStageResourceOverrideRepository() {
            return mock(LearnStageResourceOverrideRepository.class);
        }
    }
}
