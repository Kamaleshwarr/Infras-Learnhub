package com.company.learninghub.user.service;

import com.company.learninghub.auth.service.PasswordService;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.dto.CreateUserRequest;
import com.company.learninghub.user.dto.UpdateUserRequest;
import com.company.learninghub.user.dto.UserImportResponse;
import com.company.learninghub.user.dto.UserResponse;
import com.company.learninghub.user.repository.RoleRepository;
import com.company.learninghub.user.repository.UserRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordService passwordService;

    private UserManagementService service;
    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userRepository, roleRepository, passwordEncoder, passwordService);
        employeeRole = role(RoleName.EMPLOYEE);
        adminRole = role(RoleName.ADMIN);
    }

    @Test
    void createUserValidatesUniquenessAndAssignsRole() {
        when(userRepository.existsByEmployeeIdIgnoreCase("EMP002")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("john.doe@company.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode("Temp@123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });

        UserResponse response = service.createUser(new CreateUserRequest(
                "EMP002",
                "John Doe",
                "JOHN.DOE@company.com",
                RoleName.EMPLOYEE,
                "Temp@123"
        ));

        assertThat(response.employeeId()).isEqualTo("EMP002");
        assertThat(response.email()).isEqualTo("john.doe@company.com");
        assertThat(response.role()).isEqualTo(RoleName.EMPLOYEE);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(userCaptor.getValue().roleNames()).containsExactly(RoleName.EMPLOYEE);
    }

    @Test
    void createUserRejectsDuplicateEmployeeIdAndEmail() {
        when(userRepository.existsByEmployeeIdIgnoreCase("EMP002")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(new CreateUserRequest(
                "EMP002",
                "John Doe",
                "john.doe@company.com",
                RoleName.EMPLOYEE,
                "Temp@123"
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee ID already exists");

        when(userRepository.existsByEmployeeIdIgnoreCase("EMP003")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("jane.doe@company.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(new CreateUserRequest(
                "EMP003",
                "Jane Doe",
                "jane.doe@company.com",
                RoleName.EMPLOYEE,
                "Temp@123"
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void createUserRejectsDuplicateEmployeeIdIgnoringCase() {
        when(userRepository.existsByEmployeeIdIgnoreCase("EMP001")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(new CreateUserRequest(
                "emp001",
                "Jane Doe",
                "jane.doe@company.com",
                RoleName.EMPLOYEE,
                "Temp@12345"
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee ID already exists");
    }

    @Test
    void createUserNormalizesEmployeeIdToUppercase() {
        when(userRepository.existsByEmployeeIdIgnoreCase("EMP010")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("jane.doe@company.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode("Temp@12345")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });

        UserResponse response = service.createUser(new CreateUserRequest(
                "emp010",
                "Jane Doe",
                "jane.doe@company.com",
                RoleName.EMPLOYEE,
                "Temp@12345"
        ));

        assertThat(response.employeeId()).isEqualTo("EMP010");
    }

    @Test
    void updateUserChangesNameEmailAndRole() {
        User user = user("EMP002", "john.doe@company.com", RoleName.EMPLOYEE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("john.updated@company.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = service.updateUser(user.getId(), new UpdateUserRequest(
                "John Updated",
                "john.updated@company.com",
                RoleName.ADMIN
        ));

        assertThat(response.fullName()).isEqualTo("John Updated");
        assertThat(response.email()).isEqualTo("john.updated@company.com");
        assertThat(response.role()).isEqualTo(RoleName.ADMIN);
        assertThat(user.roleNames()).containsExactly(RoleName.ADMIN);
    }

    @Test
    void updateUserKeepsSameRoleWithoutReplacingRoleAssignment() {
        User user = user("EMP002", "john.doe@company.com", RoleName.EMPLOYEE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("john.doe@company.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = service.updateUser(user.getId(), new UpdateUserRequest(
                "John Updated",
                "john.doe@company.com",
                RoleName.EMPLOYEE
        ));

        assertThat(response.fullName()).isEqualTo("John Updated");
        assertThat(response.role()).isEqualTo(RoleName.EMPLOYEE);
        assertThat(user.roleNames()).containsExactly(RoleName.EMPLOYEE);
        assertThat(user.getUserRoles()).hasSize(1);
        verify(roleRepository, never()).findByName(any());
        verify(userRepository).save(user);
    }

    @Test
    void activateDeactivateAndResetPasswordWork() {
        User user = user("EMP002", "john.doe@company.com", RoleName.EMPLOYEE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.deactivateUser(user.getId(), null);
        assertThat(user.isActive()).isFalse();

        service.activateUser(user.getId());
        assertThat(user.isActive()).isTrue();

        service.resetPassword(user.getId(), "NewTemp@123!");
        verify(passwordService).updatePassword(user, "NewTemp@123!", true);
    }

    @Test
    void deactivateUserRejectsSelfDeactivation() {
        User user = user("EMP002", "john.doe@company.com", RoleName.ADMIN);
        com.company.learninghub.auth.security.AuthenticatedUser authenticatedUser =
                com.company.learninghub.auth.security.AuthenticatedUser.from(user);

        assertThatThrownBy(() -> service.deactivateUser(user.getId(), authenticatedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You cannot deactivate your own account");

        assertThat(user.isActive()).isTrue();
    }

    @Test
    void toResponseIncludesMustChangePassword() {
        User user = user("EMP002", "john.doe@company.com", RoleName.EMPLOYEE);
        user.setMustChangePassword(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponse response = service.getUser(user.getId());

        assertThat(response.mustChangePassword()).isTrue();
    }

    @Test
    void getUserThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User was not found");
    }

    @Test
    void listUsersUsesSpecificationsAndSortTranslation() {
        PageRequest pageable = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("createdAtUtc")));
        User user = user("EMP002", "john.doe@company.com", RoleName.EMPLOYEE);
        when(userRepository.findAll(org.mockito.ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(user), invocation.getArgument(1), 1));

        service.listUsers("EMP", "John", "company", RoleName.EMPLOYEE, true, pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(org.mockito.ArgumentMatchers.<Specification<User>>any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void importUsersImportsCsvAndSkipsBadRows() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                        Employee ID,Full Name,Email,Role
                        EMP002,John Doe,john.doe@company.com,EMPLOYEE
                        EMP002,Duplicate Employee,duplicate@company.com,EMPLOYEE
                        EMP003,Duplicate Email,john.doe@company.com,EMPLOYEE
                        EMP004,Invalid Role,invalid.role@company.com,MANAGER
                        EMP005,Jane Doe,jane.doe@company.com,EMPLOYEE
                        """.getBytes()
        );
        when(userRepository.existsByEmployeeIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserImportResponse response = service.importUsers(file);

        assertThat(response.totalRows()).isEqualTo(5);
        assertThat(response.imported()).isEqualTo(2);
        assertThat(response.failed()).isEqualTo(3);
        assertThat(response.errors()).contains(
                "Row 3 - Duplicate employeeId EMP002",
                "Row 4 - Duplicate email john.doe@company.com",
                "Row 5 - Invalid role MANAGER"
        );
    }

    @Test
    void importUsersSupportsXlsAndXlsx() throws Exception {
        when(userRepository.existsByEmployeeIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserImportResponse xlsResponse = service.importUsers(workbookFile("users.xls", new HSSFWorkbook()));
        UserImportResponse xlsxResponse = service.importUsers(workbookFile("users.xlsx", new XSSFWorkbook()));

        assertThat(xlsResponse.imported()).isEqualTo(1);
        assertThat(xlsxResponse.imported()).isEqualTo(1);
    }

    @Test
    void generateTemplateReturnsHeaderRowOnly() {
        assertThat(new String(service.generateTemplate())).isEqualTo("Employee ID,Full Name,Email,Role\n");
    }

    @Test
    void importUsersIgnoresBlankCsvRowsAndCommaOnlyLines() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                        Employee ID,Full Name,Email,Role
                        EMP010,Jane Doe,jane@example.com,EMPLOYEE
                        ,,,
                        , , ,
                        
                        ,,,
                        """.getBytes()
        );
        when(userRepository.existsByEmployeeIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserImportResponse response = service.importUsers(file);

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.imported()).isEqualTo(1);
        assertThat(response.failed()).isZero();
        assertThat(response.errors()).isEmpty();
    }

    @Test
    void importUsersReportsMissingRequiredValuesForPartialRowsOnly() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                        Employee ID,Full Name,Email,Role
                        EMP010,Jane Doe,,EMPLOYEE
                        ,,,
                        """.getBytes()
        );

        UserImportResponse response = service.importUsers(file);

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.imported()).isZero();
        assertThat(response.failed()).isEqualTo(1);
        assertThat(response.errors()).containsExactly("Row 2 - Missing required values");
    }

    @Test
    void importUsersIgnoresWhitespaceOnlyCsvRows() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                        Employee ID,Full Name,Email,Role
                        EMP010,Jane Doe,jane@example.com,EMPLOYEE
                           \u00a0  ,   ,   ,
                        """.getBytes()
        );
        when(userRepository.existsByEmployeeIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserImportResponse response = service.importUsers(file);

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.imported()).isEqualTo(1);
        assertThat(response.failed()).isZero();
    }

    @Test
    void importUsersRejectsInvalidRoleNamesSuchAsManagerAndAdministrator() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                """
                        Employee ID,Full Name,Email,Role
                        EMP010,Jane Doe,jane@example.com,Manager
                        EMP011,John Doe,john@example.com,Administrator
                        """.getBytes()
        );
        when(userRepository.existsByEmployeeIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);

        UserImportResponse response = service.importUsers(file);

        assertThat(response.totalRows()).isEqualTo(2);
        assertThat(response.imported()).isZero();
        assertThat(response.failed()).isEqualTo(2);
        assertThat(response.errors()).containsExactly(
                "Row 2 - Invalid role Manager",
                "Row 3 - Invalid role Administrator"
        );
    }

    @Test
    void importUsersIgnoresEmptyTrailingExcelRows() throws Exception {
        when(userRepository.existsByEmployeeIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Employee ID");
        header.createCell(1).setCellValue("Full Name");
        header.createCell(2).setCellValue("Email");
        header.createCell(3).setCellValue("Role");
        Row data = sheet.createRow(1);
        data.createCell(0).setCellValue("EMP010");
        data.createCell(1).setCellValue("Jane Doe");
        data.createCell(2).setCellValue("jane@example.com");
        data.createCell(3).setCellValue("EMPLOYEE");
        sheet.createRow(4);
        sheet.createRow(5);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.xlsx",
                "application/octet-stream",
                outputStream.toByteArray()
        );

        UserImportResponse response = service.importUsers(file);

        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.imported()).isEqualTo(1);
        assertThat(response.failed()).isZero();
        assertThat(response.errors()).isEmpty();
    }

    private MockMultipartFile workbookFile(String filename, Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Users");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Employee ID");
        header.createCell(1).setCellValue("Full Name");
        header.createCell(2).setCellValue("Email");
        header.createCell(3).setCellValue("Role");
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("EMP" + UUID.randomUUID().toString().substring(0, 6));
        row.createCell(1).setCellValue("Excel User");
        row.createCell(2).setCellValue(UUID.randomUUID() + "@company.com");
        row.createCell(3).setCellValue("EMPLOYEE");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new MockMultipartFile("file", filename, "application/octet-stream", outputStream.toByteArray());
    }

    private User user(String employeeId, String email, RoleName roleName) {
        User user = new User(employeeId, email, "John Doe", "hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.replaceRole(role(roleName));
        return user;
    }

    private Role role(RoleName roleName) {
        Role role = new Role(roleName);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }
}

