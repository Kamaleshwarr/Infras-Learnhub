package com.company.learninghub.profile.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.service.AuthenticationService;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.dto.ProfileUpdateResponse;
import com.company.learninghub.profile.dto.UpdateProfileRequest;
import com.company.learninghub.profile.mapper.ProfileMapper;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final AuthenticationService authenticationService;

    public ProfileService(
            UserRepository userRepository,
            ProfileMapper profileMapper,
            AuthenticationService authenticationService
    ) {
        this.userRepository = userRepository;
        this.profileMapper = profileMapper;
        this.authenticationService = authenticationService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(AuthenticatedUser authenticatedUser) {
        return profileMapper.toResponse(findUser(authenticatedUser.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ProfileUpdateResponse updateProfile(AuthenticatedUser authenticatedUser, UpdateProfileRequest request) {
        User user = findUser(authenticatedUser.getId());
        String email = normalizeRequired(request.email(), "Email is required").toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        String previousEmail = user.getEmail();
        user.setFullName(normalizeRequired(request.fullName(), "Full name is required"));
        user.setEmail(email);
        userRepository.save(user);

        String accessToken = null;
        if (!previousEmail.equalsIgnoreCase(email)) {
            accessToken = authenticationService.issueAccessToken(user);
        }

        return new ProfileUpdateResponse(profileMapper.toResponse(user), accessToken);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
