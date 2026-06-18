package com.company.learninghub.submission.dto;

import java.util.Locale;

public enum CertificateContentDisposition {
    INLINE,
    ATTACHMENT;

    public static CertificateContentDisposition fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return ATTACHMENT;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "inline" -> INLINE;
            case "attachment" -> ATTACHMENT;
            default -> throw new IllegalArgumentException("disposition must be inline or attachment");
        };
    }
}
