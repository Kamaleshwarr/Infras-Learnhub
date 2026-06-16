package com.company.learninghub.auth.service;

import com.company.learninghub.auth.dto.LoginRequest;
import com.company.learninghub.auth.dto.LoginResponse;
import com.company.learninghub.auth.dto.UserSummaryResponse;
import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.security.JwtService;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);
        String token = jwtService.generateToken(authenticatedUser);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.expirationSeconds(),
                toUserSummary(authenticatedUser)
        );
    }

    public String issueAccessToken(User user) {
        return jwtService.generateToken(AuthenticatedUser.from(user));
    }

    public UserSummaryResponse toUserSummary(AuthenticatedUser user) {
        Set<String> roles = user.getRoleNames().stream()
                .map(RoleName::name)
                .collect(Collectors.toUnmodifiableSet());
        return new UserSummaryResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail(),
                roles,
                user.isMustChangePassword()
        );
    }
}

