package com.company.learninghub.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeWritesAvatarUnderUserDirectory() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        AvatarStorageService storageService = new AvatarStorageService(storageProperties);
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../avatar.PNG",
                "image/png",
                "avatar-content".getBytes()
        );

        StoredFile storedFile = storageService.store(userId, file);

        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();
        assertThat(storedFile.storageProvider()).isEqualTo("LOCAL");
        assertThat(storedFile.storageKey()).startsWith("avatars/" + userId + "/");
        assertThat(storedFile.storageKey()).endsWith(".png");
        assertThat(storedFile.originalFilename()).isEqualTo("avatar.PNG");
        assertThat(storedFile.contentType()).isEqualTo("image/png");
        assertThat(Files.readString(storedPath)).isEqualTo("avatar-content");
        assertThat(storedPath).startsWith(tempDir);
    }

    @Test
    void deleteQuietlyRemovesStoredAvatar() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        AvatarStorageService storageService = new AvatarStorageService(storageProperties);
        StoredFile storedFile = storageService.store(
                UUID.randomUUID(),
                new MockMultipartFile("file", "avatar.png", "image/png", "avatar-content".getBytes())
        );
        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();

        storageService.deleteQuietly(storedFile.storageKey());

        assertThat(storedPath).doesNotExist();
    }

    @Test
    void loadResourceRejectsInvalidStorageKey() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        AvatarStorageService storageService = new AvatarStorageService(storageProperties);

        assertThatThrownBy(() -> storageService.loadResource("../escape.png"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid storage key");
    }
}
