package com.company.learninghub.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StudyMaterialStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeWritesStudyMaterialUnderConfiguredRoot() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        StudyMaterialStorageService storageService = new StudyMaterialStorageService(storageProperties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../slides.PPTX",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "slides-content".getBytes()
        );

        StoredFile storedFile = storageService.store(file);

        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();
        assertThat(storedFile.storageProvider()).isEqualTo("LOCAL");
        assertThat(storedFile.storageKey()).startsWith("study-materials/");
        assertThat(storedFile.storageKey()).endsWith(".pptx");
        assertThat(storedFile.originalFilename()).isEqualTo("slides.PPTX");
        assertThat(Files.readString(storedPath)).isEqualTo("slides-content");
        assertThat(storedPath).startsWith(tempDir);
    }

    @Test
    void loadAndDeleteStoredStudyMaterial() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        StudyMaterialStorageService storageService = new StudyMaterialStorageService(storageProperties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "guide.pdf",
                "application/pdf",
                "guide-content".getBytes()
        );
        StoredFile storedFile = storageService.store(file);
        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();

        Resource resource = storageService.loadAsResource(storedFile.storageKey());

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("guide-content");

        storageService.deleteQuietly(storedFile.storageKey());

        assertThat(storedPath).doesNotExist();
    }
}

