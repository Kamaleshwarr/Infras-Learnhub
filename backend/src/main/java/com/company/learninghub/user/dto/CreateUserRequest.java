package com.company.learninghub.user.dto;

import com.company.learninghub.user.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 64)
        String employeeId,

        @NotBlank
        @Size(max = 200)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @NotNull
        RoleName role,

        @NotBlank
        @Size(min = 8, max = 128)
        String password
) {
}

