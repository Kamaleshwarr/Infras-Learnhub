package com.company.learninghub.learn.dto;

import java.util.List;

public record RoadmapResourceResponse(
        String slug,
        String title,
        String url,
        String type,
        String provider,
        String freePaid
) {
}
