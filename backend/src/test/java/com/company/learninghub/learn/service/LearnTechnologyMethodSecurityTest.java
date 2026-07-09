package com.company.learninghub.learn.service;

import com.company.learninghub.learn.domain.TechnologyStatus;
import com.company.learninghub.learn.dto.TechnologyCurationRequest;
import com.company.learninghub.learn.mapper.LearnTechnologyMapper;
import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.projectknowledge.repository.ProjectMemberRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = LearnTechnologyMethodSecurityTest.TestConfig.class)
class LearnTechnologyMethodSecurityTest {

    @Autowired
    private LearnTechnologyService technologyService;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotUpdateCuration() {
        assertThatThrownBy(() -> technologyService.updateCuration(
                UUID.randomUUID(),
                new TechnologyCurationRequest(true, TechnologyStatus.PUBLISHED, "notes")
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotListAdminTechnologies() {
        assertThatThrownBy(() -> technologyService.listAdminTechnologies(
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20)
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanListAdminTechnologies() {
        technologyService.listAdminTechnologies(null, null, null, null, PageRequest.of(0, 20));
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        LearnTechnologyService learnTechnologyService(
                LearnTechnologyRepository technologyRepository,
                LearnTechnologyProjectLinkRepository projectLinkRepository,
                ProjectRepository projectRepository,
                ProjectMemberRepository projectMemberRepository,
                LearnTechnologyMapper mapper
        ) {
            return new LearnTechnologyService(
                    technologyRepository,
                    projectLinkRepository,
                    projectRepository,
                    projectMemberRepository,
                    mapper
            );
        }

        @Bean
        LearnTechnologyRepository learnTechnologyRepository() {
            LearnTechnologyRepository repository = mock(LearnTechnologyRepository.class);
            when(repository.findAll(any(Specification.class), eq(PageRequest.of(0, 20))))
                    .thenReturn(new PageImpl<>(List.of()));
            return repository;
        }

        @Bean LearnTechnologyProjectLinkRepository learnTechnologyProjectLinkRepository() { return mock(LearnTechnologyProjectLinkRepository.class); }
        @Bean ProjectRepository projectRepository() { return mock(ProjectRepository.class); }
        @Bean ProjectMemberRepository projectMemberRepository() { return mock(ProjectMemberRepository.class); }
        @Bean LearnTechnologyMapper learnTechnologyMapper() { return new LearnTechnologyMapper(); }
    }
}
