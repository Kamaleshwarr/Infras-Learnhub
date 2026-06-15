package com.company.learninghub.user.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.dto.CreateUserRequest;
import com.company.learninghub.user.dto.ResetPasswordRequest;
import com.company.learninghub.user.dto.UpdateUserRequest;
import com.company.learninghub.user.dto.UserImportResponse;
import com.company.learninghub.user.dto.UserResponse;
import com.company.learninghub.user.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserManagementControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserManagementService userManagementService;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userManagementService = mock(UserManagementService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserManagementController(userManagementService))
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver()
                )
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void listUsersReturnsPagedResponse() throws Exception {
        UserResponse user = userResponse();
        when(userManagementService.listUsers(eq("EMP"), eq(null), eq(null), eq(RoleName.EMPLOYEE), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users")
                        .queryParam("employeeId", "EMP")
                        .queryParam("role", "EMPLOYEE")
                        .queryParam("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value("EMP002"))
                .andExpect(jsonPath("$.content[0].mustChangePassword").value(false));
    }

    @Test
    void getUserReturnsSingleUser() throws Exception {
        UserResponse user = userResponse();
        when(userManagementService.getUser(user.id())).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@company.com"));
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        UserResponse user = userResponse();
        CreateUserRequest request = new CreateUserRequest("EMP002", "John Doe", "john.doe@company.com", RoleName.EMPLOYEE, "Temp@123");
        when(userManagementService.createUser(request)).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/" + user.id()))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    void createUserRejectsInvalidRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest("", "", "not-email", RoleName.EMPLOYEE, "short");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateActivateDeactivateAndResetPasswordDelegateToService() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserRequest update = new UpdateUserRequest("John Doe", "john.doe@company.com", RoleName.ADMIN);
        when(userManagementService.updateUser(id, update)).thenReturn(userResponse());
        when(userManagementService.activateUser(id)).thenReturn(userResponse());
        when(userManagementService.deactivateUser(eq(id), any())).thenReturn(userResponse());

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/users/{id}/activate", id))
                .andExpect(status().isOk());

        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(adminUser());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        authenticatedUser,
                        null,
                        authenticatedUser.getAuthorities()
                )
        );

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", id))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/users/{id}/reset-password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest("NewTemp@123"))))
                .andExpect(status().isNoContent());

        verify(userManagementService).resetPassword(id, "NewTemp@123");
    }

    @Test
    void importUsersReturnsSummary() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", "EMP002,John Doe,john@company.com,EMPLOYEE".getBytes());
        when(userManagementService.importUsers(any()))
                .thenReturn(new UserImportResponse(1, 1, 0, List.of()));

        mockMvc.perform(multipart("/api/v1/users/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.imported").value(1));
    }

    @Test
    void templateDownloadReturnsCsv() throws Exception {
        when(userManagementService.generateTemplate()).thenReturn("Employee ID,Full Name,Email,Role\n".getBytes());

        mockMvc.perform(get("/api/v1/users/import/template"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("Employee ID,Full Name,Email,Role\n"));
    }

    private User adminUser() {
        User user = new User("EMP001", "admin@example.com", "Admin User", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(RoleName.ADMIN));
        return user;
    }

    private UserResponse userResponse() {
        return new UserResponse(
                UUID.randomUUID(),
                "EMP002",
                "John Doe",
                "john.doe@company.com",
                RoleName.EMPLOYEE,
                true,
                false,
                Instant.parse("2026-06-10T00:00:00Z"),
                Instant.parse("2026-06-10T00:00:00Z")
        );
    }
}

