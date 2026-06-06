package com.company.learninghub.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class StudyMaterialStorageService {

    private static final String PROVIDER_LOCAL = "LOCAL";
    private static final String STUDY_MATERIAL_DIRECTORY = "study-materials";

    private final StorageProperties storageProperties;

    public StudyMaterialStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public StoredFile store(MultipartFile file) {
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = extractExtension(originalFilename);
        String storageKey = STUDY_MATERIAL_DIRECTORY + "/" + UUID.randomUUID() + extension;
        Path destination = resolveStorageKey(storageKey);

        try {
            Files.createDirectories(destination.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store study material file", ex);
        }

        return new StoredFile(
                PROVIDER_LOCAL,
                storageKey,
                originalFilename,
                contentType(file),
                file.getSize()
        );
    }

    public Resource loadAsResource(String storageKey) {
        try {
            Path file = resolveStorageKey(storageKey);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Study material file is not readable");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Study material file is not readable", ex);
        }
    }

    public void deleteQuietly(String storageKey) {
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
        String cleaned = StringUtils.cleanPath(filename == null ? "study-material" : filename);
        int lastSeparator = Math.max(cleaned.lastIndexOf('/'), cleaned.lastIndexOf('\\'));
        String sanitized = lastSeparator >= 0 ? cleaned.substring(lastSeparator + 1) : cleaned;
        return sanitized.isBlank() ? "study-material" : sanitized;
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

