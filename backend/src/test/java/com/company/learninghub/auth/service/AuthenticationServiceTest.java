package com.company.learninghub.auth.service;

import com.company.learninghub.auth.dto.LoginRequest;
import com.company.learninghub.auth.dto.LoginResponse;
import com.company.learninghub.auth.security.JwtService;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void loginAuthenticatesCredentialsAndReturnsJwtResponse() {
        LoginRequest request = new LoginRequest("employee@example.com", "ValidPass123");
        User user = new User("E12345", "employee@example.com", "Employee One", "$2a$12$hash");
        user.assignRole(new Role(RoleName.EMPLOYEE));
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                request.email(),
                null,
                List.of()
        );

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        LoginResponse response = authenticationService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);
        assertThat(response.user().email()).isEqualTo("employee@example.com");
        assertThat(response.user().employeeId()).isEqualTo("E12345");
        assertThat(response.user().roles()).containsExactly("EMPLOYEE");

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("employee@example.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("ValidPass123");
    }
}

