package com.company.learninghub.learn.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.dto.TechnologyCreateRequest;
import com.company.learninghub.learn.mapper.LearnTechnologyMapper;
import com.company.learninghub.learn.repository.LearnTechnologyProjectLinkRepository;
import com.company.learninghub.learn.repository.LearnTechnologyRepository;
import com.company.learninghub.projectknowledge.repository.ProjectRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
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

    @Autowired
    private LearnTechnologyRepository technologyRepository;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotCreateTechnology() {
        assertThatThrownBy(() -> technologyService.create(createRequest(), principal(RoleName.EMPLOYEE)))
                .isInstanceOf(AccessDeniedException.class);
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

    private TechnologyCreateRequest createRequest() {
        return new TechnologyCreateRequest(
                "AWS",
                "AWS",
                "Cloud",
                TechnologyCategory.CLOUD,
                TechnologyDifficulty.BEGINNER
        );
    }

    private AuthenticatedUser principal(RoleName roleName) {
        return AuthenticatedUser.from(user(roleName));
    }

    private User user(RoleName roleName) {
        User user = new User("EMP001", "employee@learninghub.local", "Employee", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        LearnTechnologyService learnTechnologyService(
                LearnTechnologyRepository technologyRepository,
                LearnTechnologyProjectLinkRepository projectLinkRepository,
                ProjectRepository projectRepository,
                UserRepository userRepository,
                LearnTechnologyMapper mapper
        ) {
            return new LearnTechnologyService(
                    technologyRepository,
                    projectLinkRepository,
                    projectRepository,
                    userRepository,
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
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean LearnTechnologyMapper learnTechnologyMapper() { return new LearnTechnologyMapper(); }
    }
}
