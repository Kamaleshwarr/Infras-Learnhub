package com.company.learninghub.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectKnowledgeStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeWritesProjectKnowledgeFileUnderConfiguredRoot() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        ProjectKnowledgeStorageService storageService = new ProjectKnowledgeStorageService(storageProperties);
        MockMultipartFile file = new MockMultipartFile("file", "../architecture.PDF", "application/pdf", "architecture".getBytes());

        StoredFile storedFile = storageService.store(file);

        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();
        assertThat(storedFile.storageProvider()).isEqualTo("LOCAL");
        assertThat(storedFile.storageKey()).startsWith("project-knowledge/");
        assertThat(storedFile.storageKey()).endsWith(".pdf");
        assertThat(storedFile.originalFilename()).isEqualTo("architecture.PDF");
        assertThat(Files.readString(storedPath)).isEqualTo("architecture");
        assertThat(storedPath).startsWith(tempDir);
    }

    @Test
    void loadAndDeleteStoredProjectKnowledgeFile() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        ProjectKnowledgeStorageService storageService = new ProjectKnowledgeStorageService(storageProperties);
        MockMultipartFile file = new MockMultipartFile("file", "release-notes.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "notes".getBytes());
        StoredFile storedFile = storageService.store(file);
        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();

        Resource resource = storageService.loadAsResource(storedFile.storageKey());

        assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("notes");

        storageService.deleteQuietly(storedFile.storageKey());

        assertThat(storedPath).doesNotExist();
    }
}

