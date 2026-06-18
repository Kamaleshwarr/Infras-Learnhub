package com.company.learninghub.submission.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificateContentDispositionTest {

    @Test
    void fromQueryValueDefaultsToAttachment() {
        assertThat(CertificateContentDisposition.fromQueryValue(null)).isEqualTo(CertificateContentDisposition.ATTACHMENT);
        assertThat(CertificateContentDisposition.fromQueryValue("")).isEqualTo(CertificateContentDisposition.ATTACHMENT);
        assertThat(CertificateContentDisposition.fromQueryValue("attachment")).isEqualTo(CertificateContentDisposition.ATTACHMENT);
    }

    @Test
    void fromQueryValueAcceptsInline() {
        assertThat(CertificateContentDisposition.fromQueryValue("inline")).isEqualTo(CertificateContentDisposition.INLINE);
        assertThat(CertificateContentDisposition.fromQueryValue(" INLINE ")).isEqualTo(CertificateContentDisposition.INLINE);
    }

    @Test
    void fromQueryValueRejectsInvalidValues() {
        assertThatThrownBy(() -> CertificateContentDisposition.fromQueryValue("preview"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("disposition must be inline or attachment");
    }
}
