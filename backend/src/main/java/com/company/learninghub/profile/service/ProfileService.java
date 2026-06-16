package com.company.learninghub.profile.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.auth.service.AuthenticationService;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.profile.config.ProfileProperties;
import com.company.learninghub.profile.dto.AvatarContentResponse;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.dto.ProfileUpdateResponse;
import com.company.learninghub.profile.dto.UpdateProfileRequest;
import com.company.learninghub.profile.mapper.ProfileMapper;
import com.company.learninghub.storage.AvatarStorageService;
import com.company.learninghub.storage.StoredFile;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final AuthenticationService authenticationService;
    private final AvatarStorageService avatarStorageService;
    private final ProfileProperties profileProperties;

    public ProfileService(
            UserRepository userRepository,
            ProfileMapper profileMapper,
            AuthenticationService authenticationService,
            AvatarStorageService avatarStorageService,
            ProfileProperties profileProperties
    ) {
        this.userRepository = userRepository;
        this.profileMapper = profileMapper;
        this.authenticationService = authenticationService;
        this.avatarStorageService = avatarStorageService;
        this.profileProperties = profileProperties;
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

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ProfileResponse uploadAvatar(AuthenticatedUser authenticatedUser, MultipartFile file) {
        validateAvatar(file);
        User user = findUser(authenticatedUser.getId());
        String previousStorageKey = user.getAvatarStorageKey();
        StoredFile storedFile = avatarStorageService.store(user.getId(), file);
        if (StringUtils.hasText(previousStorageKey)) {
            avatarStorageService.deleteQuietly(previousStorageKey);
        }
        applyAvatarMetadata(user, storedFile);
        userRepository.save(user);
        return profileMapper.toResponse(user);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteAvatar(AuthenticatedUser authenticatedUser) {
        User user = findUser(authenticatedUser.getId());
        if (!StringUtils.hasText(user.getAvatarStorageKey())) {
            return;
        }
        avatarStorageService.deleteQuietly(user.getAvatarStorageKey());
        clearAvatarMetadata(user);
        userRepository.save(user);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public AvatarContentResponse getAvatar(AuthenticatedUser authenticatedUser) {
        User user = findUser(authenticatedUser.getId());
        if (!StringUtils.hasText(user.getAvatarStorageKey())) {
            throw new ResourceNotFoundException("Avatar was not found");
        }
        return new AvatarContentResponse(
                avatarStorageService.loadResource(user.getAvatarStorageKey()),
                user.getAvatarContentType(),
                user.getAvatarUpdatedAt()
        );
    }

    private void applyAvatarMetadata(User user, StoredFile storedFile) {
        user.setAvatarStorageProvider(storedFile.storageProvider());
        user.setAvatarStorageKey(storedFile.storageKey());
        user.setAvatarContentType(storedFile.contentType());
        user.setAvatarOriginalFilename(storedFile.originalFilename());
        user.setAvatarFileSizeBytes(storedFile.fileSizeBytes());
        user.setAvatarUpdatedAt(Instant.now());
    }

    private void clearAvatarMetadata(User user) {
        user.setAvatarStorageProvider(null);
        user.setAvatarStorageKey(null);
        user.setAvatarContentType(null);
        user.setAvatarOriginalFilename(null);
        user.setAvatarFileSizeBytes(null);
        user.setAvatarUpdatedAt(null);
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }
        if (file.getSize() > profileProperties.getAvatarMaxSizeBytes()) {
            throw new IllegalArgumentException("Avatar file must be 2 MB or smaller");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Avatar file must be a JPG, JPEG, PNG, or WebP image");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Avatar file must be a JPG, JPEG, PNG, or WebP image");
        }
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
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
