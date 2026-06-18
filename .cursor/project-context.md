# Learning Hub Project Context

## Project Identity

- **Project Name:** Learning Hub
- **Repository:** https://github.com/Kamaleshwarr/Infras-Learnhub.git
- **Purpose:** Enterprise internal learning platform for initiatives, certification submissions, study materials, project knowledge, leaderboards, and administration workflows.

## Technology Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- PostgreSQL
- Flyway
- Spring Data JPA
- Docker / Docker Compose
- Swagger / OpenAPI

### Frontend

- React
- TypeScript
- Material UI
- React Router
- Axios
- Context API
- Vitest / Testing Library for frontend tests

## Roles

- `ADMIN`
- `EMPLOYEE`

## Current Release

**v0.6.2** — Certificate Preview, Download & Pending Reviews Drilldown (ready for release)

Release notes: `docs/releases/release-v0.6.2.md`  
Prior release: `docs/releases/release-v0.6.1.md`  
Workstream summary: `docs/releases/notification-infrastructure-final-summary.md`  
Roadmap: `docs/project-roadmap.md`

### v0.6.2 Highlights

- Backend `GET /api/v1/submissions/{id}/certificate?disposition=inline|attachment`
- Admin certificate preview (PDF iframe, PNG/JPEG image) and download in Certificate Review
- `CertificateFileActions`, `CertificatePreviewDialog`, `CertificateDocumentMetadata`
- Admin Dashboard **Pending Reviews** metric → `/submissions/review` (F4 drilldown only)

### v0.6.1 Highlights

- Submit Certificate — `/submissions/new`
- My Submissions — `/submissions`
- Admin Certificate Review — `/submissions/review` (approve/reject)
- Notification E2E validated — `CERTIFICATE_SUBMITTED` → approve → `CERTIFICATE_APPROVED`
- `CERTIFICATE_SUBMITTED` actionPath → `/submissions/review`
- Dashboard fault isolation — CW-D01 (employee), CW-D02 (admin)
- Dropdown UX — available initiatives first in Submit Certificate

### v0.6 Highlights

- Notification persistence — Flyway `V9__create_notifications.sql`
- Inbox APIs — list, unread-count, mark-read, mark-all-read
- Frontend bell, dropdown, `/notifications` page, badge synchronization
- Certificate backend producers — **E2E validated via application UI in v0.6.1**

### v0.5 Highlights

- Profile View — `GET /api/v1/profile`, `/profile` page, sidebar navigation
- Edit Profile — `PUT /api/v1/profile`, email change JWT refresh
- Change Password Entry — profile page link to `/change-password`
- Avatar Upload / Replace / Delete — multipart upload, local storage, initials fallback

### v0.4 Highlights

- User Management UI Phase 4 — bulk import, template download, parser hardening

### v0.3 Highlights

- User Management UI: list, create, edit, activate, deactivate, reset password
- `mustChangePassword` on `UserResponse` (UM-001)
- Backend self-deactivation guard (UM-005)

### v0.2 Highlights

- Password Management module (backend + frontend)
- JWT hardening (`password_changed_at`, deactivated-user rejection)
- Password reset tokens and email reset flow
- Cursor project governance documentation (`.cursor/`)

## Completed Backend Modules

- Authentication
- Learning Initiatives
- Certificate Submissions
- Leaderboards
- Study Materials Repository
- Project Knowledge Repository
- User Management
- Password Management
- Profile Management
- Notifications (infrastructure — persistence, APIs, certificate backend producers)

## Completed Frontend Modules

1. Frontend foundation
2. Authentication UI
3. Role-aware dashboard foundation
4. Password Management UI
5. User Management UI (Phases 1–4: list, create/edit, activate/deactivate/reset, bulk import)
6. Profile Management UI (Phases 1–4: view, edit, change-password entry, avatar)
7. Notifications UI (bell, dropdown, inbox — consumer only)
8. Certificate Workflow UI — Submit Certificate, My Submissions, Admin Review (v0.6.1)
9. Certificate Review enhancements — Admin preview/download, Pending Reviews dashboard drilldown (v0.6.2)

## Completed Features

### Notifications (v0.6 — Infrastructure)

- Persistent notification inbox (`notifications` table)
- Inbox APIs under `/api/v1/notifications`
- Bell, dropdown, `/notifications` page, badge sync
- Certificate producers wired in `CertificateSubmissionService` — E2E validated v0.6.1

### Password Management (v0.2)

- Change Password
- First Login Password Change Enforcement
- Forgot Password (Email Reset)
- Reset Password
- Email Templates (`forgot-password.html`, `forgot-password.txt`)
- Password Reset Tokens
- JWT Hardening

### Password Management APIs

| Method | Path | Auth | Status |
|--------|------|------|--------|
| `POST` | `/api/v1/auth/change-password` | Bearer JWT | `204` |
| `POST` | `/api/v1/auth/forgot-password` | Public | `202` |
| `POST` | `/api/v1/auth/reset-password` | Public | `204` |

`LoginResponse` and `GET /api/v1/auth/me` expose `mustChangePassword`.

### Password Management Frontend

- `ChangePasswordPage` — `/change-password`
- `ForgotPasswordPage` — `/forgot-password`
- `ResetPasswordPage` — `/reset-password?token=...`
- `MustChangePasswordRoute` — redirects until password is changed

## User Management Features

- User CRUD
- Activate user
- Deactivate user
- Admin Password Reset
- Force Password Change After Reset
- CSV import
- XLS import
- XLSX import
- User import template download

## Pending Features

1. Employee self-service certificate download from My Submissions (deferred from v0.6.2)
2. Dashboard drilldowns for Active/Expiring Initiatives and Top Learners (deferred from v0.6.2)
3. Dashboard status chips / filtering (deferred from v0.6.1)
4. User Management UI backlog (UM-002, UM-003, UM-004, UM-006)
5. Learning domain UI (initiatives, leaderboards, study materials, projects — placeholder pages)
6. Global Search
7. Email notifications (account lifecycle)
8. AI Features

## Current Backend Package Pattern

Backend modules use package-per-feature under:

```text
backend/src/main/java/com/company/learninghub/
```

Typical module structure:

```text
module/
├── controller/
├── domain/
├── dto/
├── mapper/
├── repository/
└── service/
```

## Current Frontend Structure

Frontend code lives under:

```text
frontend/src/
├── api/
├── auth/
├── components/
├── layout/
├── pages/
├── routes/
├── theme/
└── types/
```

## Important Implementation Notes

- Do not recreate identity tables or duplicate `User`, `Role`, `RoleName`, or `UserRole`.
- Use existing `UserRepository` and `RoleRepository` for identity-related work.
- Use Flyway only when schema changes are required.
- Password Management schema is in `V7__password_management.sql` (`must_change_password`, `password_changed_at`, `password_reset_tokens`).
- Profile avatar metadata is in `V8__profile_avatar.sql` (nullable avatar columns on `users`).
- Notifications schema is in `V9__create_notifications.sql` (`notifications` table).
- Reuse `PasswordService` for all password mutations (change, admin reset, email reset).
- Store only hashed reset tokens (SHA-256); never persist raw tokens.
- Use `app.mail.mode=log` for local development (reset URL logged); use `smtp` in production.
- Use Spring Specifications for dynamic filtering/search where nullable JPQL parameters can cause PostgreSQL type inference problems.
- Preserve UTC timestamp handling with `Instant` and `TIMESTAMPTZ`.
- Keep endpoints under `/api/v1`.
- All new APIs require Swagger/OpenAPI documentation.
- All new modules require service and controller tests.
