package com.company.learninghub.learn.dto;

import com.company.learninghub.learn.domain.TechnologyStatus;
import jakarta.validation.constraints.Size;

public record TechnologyCurationRequest(
        Boolean featured,
        TechnologyStatus status,
        @Size(max = 2000) String orgNotes
) {
}
