package com.company.learninghub.storage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class AvatarStorageService {

    private static final String PROVIDER_LOCAL = "LOCAL";
    private static final String AVATAR_DIRECTORY = "avatars";

    private final StorageProperties storageProperties;

    public AvatarStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public StoredFile store(UUID userId, MultipartFile file) {
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = extractExtension(originalFilename);
        String storageKey = AVATAR_DIRECTORY + "/" + userId + "/" + UUID.randomUUID() + extension;
        Path destination = resolveStorageKey(storageKey);

        try {
            Files.createDirectories(destination.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store avatar file", ex);
        }

        return new StoredFile(
                PROVIDER_LOCAL,
                storageKey,
                originalFilename,
                contentType(file),
                file.getSize()
        );
    }

    public Resource loadResource(String storageKey) {
        Path path = resolveStorageKey(storageKey);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Avatar file was not found");
        }
        return new FileSystemResource(path);
    }

    public void deleteQuietly(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }
        try {
            Files.deleteIfExists(resolveStorageKey(storageKey));
        } catch (IOException ignored) {
            // Best-effort cleanup. Metadata persistence remains the source of truth.
        }
    }

    private Path resolveStorageKey(String storageKey) {
        Path root = Path.of(storageProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path destination = root.resolve(storageKey).normalize();
        if (!destination.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return destination;
    }

    private String sanitizeFilename(String filename) {
        String cleaned = StringUtils.cleanPath(filename == null ? "avatar" : filename);
        int lastSeparator = Math.max(cleaned.lastIndexOf('/'), cleaned.lastIndexOf('\\'));
        String sanitized = lastSeparator >= 0 ? cleaned.substring(lastSeparator + 1) : cleaned;
        return sanitized.isBlank() ? "avatar" : sanitized;
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String contentType(MultipartFile file) {
        return StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";
    }
}
