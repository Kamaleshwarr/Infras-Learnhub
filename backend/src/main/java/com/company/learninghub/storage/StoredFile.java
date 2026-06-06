package com.company.learninghub.storage;

public record StoredFile(
        String storageProvider,
        String storageKey,
        String originalFilename,
        String contentType,
        long fileSizeBytes
) {
}

