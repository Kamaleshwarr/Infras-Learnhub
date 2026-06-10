package com.company.learninghub.user.controller;

import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.dto.CreateUserRequest;
import com.company.learninghub.user.dto.ResetPasswordRequest;
import com.company.learninghub.user.dto.UpdateUserRequest;
import com.company.learninghub.user.dto.UserImportResponse;
import com.company.learninghub.user.dto.UserResponse;
import com.company.learninghub.user.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "ADMIN-only user administration APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @Operation(summary = "List users", description = "ADMIN only. Supports pagination, sorting, and filters.")
    public ResponseEntity<PageResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) RoleName role,
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Supported sort fields: employeeId, fullName, email, active, createdAtUtc, updatedAtUtc.")
            @ParameterObject
            @PageableDefault(size = 20, sort = "employeeId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(userManagementService.listUsers(
                employeeId,
                fullName,
                email,
                role,
                active,
                pageable
        )));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "ADMIN only.")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getUser(id));
    }

    @PostMapping
    @Operation(summary = "Create a user", description = "ADMIN only.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userManagementService.createUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "ADMIN only. Updates full name, email, and role.")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a user", description = "ADMIN only.")
    public ResponseEntity<UserResponse> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user", description = "ADMIN only.")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.deactivateUser(id));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset a user's password", description = "ADMIN only.")
    public ResponseEntity<Void> resetPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userManagementService.resetPassword(id, request.password());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk import users", description = "ADMIN only. Supports CSV, XLS, and XLSX files.")
    public ResponseEntity<UserImportResponse> importUsers(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(userManagementService.importUsers(file));
    }

    @GetMapping("/import/template")
    @Operation(summary = "Download user import CSV template", description = "ADMIN only.")
    public ResponseEntity<ByteArrayResource> downloadImportTemplate() {
        byte[] template = userManagementService.generateTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user-import-template.csv\"")
                .body(new ByteArrayResource(template));
    }
}

