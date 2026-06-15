# Backend Architecture & Roadmap

Last updated: 2026-06-15 (v0.3)

## Stack

- Java 21 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA
- PostgreSQL · Flyway · OpenAPI / Swagger
- Package-per-feature under `com.company.learninghub`

## User Management Module

**Package:** `com.company.learninghub.user`  
**Access:** `ADMIN` only (`@PreAuthorize` on service + controller)

### REST API (`/api/v1/users`)

| Method | Path | Description | UI Phase |
|--------|------|-------------|----------|
| `GET` | `/users` | List with filters, pagination, sort | Phase 1 |
| `GET` | `/users/{id}` | Get user by ID | Phase 2 |
| `POST` | `/users` | Create user | Phase 2 |
| `PUT` | `/users/{id}` | Update name, email, role | Phase 2 |
| `PATCH` | `/users/{id}/activate` | Activate account | Phase 3 |
| `PATCH` | `/users/{id}/deactivate` | Deactivate account | Phase 3 |
| `POST` | `/users/{id}/reset-password` | Admin password reset | Phase 3 |
| `POST` | `/import` | Bulk import (multipart `file`) | **Phase 4 UI** |
| `GET` | `/import/template` | CSV template download | **Phase 4 UI** |

### `UserResponse` fields

```
id, employeeId, fullName, email, role, active, mustChangePassword, createdAtUtc, updatedAtUtc
```

`mustChangePassword` added in v0.3 / UM-001 (Phase 3).

### Business rules (v0.3)

| Rule | Implementation |
|------|----------------|
| Employee ID uniqueness | Case-insensitive (`existsByEmployeeIdIgnoreCase`); stored uppercase |
| Email uniqueness | Case-insensitive |
| Self-deactivation | Rejected with `400` when target ID = authenticated admin (UM-005) |
| Admin reset password | `PasswordService.updatePassword(user, password, true)` → `mustChangePassword = true` |
| Role update | Skipped when unchanged; `replaceRole()` is idempotent |

### Bulk import (backend ready — UI Phase 4)

**Endpoint:** `POST /api/v1/users/import`  
**Formats:** CSV, XLS, XLSX  
**Template:** `GET /api/v1/users/import/template` → `user-import-template.csv`

**Columns:** `Employee ID`, `Full Name`, `Email`, `Role` (`ADMIN` | `EMPLOYEE`)

**Behavior:**

- Each imported user receives default password `Temp@12345` (hashed)
- `mustChangePassword = true` on import
- Row-level validation; partial success supported
- Response: `UserImportResponse { totalRows, imported, failed, errors[] }`

**Parser location:** `UserManagementService.importUsers()` — Apache POI for Excel, buffered reader for CSV.

### Identity integration

- Reuse existing `User`, `Role`, `UserRole` entities — do not duplicate identity tables
- Password mutations delegate to `PasswordService`
- JWT / `AuthenticatedUser` used for self-deactivation guard on deactivate

---

## Roadmap — Backend

### Shipped (v0.3)

- [x] `mustChangePassword` on `UserResponse` (UM-001)
- [x] Self-deactivation guard on deactivate (UM-005)
- [x] Case-insensitive employee ID normalization on create/import

### Phase 4 (frontend only — APIs exist)

No backend changes required for bulk import UI unless validation gaps are found during QA.

### Future backend enhancements

| Item | Description |
|------|-------------|
| UM-003 | Optional `search` query param (OR across employeeId, fullName, email) |
| Import audit | Optional import batch ID / audit log table |
| Self-role guard | Backend enforcement mirroring edit UI (defense-in-depth) |

---

## Testing expectations

- Service tests: `UserManagementServiceTest` (CRUD, import parsers, activate/deactivate/reset, self-deactivation)
- Controller tests: `UserManagementControllerTest` (standalone MockMvc + `AuthenticationPrincipalArgumentResolver`)
- Method security: `UserManagementMethodSecurityTest`
- Import/parser tests required for any parser changes (see `.cursor/coding-standards.md`)
