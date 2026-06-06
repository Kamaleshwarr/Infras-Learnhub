package com.company.learninghub.submission.repository;

import com.company.learninghub.submission.domain.CertificateDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificateDocumentRepository extends JpaRepository<CertificateDocument, UUID> {
}

