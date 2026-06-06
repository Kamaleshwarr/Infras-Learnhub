package com.company.learninghub.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CertificateFileStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeWritesCertificateUnderConfiguredRoot() throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        CertificateFileStorageService storageService = new CertificateFileStorageService(storageProperties);
        MockMultipartFile file = new MockMultipartFile(
                "certificateFile",
                "../certificate.PDF",
                "application/pdf",
                "certificate-content".getBytes()
        );

        StoredFile storedFile = storageService.store(file);

        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();
        assertThat(storedFile.storageProvider()).isEqualTo("LOCAL");
        assertThat(storedFile.storageKey()).startsWith("certificates/");
        assertThat(storedFile.storageKey()).endsWith(".pdf");
        assertThat(storedFile.originalFilename()).isEqualTo("certificate.PDF");
        assertThat(storedFile.contentType()).isEqualTo("application/pdf");
        assertThat(Files.readString(storedPath)).isEqualTo("certificate-content");
        assertThat(storedPath).startsWith(tempDir);
    }

    @Test
    void deleteQuietlyRemovesStoredFile() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocalRoot(tempDir.toString());
        CertificateFileStorageService storageService = new CertificateFileStorageService(storageProperties);
        MockMultipartFile file = new MockMultipartFile(
                "certificateFile",
                "certificate.pdf",
                "application/pdf",
                "certificate-content".getBytes()
        );
        StoredFile storedFile = storageService.store(file);
        Path storedPath = tempDir.resolve(storedFile.storageKey()).normalize();

        storageService.deleteQuietly(storedFile.storageKey());

        assertThat(storedPath).doesNotExist();
    }
}

