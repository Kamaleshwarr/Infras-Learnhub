package com.company.learninghub.projectknowledge.dto;

import java.util.UUID;

public record ProjectUserResponse(UUID id, String employeeId, String fullName, String email) {
}

