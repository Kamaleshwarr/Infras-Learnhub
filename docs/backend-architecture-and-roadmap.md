# Backend Architecture & Roadmap

Last updated: 2026-06-16 (v0.6.1 — Certificate Workflow UI in progress)

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
| `POST` | `/import` | Bulk import (multipart `file`) | Phase 4 |
| `GET` | `/import/template` | CSV template download | Phase 4 |

### `UserResponse` fields

```
id, employeeId, fullName, email, role, active, mustChangePassword, createdAtUtc, updatedAtUtc
```

`mustChangePassword` added in v0.3 / UM-001 (Phase 3).

### Business rules (v0.3–v0.4)

| Rule | Implementation |
|------|----------------|
| Employee ID uniqueness | Case-insensitive (`existsByEmployeeIdIgnoreCase`); stored uppercase |
| Email uniqueness | Case-insensitive |
| Self-deactivation | Rejected with `400` when target ID = authenticated admin (UM-005) |
| Admin reset password | `PasswordService.updatePassword(user, password, true)` → `mustChangePassword = true` |
| Role update | Skipped when unchanged; `replaceRole()` is idempotent |
| Import create-only | Each row creates a new user; no update-by-import |
| Import default password | `Temp@12345` (hashed); `mustChangePassword = true` |

### Bulk import (v0.4 — UI + parser hardening)

**Endpoint:** `POST /api/v1/users/import`  
**Formats:** CSV, XLS, XLSX  
**Template:** `GET /api/v1/users/import/template` → `user-import-template.csv`

**Columns:** `Employee ID`, `Full Name`, `Email`, `Role` (`ADMIN` | `EMPLOYEE`)

**Behavior:**

- Each imported user receives default password `Temp@12345` (hashed)
- `mustChangePassword = true` on import
- Row-level validation; partial success supported
- Invalid roles (e.g. `Manager`, `Administrator`) rejected per row
- Response: `UserImportResponse { totalRows, imported, failed, errors[] }`

**Parser location:** `UserManagementService.importUsers()` — Apache POI for Excel, buffered reader for CSV.

**Parser skip rules (v0.4 / PR #23):**

| Rule | Method | Applies to |
|------|--------|------------|
| Header row | `isHeader()` | CSV, Excel |
| All-blank row | `isBlankImportRow()` | CSV, Excel |
| `#` comment line | `isImportCommentLine()` | CSV |
| `#` comment row | `isImportCommentRow()` | Excel |
| Cell normalization | `normalizeImportCell()` | All cells (trim NBSP `\u00a0`, zero-width space `\u200b`) |

**Template (v0.4 / PR #24):** header-only — `"Employee ID,Full Name,Email,Role\n"`. Role guidance is UI-only (not embedded in template).

### Identity integration

- Reuse existing `User`, `Role`, `UserRole` entities — do not duplicate identity tables
- Password mutations delegate to `PasswordService`
- JWT / `AuthenticatedUser` used for self-deactivation guard on deactivate

---

## Profile Management Module

**Package:** `com.company.learninghub.profile`  
**Access:** Authenticated users only (`@PreAuthorize("isAuthenticated()")` on service + controller)  
**Scope:** Self-service — users can read and update only their own profile

### REST API (`/api/v1/profile`)

| Method | Path | Description | UI Phase |
|--------|------|-------------|----------|
| `GET` | `/profile` | Get current user profile | Phase 1 |
| `PUT` | `/profile` | Update full name and email | Phase 2 |
| `POST` | `/profile/avatar` | Upload or replace avatar (multipart `file`) | Phase 4 |
| `DELETE` | `/profile/avatar` | Delete avatar (idempotent `204`) | Phase 4 |
| `GET` | `/profile/avatar` | Serve avatar image bytes | Phase 4 |

### `ProfileResponse` fields

```
id, employeeId, fullName, email, role, active, mustChangePassword, avatarUrl, createdAtUtc, updatedAtUtc
```

`avatarUrl` is a relative path to `GET /api/v1/profile/avatar` when an avatar exists; `null` otherwise.

### Business rules (v0.5)

| Rule | Implementation |
|------|----------------|
| Self-only access | All operations use `AuthenticatedUser.getId()` from JWT principal |
| Email uniqueness | Case-insensitive; rejects duplicates belonging to other users |
| Email change JWT | `ProfileUpdateResponse.accessToken` issued when email changes |
| Avatar file types | JPEG, PNG, WebP (content-type + extension allowlist) |
| Avatar max size | `app.profile.avatar-max-size-bytes` (default 2 MB) |
| Avatar delete | Idempotent `204` when no avatar exists |
| Avatar storage | `AvatarStorageService` — local filesystem at `avatars/{userId}/{uuid}.{ext}` |

### Avatar schema (v0.5 / Flyway V8)

Nullable columns on `users`: `avatar_storage_provider`, `avatar_storage_key`, `avatar_content_type`, `avatar_original_filename`, `avatar_file_size_bytes`, `avatar_updated_at`.

Partial index: `idx_users_avatar_updated_at` (where `avatar_storage_key IS NOT NULL`).

### Auth integration

- `UserSummaryResponse.avatarUrl` on login and `GET /api/v1/auth/me`
- `AuthenticationService.resolveAvatarUrl()` builds relative avatar URL
- `AuthenticationService.issueAccessToken()` used for email-change token refresh

**Testing:** `ProfileServiceTest`, `ProfileControllerTest`, `ProfileMethodSecurityTest`

---

## Notifications Module

**Package:** `com.company.learninghub.notification`  
**Access:** Authenticated users only (`@PreAuthorize("isAuthenticated()")` on inbox APIs)  
**Scope:** Self-service inbox — users read only their own notifications  
**v0.6 classification:** **In-App Notification Infrastructure** (foundation) — not notification feature complete

### REST API (`/api/v1/notifications`)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/notifications` | Paginated inbox (`read`, `type` filters) |
| `GET` | `/notifications/unread-count` | Unread count for header badge |
| `PATCH` | `/notifications/{id}/read` | Mark one notification read |
| `PATCH` | `/notifications/read-all` | Mark all notifications read |

### v0.6 certificate producers (backend-only trigger today)

| Type | Producer | Recipients |
|------|----------|------------|
| `CERTIFICATE_SUBMITTED` | `CertificateSubmissionService.submit()` | All active `ADMIN` users |
| `CERTIFICATE_APPROVED` | `CertificateSubmissionService.approve()` | Submitting employee |
| `CERTIFICATE_REJECTED` | `CertificateSubmissionService.reject()` | Submitting employee |

**Limitation (v0.6):** Producers were wired and unit-tested but not triggerable through the application UI until certificate workflow pages shipped.

**v0.6.1 progress:** Submit Certificate, My Submissions, and Admin Review UI shipped; `CERTIFICATE_SUBMITTED` actionPath points to `/submissions/review`. Full notification E2E validation pending Phase 4.

---

## Certificate Submissions Module

**Package:** `com.company.learninghub.submission`  
**Access:** Employees submit; admins list/review all submissions

### REST API (`/api/v1`)

| Method | Path | Description | UI Phase |
|--------|------|-------------|----------|
| `POST` | `/initiatives/{initiativeId}/submissions` | Submit certificate (multipart) | Phase 1 |
| `GET` | `/me/submissions` | List own submissions | Phase 2 |
| `GET` | `/submissions` | List all submissions (admin) | Phase 3 — **shipped** |
| `GET` | `/submissions/{submissionId}` | View submission detail | Phase 3 (optional) |
| `POST` | `/submissions/{submissionId}/approve` | Approve submission | Phase 3 — **shipped** |
| `POST` | `/submissions/{submissionId}/reject` | Reject with reason | Phase 3 — **shipped** |

### Business rules

| Rule | Implementation |
|------|----------------|
| One submission per employee per initiative | `existsByEmployeeIdAndInitiativeId` on submit |
| Initiative visibility | Employee submit only when initiative is `ACTIVE` and within UTC start/expiry window |
| Review state | Only `SUBMITTED` submissions can be approved or rejected |
| Rejection reason | Required, max 2000 characters |
| Certificate file types | PDF, JPEG, PNG; max size from `StorageProperties` |

**Testing:** `CertificateSubmissionServiceTest` (submit, approve, reject, notification producers)

---

`ACCOUNT_CREATED`, `ACCOUNT_ACTIVATED`, `ACCOUNT_DEACTIVATED`, `PASSWORD_RESET_BY_ADMIN` — enum values and schema constraint retained for historical rows; `NotificationFactory` helpers kept for a future email workstream.

**Flyway:** `V9__create_notifications.sql`

**Testing:** `NotificationServiceTest`, `NotificationFactoryTest`, `NotificationControllerTest`, `NotificationMethodSecurityTest`, producer tests in `CertificateSubmissionServiceTest`

---

## Roadmap — Backend

### Shipped (v0.3)

- [x] `mustChangePassword` on `UserResponse` (UM-001)
- [x] Self-deactivation guard on deactivate (UM-005)
- [x] Case-insensitive employee ID normalization on create/import

### Shipped (v0.4)

- [x] Blank row skipping in CSV and Excel import parsers
- [x] Comment-line skipping for user-added `#` rows
- [x] Import cell normalization (NBSP / zero-width space)
- [x] Header-only import template

### Shipped (v0.5)

- [x] Profile module — `GET` / `PUT /api/v1/profile`
- [x] Avatar upload, replace, delete, and serve APIs
- [x] `V8__profile_avatar.sql` — avatar metadata on `users`
- [x] `AvatarStorageService` — local filesystem storage
- [x] Email change JWT refresh via `ProfileUpdateResponse.accessToken`
- [x] `avatarUrl` on auth user summary

### Shipped (v0.6) — Notification Infrastructure

- [x] Notification module — inbox APIs and read-state
- [x] `V9__create_notifications.sql`
- [x] Certificate-workflow producers (`CERTIFICATE_SUBMITTED`, `CERTIFICATE_APPROVED`, `CERTIFICATE_REJECTED`)
- [x] Merged PR #28

**Not feature complete:** E2E producer validation in progress via v0.6.1 certificate workflow UI (PR #29).

### In progress (v0.6.1) — Certificate Workflow UI

- [x] Submit Certificate page — validated
- [x] My Submissions page — validated
- [x] Submit Certificate dropdown UX — available initiatives first
- [x] Admin Review page — approve/reject UI (Phase 3)
- [x] `CERTIFICATE_SUBMITTED` actionPath → `/submissions/review`
- [ ] Notification E2E validation (Phase 4)

### Future backend enhancements

| Item | Description |
|------|-------------|
| UM-003 | Optional `search` query param (OR across employeeId, fullName, email) |
| UM-006 | Downloadable import error report endpoint or export attachment |
| Import audit | Optional import batch ID / audit log table |
| Self-role guard | Backend enforcement mirroring edit UI (defense-in-depth) |
| Email notifications | Account lifecycle and transactional email channel |
| Global Search | Cross-entity search index or unified query layer |

---

## Testing expectations

- Service tests: `UserManagementServiceTest` (CRUD, import parsers, blank rows, activate/deactivate/reset, self-deactivation)
- Controller tests: `UserManagementControllerTest` (standalone MockMvc + `AuthenticationPrincipalArgumentResolver`)
- Method security: `UserManagementMethodSecurityTest`
- Service tests: `ProfileServiceTest` (get/update, email duplicate, avatar CRUD, validation)
- Controller tests: `ProfileControllerTest` (API contracts, multipart upload)
- Method security: `ProfileMethodSecurityTest`
- Import/parser tests required for any parser changes (see `.cursor/coding-standards.md`)
