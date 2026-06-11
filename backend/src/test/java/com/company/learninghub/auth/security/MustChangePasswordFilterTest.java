package com.company.learninghub.auth.security;

import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MustChangePasswordFilterTest {

    private final MustChangePasswordFilter filter = new MustChangePasswordFilter(
            new ObjectMapper().registerModule(new JavaTimeModule())
    );

    @Test
    void blocksProtectedEndpointWhenPasswordChangeRequired() throws Exception {
        User user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.setMustChangePassword(true);
        user.assignRole(new Role(RoleName.EMPLOYEE));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(AuthenticatedUser.from(user), null, AuthenticatedUser.from(user).getAuthorities())
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/initiatives");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Password change required");
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsChangePasswordEndpointWhenPasswordChangeRequired() throws Exception {
        User user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.setMustChangePassword(true);
        user.assignRole(new Role(RoleName.EMPLOYEE));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(AuthenticatedUser.from(user), null, AuthenticatedUser.from(user).getAuthorities())
        );

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/change-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        SecurityContextHolder.clearContext();
    }
}
