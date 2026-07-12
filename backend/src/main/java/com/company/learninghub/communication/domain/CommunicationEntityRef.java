package com.company.learninghub.communication.domain;

import java.util.UUID;

public record CommunicationEntityRef(
        String entityType,
        UUID entityId,
        String actionPath
) {
}
