package com.company.learninghub.projectknowledge.dto;

import com.company.learninghub.projectknowledge.domain.ExternalContactType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectExternalContactRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull ExternalContactType contactType,
        @Size(max = 200) String roleTitle,
        @Size(max = 200) String organization,
        @Email @Size(max = 320) String email,
        @Size(max = 50) String phone,
        @Size(max = 2000) String contactUrl,
        @Size(max = 2000) String notes,
        Boolean primaryContact,
        Integer displayOrder,
        Boolean active
) {
}
