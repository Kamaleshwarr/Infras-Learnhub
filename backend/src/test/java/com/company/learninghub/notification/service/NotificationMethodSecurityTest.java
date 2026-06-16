package com.company.learninghub.notification.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.notification.dto.UnreadCountResponse;
import com.company.learninghub.notification.mapper.NotificationMapper;
import com.company.learninghub.notification.repository.NotificationRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = NotificationMethodSecurityTest.TestConfig.class)
class NotificationMethodSecurityTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void unauthenticatedUserCannotListNotifications() {
        assertThatThrownBy(() -> notificationService.list(null, null, Pageable.unpaged(), employeePrincipal()))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanReadUnreadCount() {
        when(notificationRepository.countByUserIdAndReadAtIsNull(employeePrincipal().getId())).thenReturn(2L);

        UnreadCountResponse response = notificationService.unreadCount(employeePrincipal());

        assertThat(response.count()).isEqualTo(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadUnreadCount() {
        when(notificationRepository.countByUserIdAndReadAtIsNull(employeePrincipal().getId())).thenReturn(0L);

        UnreadCountResponse response = notificationService.unreadCount(employeePrincipal());

        assertThat(response.count()).isZero();
    }

    private AuthenticatedUser employeePrincipal() {
        User user = new User("EMP001", "jane.doe@company.com", "Jane Doe", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        return AuthenticatedUser.from(user);
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        NotificationService notificationService(
                NotificationRepository notificationRepository,
                UserRepository userRepository,
                NotificationFactory notificationFactory,
                NotificationMapper notificationMapper
        ) {
            return new NotificationService(
                    notificationRepository,
                    userRepository,
                    notificationFactory,
                    notificationMapper
            );
        }

        @Bean
        NotificationFactory notificationFactory() {
            return new NotificationFactory();
        }

        @Bean
        NotificationMapper notificationMapper() {
            return new NotificationMapper();
        }

        @Bean
        NotificationRepository notificationRepository() {
            return mock(NotificationRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }
}
