# Learning Hub Project Context

## Project Identity

- **Project Name:** Learning Hub
- **Repository:** https://github.com/Kamaleshwarr/Infras-Learnhub.git
- **Purpose:** Enterprise internal learning platform for initiatives, certification submissions, study materials, project knowledge, leaderboards, and administration workflows.

## Engineering Standards (Mandatory)

All implementation work must follow **`.cursor/engineering-standards.md`** unless explicitly overridden by the product owner or release plan.

That document defines:

- Engineering principles and implementation standards
- Reuse requirements, architecture consistency, and frontend/backend separation
- Mandatory self-QA (functional, UX, regression, product review, browser compatibility)
- Phase completion criteria and working rules
- Feature numbering conventions (F0, F1, …)

Read `engineering-standards.md` **before starting any feature phase**. Language-level rules remain in `.cursor/coding-standards.md`.

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

**v0.7.1** — Initiative Management (**Complete** — F11–F15 implemented; manual QA passed)  
**v0.8.0** — Learn module (**F16-R shipped**; **F17 blocked**)  
**Philosophy:** *Engineering Learning Hub owns guidance, not knowledge.*  
**Validated (pending merge):** v0.7.0 Initiatives Experience (PR #36); v0.7.1 Initiative Management (PR #43)  
**Shipped baseline:** v0.6.2 — Certificate Preview, Download & Pending Reviews Drilldown (PR #32)

Release notes: `docs/releases/release-v0.7.1.md`  
v0.8.0 catalog specification: `docs/v0.8.0/10-catalog-specification.md`
Roadmap: `docs/project-roadmap.md`

### v0.7.1 Highlights (complete)

| Phase | Status | Deliverable |
|-------|--------|-------------|
| F11 / Phase 0 | **Completed** | Initiative management foundation — types, API, shared form state |
| F12 | **Completed** | Create Initiative dialog + admin list integration |
| F13 | **Completed** | Edit Initiative dialog (list + detail), metadata panel, date/lifecycle business rules |
| F14 | **Completed** | Initiative Lifecycle Management — dedicated actions, confirmations, backend transition enforcement (PR #42) |
| F15 | **Completed** | Delete Initiative — hard delete, submission-count eligibility, blocked/confirm dialogs (PR #43) |

**F13 business rules (finalized, manual QA passed):**

- Create: start date ≥ today (UTC)
- Edit, start unchanged: preserve stored start (even if past)
- Edit, start modified: new start ≥ today (UTC) — no backdating
- Mark Expired: expiry auto-set to today (UTC); banners status-aware (never "Expires in X days" when expired)
- Field limits: title 100, description 2000, reward 500

**F14 lifecycle business rules (finalized, manual QA passed):**

- Status read-only throughout; removed from Create/Edit forms
- Lifecycle transitions only via dedicated actions: Publish, Return to Draft, Mark Expired, Reactivate
- Transition matrix: DRAFT→ACTIVE, ACTIVE→DRAFT, ACTIVE→EXPIRED, EXPIRED→ACTIVE; blocked: DRAFT→EXPIRED, EXPIRED→DRAFT, status via PUT
- Publish: full metadata validation; employees gain access per configured start date
- Return to Draft: allowed with submissions; preserves history; employees hidden; no new submissions while Draft
- Mark Expired: expiry → today (UTC); employees lose access; submissions preserved
- Reactivate: expiry ≥ today (UTC) and ≥ start date; same-cohort reopen only (not recurring yearly programs)
- Backend: `POST /publish`, `/return-to-draft`, `/mark-expired`, `/reactivate`

**F15 delete business rules (finalized, manual QA passed):**

- Delete eligibility depends **only** on certificate submission count (`0` = allowed, `>0` = blocked)
- Hard delete only; no soft delete, cascade delete, clone, or typed confirmation
- Backend validates before `repository.delete()`; returns HTTP **409** when blocked (never 500)
- `DELETE /api/v1/initiatives/{initiativeId}` — admin only; structured INFO log on success
- Frontend: Delete always clickable for admins (list + detail); employees never see delete controls
- On click: check eligibility → blocked informational dialog OR confirmation dialog
- Title reuse allowed after successful delete (unique constraint released)

### v0.7.0 Highlights (PR #36 — pending merge)

- Initiative list — `/initiatives` with search, sort, pagination, admin status filters
- Initiative detail — `/initiatives/:initiativeId` with progress, top learner, submit CTA
- F10 — Submit Certificate pre-selection via `?initiativeId=`
- F2.1 — **Reward / Benefits** column label; **Back to Initiatives** navigation
- Fault-isolated detail loading; no backend or Flyway changes

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

### v0.8.0 F16 — Technology Discovery & Search (shipped)

- Learn module shell: `/learn`, `/learn/technologies`, `/learn/technologies/:id`, admin `/learn/manage`
- Technology entity, CRUD, publish/archive, search/filters, Technology ↔ Project cross-navigation
- Flyway `V12__learn_technologies.sql` with dev seed technologies
- Sidebar: Learn (position 2), Projects retained; My Certifications / Review Submissions renames

### v0.8.0 F16-R — Catalog Foundation Refactor (shipped)

- Catalog-first technology model: `catalog/manifest.json`, `catalog/technologies/wave-1.json` (30 technologies), JSON schema validation
- `CatalogImportService` — idempotent startup import by `slug`, version tracking, org override preservation
- Flyway `V13__learn_catalog_foundation.sql` — slug, catalog columns, `learn_catalog_imports`, `HIDDEN` status
- Admin curation APIs: `PATCH .../curation`, `POST .../publish|hide|archive`, `GET .../manage/catalog/status`
- Admin UI: removed Create/Edit metadata authoring; `TechnologyCurationPanel` (feature, org notes, project links)
- Employee browse/search/detail unchanged (F16 parity)

### v0.8.0 Architecture Revision v2 (APPROVED — FROZEN)

> **Engineering Learning Hub owns guidance, not knowledge.**

- Built-in Technology Catalog (~100 technologies) — metadata only
- Catalog-sourced roadmaps — platform ships seed data
- Admin as curator — publish/feature/hide/org notes/project links
- Catalog spec: directory structure, JSON schemas, import lifecycle

See: `docs/v0.8.0/08-navigation-platform-revision.md`, `docs/v0.8.0/09-implementation-plan-v2.md`, `docs/v0.8.0/10-catalog-specification.md`

**F16-R shipped. F17 may begin after F16-R merge.**

## Completed Backend Modules

- Authentication
- Learning Initiatives
- **Learn — Technologies (F16 + F16-R catalog foundation)**
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
10. Initiatives Experience UI — List, detail, submit integration (v0.7.0 — PR #36 pending merge)
11. Initiative Management UI — Create (F12), Edit (F13), Lifecycle (F14), Delete (F15) (v0.7.1 — complete, PR #43 pending merge)
12. Learn UI — Technology discovery, search, detail, admin management (v0.8.0 F16)

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

1. Initiative leaderboard full page UI (`InitiativeLeaderboardPage` — placeholder)
2. Top 3 learners + leaderboard navigation from detail (future)
3. Rejected submission resubmission workflow (future — backend constraint)
4. Clone Initiative (future enhancement — deferred from F14)
5. Employee self-service certificate download from My Submissions (deferred from v0.6.2)
6. Dashboard drilldowns for Active/Expiring Initiatives and Top Learners (deferred from v0.6.2)
7. Dashboard status chips / filtering (deferred from v0.6.1)
8. User Management UI backlog (UM-002, UM-003, UM-004, UM-006)
9. Study materials and projects full UI surfaces (placeholder pages remain)
10. Global Search
11. Email notifications (account lifecycle)
12. AI Features

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
- Learning initiative date constraint: `V10__relax_learning_initiative_date_constraint.sql` (`expiry_date_utc >= start_date_utc`).
- Learning initiative text limits: `V11__tighten_learning_initiative_text_limits.sql` (title `VARCHAR(100)`).
- Reuse `PasswordService` for all password mutations (change, admin reset, email reset).
- Store only hashed reset tokens (SHA-256); never persist raw tokens.
- Use `app.mail.mode=log` for local development (reset URL logged); use `smtp` in production.
- Use Spring Specifications for dynamic filtering/search where nullable JPQL parameters can cause PostgreSQL type inference problems.
- Preserve UTC timestamp handling with `Instant` and `TIMESTAMPTZ`.
- Keep endpoints under `/api/v1`.
- All new APIs require Swagger/OpenAPI documentation.
- All new modules require service and controller tests.
