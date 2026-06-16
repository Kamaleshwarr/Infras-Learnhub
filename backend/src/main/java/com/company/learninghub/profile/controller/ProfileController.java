package com.company.learninghub.profile.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.profile.dto.AvatarContentResponse;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.dto.ProfileUpdateResponse;
import com.company.learninghub.profile.dto.UpdateProfileRequest;
import com.company.learninghub.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Authenticated user profile APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(summary = "Get the current user's profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(profileService.getProfile(authenticatedUser));
    }

    @PutMapping
    @Operation(summary = "Update the current user's profile")
    public ResponseEntity<ProfileUpdateResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(authenticatedUser, request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace the current user's avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(profileService.uploadAvatar(authenticatedUser, file));
    }

    @DeleteMapping("/avatar")
    @Operation(summary = "Delete the current user's avatar")
    public ResponseEntity<Void> deleteAvatar(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        profileService.deleteAvatar(authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/avatar")
    @Operation(summary = "Get the current user's avatar image")
    public ResponseEntity<org.springframework.core.io.Resource> getAvatar(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        AvatarContentResponse avatar = profileService.getAvatar(authenticatedUser);
        MediaType mediaType = MediaType.parseMediaType(
                avatar.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : avatar.contentType()
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .contentType(mediaType)
                .eTag(avatar.updatedAtUtc() == null ? null : avatar.updatedAtUtc().toString())
                .body(avatar.resource());
    }
}
