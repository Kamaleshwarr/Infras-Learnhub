package com.company.learninghub.learn.dto;

import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TechnologyUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 30)
        String shortName,

        @Size(max = 2000)
        String description,

        @NotNull
        TechnologyCategory category,

        @NotNull
        TechnologyDifficulty difficulty,

        Boolean featured
) {
}
