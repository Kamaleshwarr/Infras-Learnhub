package com.company.learninghub.learn.dto;

import java.util.UUID;

public record RelatedTechnologySummary(
        UUID id,
        String name,
        String shortName
) {
}
