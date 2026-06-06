package com.company.learninghub.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectSubmissionRequest(
        @NotBlank
        @Size(max = 2000)
        String rejectionReason
) {
}

