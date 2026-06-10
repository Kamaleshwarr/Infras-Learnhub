package com.company.learninghub.user.service;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserManagementService {

    private static final String TEMPLATE = "Employee ID,Full Name,Email,Role\n";
    private static final String DEFAULT_IMPORT_PASSWORD = "Temp@12345";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(
            String employeeId,
            String fullName,
            String email,
            RoleName role,
            Boolean active,
            Pageable pageable
    ) {
        Specification<User> specification = Specification
                .where(containsIgnoreCase("employeeId", normalizeSearch(employeeId)))
                .and(containsIgnoreCase("fullName", normalizeSearch(fullName)))
                .and(containsIgnoreCase("email", normalizeSearch(email)))
                .and(hasRole(role))
                .and(hasActive(active));
        return userRepository.findAll(specification, normalizePageable(pageable))
                .map(this::toResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        return toResponse(findUser(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String employeeId = normalizeRequired(request.employeeId(), "Employee ID is required");
        String email = normalizeRequired(request.email(), "Email is required").toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new IllegalArgumentException("Employee ID already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(
                employeeId,
                email,
                normalizeRequired(request.fullName(), "Full name is required"),
                passwordEncoder.encode(request.password())
        );
        user.replaceRole(findRole(request.role()));
        return toResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUser(id);
        String email = normalizeRequired(request.email(), "Email is required").toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        user.setFullName(normalizeRequired(request.fullName(), "Full name is required"));
        user.setEmail(email);
        user.replaceRole(findRole(request.role()));
        return toResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse activateUser(UUID id) {
        User user = findUser(id);
        user.setActive(true);
        return toResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse deactivateUser(UUID id) {
        User user = findUser(id);
        user.setActive(false);
        return toResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void resetPassword(UUID id, String password) {
        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(password));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserImportResponse importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Import file is required");
        }

        List<ImportRow> rows = parseImportRows(file);
        List<String> errors = new ArrayList<>();
        int imported = 0;
        Set<String> seenEmployeeIds = new HashSet<>();
        Set<String> seenEmails = new HashSet<>();

        for (ImportRow row : rows) {
            String employeeId = normalizeSearch(row.employeeId());
            String fullName = normalizeSearch(row.fullName());
            String email = normalizeSearch(row.email());
            String roleValue = normalizeSearch(row.role());

            if (!StringUtils.hasText(employeeId) || !StringUtils.hasText(fullName)
                    || !StringUtils.hasText(email) || !StringUtils.hasText(roleValue)) {
                errors.add("Row " + row.rowNumber() + " - Missing required values");
                continue;
            }
            if (!seenEmployeeIds.add(employeeId.toLowerCase(Locale.ROOT)) || userRepository.existsByEmployeeId(employeeId)) {
                errors.add("Row " + row.rowNumber() + " - Duplicate employeeId " + employeeId);
                continue;
            }
            if (!seenEmails.add(email.toLowerCase(Locale.ROOT)) || userRepository.existsByEmailIgnoreCase(email)) {
                errors.add("Row " + row.rowNumber() + " - Duplicate email " + email);
                continue;
            }

            RoleName roleName;
            try {
                roleName = RoleName.valueOf(roleValue.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                errors.add("Row " + row.rowNumber() + " - Invalid role " + roleValue);
                continue;
            }

            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role == null) {
                errors.add("Row " + row.rowNumber() + " - Invalid role " + roleValue);
                continue;
            }

            User user = new User(employeeId, email.toLowerCase(Locale.ROOT), fullName, passwordEncoder.encode(DEFAULT_IMPORT_PASSWORD));
            user.replaceRole(role);
            userRepository.save(user);
            imported++;
        }

        return new UserImportResponse(rows.size(), imported, errors.size(), errors);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generateTemplate() {
        return TEMPLATE.getBytes(StandardCharsets.UTF_8);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private Role findRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role " + roleName));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail(),
                primaryRole(user),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private RoleName primaryRole(User user) {
        if (user.roleNames().contains(RoleName.ADMIN)) {
            return RoleName.ADMIN;
        }
        if (user.roleNames().contains(RoleName.EMPLOYEE)) {
            return RoleName.EMPLOYEE;
        }
        return null;
    }

    private Specification<User> containsIgnoreCase(String field, String value) {
        return (root, query, criteriaBuilder) -> !StringUtils.hasText(value)
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), "%" + value.toLowerCase(Locale.ROOT) + "%");
    }

    private Specification<User> hasActive(Boolean active) {
        return (root, query, criteriaBuilder) -> active == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("active"), active);
    }

    private Specification<User> hasRole(RoleName role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<Object, Object> userRole = root.join("userRoles", JoinType.INNER);
            Join<Object, Object> roleJoin = userRole.join("role", JoinType.INNER);
            return criteriaBuilder.equal(roleJoin.get("name"), role);
        };
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(this::toRepositorySortOrder)
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Sort.Order toRepositorySortOrder(Sort.Order order) {
        String property = switch (order.getProperty()) {
            case "id", "employeeId", "fullName", "email", "active", "createdAt", "updatedAt" -> order.getProperty();
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        };
        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeSearch(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<ImportRow> parseImportRows(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            if (filename.endsWith(".csv")) {
                return parseCsvRows(file);
            }
            if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
                return parseWorkbookRows(file);
            }
            throw new IllegalArgumentException("Unsupported import file format");
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read import file", ex);
        }
    }

    private List<ImportRow> parseCsvRows(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                List<String> columns = splitCsvLine(line);
                if (isHeader(columns)) {
                    continue;
                }
                rows.add(new ImportRow(rowNumber, value(columns, 0), value(columns, 1), value(columns, 2), value(columns, 3)));
            }
        }
        return rows;
    }

    private List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private List<ImportRow> parseWorkbookRows(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(file.getBytes()))) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (Row row : sheet) {
                int rowNumber = row.getRowNum() + 1;
                List<String> columns = List.of(
                        cellValue(row.getCell(0), formatter),
                        cellValue(row.getCell(1), formatter),
                        cellValue(row.getCell(2), formatter),
                        cellValue(row.getCell(3), formatter)
                );
                if (isHeader(columns) || columns.stream().allMatch(value -> !StringUtils.hasText(value))) {
                    continue;
                }
                rows.add(new ImportRow(rowNumber, columns.get(0), columns.get(1), columns.get(2), columns.get(3)));
            }
        }
        return rows;
    }

    private String cellValue(Cell cell, DataFormatter formatter) {
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private boolean isHeader(List<String> columns) {
        return columns.size() >= 4
                && "employee id".equalsIgnoreCase(value(columns, 0))
                && "full name".equalsIgnoreCase(value(columns, 1))
                && "email".equalsIgnoreCase(value(columns, 2))
                && "role".equalsIgnoreCase(value(columns, 3));
    }

    private String value(List<String> columns, int index) {
        return index < columns.size() ? columns.get(index).trim() : "";
    }

    private record ImportRow(int rowNumber, String employeeId, String fullName, String email, String role) {
    }
}

