# Testing & Defect History

Last updated: 2026-06-18 (v0.6.2 — ready for release)

## Test Baselines

| Area | Command | Baseline (v0.6.2) |
|------|---------|-------------------|
| Frontend | `cd frontend && npm test` | **231 tests** — 54 files |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend | `mvn -f backend/pom.xml test` | **224 tests** run; **12 skipped** (Testcontainers/Docker); **4 pre-existing failures** (not v0.6.2) |
| Backend (certificate-scoped) | `mvn test -Dtest='Certificate*Test,CertificateFileStorageServiceTest,CertificateContentDispositionTest'` | **34/34 pass** |

---

## Certificate Review — Validation History (v0.6.2)

| Phase | Deliverable | Status | Notes |
|-------|-------------|--------|-------|
| B1–B4 | Backend certificate streaming API | **Passed** | `GET /submissions/{id}/certificate?disposition=inline\|attachment` |
| F1–F3 | Admin preview, download, metadata UI | **Passed** | `CertificateFileActions`, `CertificatePreviewDialog`, review table + dialogs |
| F4 | Pending Reviews dashboard drilldown | **Passed** | Admin metric → `/submissions/review`; other metrics unchanged |
| F5–F6 | Docs, validation checklist, release notes | **Passed** | This document + `docs/releases/release-v0.6.2.md` |

### v0.6.2 — Manual validation checklist

| # | Scenario | Status |
|---|----------|--------|
| 1 | Employee submit PDF → admin queue | **Pass** |
| 2 | Employee submit PNG → admin image preview | **Pass** |
| 3 | Admin preview PDF from review table | **Pass** |
| 4 | Admin download from review table | **Pass** |
| 5 | Preview/Download in approve/reject dialogs | **Pass** |
| 6 | Reject after preview | **Pass** |
| 7 | Dashboard Pending Reviews → `/submissions/review` | **Pass** |
| 8–10 | Other admin metrics not clickable | **Pass** |
| 11 | Employee dashboard unchanged | **Pass** |
| 12–13 | CW-D01 / CW-D02 regression | **Pass** |
| 14 | Notification E2E regression | **Pass** |
| 15 | Unauthorized certificate access (404) | **Pass** |

### v0.6.2 — Test coverage added

| Component / area | Tests |
|------------------|-------|
| `submissionsApi.getCertificateBlob` | API client blob + disposition |
| `certificateDocumentDisplay` | Filename, size, type formatting |
| `CertificateFileActions`, `CertificatePreviewDialog` | Preview/download UX |
| `AdminReviewTable` | Certificate actions in review queue |
| `DashboardWidget`, `DashboardPage` | Pending Reviews `href` drilldown |

---

## Certificate Workflow — Validation History (v0.6.1)

| Phase | Deliverable | Status | Notes |
|-------|-------------|--------|-------|
| Phase 0 | API/types/shared route prep | **Passed** | PR #29 |
| Phase 1 | Submit Certificate page | **Passed** | Employee submit E2E validated; initiative visibility was test data (`DRAFT`), not code defect; temporary diagnostics removed before release |
| Phase 2 | My Submissions page | **Passed** | Employee list, status filter, pagination, refresh |
| Dropdown UX | Submit Certificate initiative ordering | **Passed** | Available initiatives first, already-submitted last (disabled); within each group `expiryDateUtc ASC` |
| Phase 3 | Admin Review page | **Passed** | Approve/reject UI; `CERTIFICATE_SUBMITTED` actionPath → `/submissions/review` |
| Phase 4 | Notification E2E + docs | **Passed** | Submit → admin notify → approve → employee notify; badge sync; CW-D01/CW-D02 regression |

**Deferred (not in v0.6.1):** Employee dashboard status chips, filtering, and other dashboard UX refinements.

### v0.6.1 — Test coverage added

| Component / area | Tests |
|------------------|-------|
| `submissionsApi`, `loadAllMySubmissions` | API contracts |
| `SubmitCertificatePage`, `SubmitCertificateForm`, `submissionInitiativeFilter` | Submit flow, dropdown ordering |
| `MySubmissionsPage`, `MySubmissionsTable`, `mySubmissionsListParams` | List, filter, pagination |
| `AdminReviewPage`, `AdminReviewTable`, approve/reject dialogs | Admin review workflow |
| `dashboardApi` | CW-D01/CW-D02 fault isolation |
| `AppRoutes` | Certificate workflow routes and role guards |

---

## Defects — Certificate Workflow (v0.6.1)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| CW-D01 | Employee dashboard shows **"Unable to load dashboard data"**; active initiatives and submissions widgets empty | `getEmployeeDashboardData()` used `Promise.all` across six APIs. Any single failure (e.g. leaderboard, projects, study materials) rejected the entire load; `DashboardPage` catch block set `error` and `data = null`, hiding initiatives/submissions even when `GET /initiatives` and `GET /me/submissions` succeeded | Fix in `dashboardApi.ts`: load initiatives separately and use `Promise.allSettled` for secondary widgets so one failure does not blank the page | **Pass** — revalidated 2026-06-16 |
| CW-D02 | Admin dashboard shows **"Unable to load dashboard data"**; all metric cards `--`; widgets empty while employee dashboard works | `getAdminDashboardData()` still used `Promise.all` across five APIs. Any secondary failure (leaderboard, study materials, projects) rejected the entire admin load even when `GET /initiatives` and `GET /submissions?status=SUBMITTED` succeeded — same fault-isolation gap as CW-D01 on the admin path | Fix in `dashboardApi.ts`: load initiatives and pending submissions as isolated primary sources; use `Promise.allSettled` for secondary widgets; throw only when both primary APIs fail | **Pass** — validated 2026-06-16 |

### CW-D01 — APIs involved

| API | Role in dashboard | Employee dashboard usage |
|-----|-------------------|--------------------------|
| `GET /api/v1/initiatives?size=5&status=ACTIVE&sort=expiryDateUtc,asc` | Active initiatives count + list | Primary |
| `GET /api/v1/me/submissions?size=5&sort=submittedAtUtc,desc` | My submissions count + list | Primary |
| `GET /api/v1/leaderboards/global?size=5&sort=rank,asc` | Leaderboard preview | Secondary |
| `GET /api/v1/leaderboards/me` | My rank metrics | Secondary |
| `GET /api/v1/study-materials/materials?size=5&sort=createdAtUtc,desc` | Recent study materials | Secondary |
| `GET /api/v1/projects?size=5&sort=updatedAtUtc,desc` | Assigned projects | Secondary |

### CW-D01 — Fix summary

1. **Before:** `Promise.all([initiatives, submissions, leaderboard, myRank, materials, projects])` — one HTTP 4xx/5xx or network error failed the whole dashboard.
2. **After:** Initiatives fetched with isolated `catch`; remaining calls use `Promise.allSettled`; successful sections render with empty fallbacks for failed sections.
3. **Follow-up:** Admin dashboard fault isolation addressed in **CW-D02**.

### CW-D02 — APIs involved (admin dashboard)

| API | Role in dashboard | Admin dashboard usage |
|-----|-------------------|----------------------|
| `GET /api/v1/initiatives?size=50&status=ACTIVE&sort=expiryDateUtc,asc` | Active + expiring initiative metrics | Primary |
| `GET /api/v1/submissions?size=1&status=SUBMITTED` | Pending reviews count | Primary |
| `GET /api/v1/leaderboards/global?size=5&sort=rank,asc` | Top learners preview | Secondary |
| `GET /api/v1/study-materials/materials?size=5&sort=createdAtUtc,desc` | Recent study materials | Secondary |
| `GET /api/v1/projects?size=5&sort=updatedAtUtc,desc` | Recent project updates | Secondary |

### CW-D02 — Fix summary

1. **Before:** `Promise.all([initiatives, pendingSubmissions, leaderboard, materials, projects])` — one failure blanked the entire admin dashboard.
2. **After:** Initiatives and pending submissions fetched with isolated `catch`; secondary widgets use `Promise.allSettled`; global error only when **both** primary APIs fail.

### CW-D02 — Validation steps

1. Log in as **admin** with at least one active initiative.
2. Open `/` (Admin Dashboard).
3. Confirm **no** top-level error when initiatives and/or pending-submissions APIs succeed, even if a secondary API fails.
4. Confirm **Active Initiatives** and **Pending Reviews** metrics populate when their APIs succeed.
5. Confirm secondary widgets (Top Learners, Recent Study Materials, Recent Project Updates) show empty states when their APIs fail — not a global error.
6. Network tab: verify a failed secondary call does not set `data = null` or blank primary metrics.

---

### CW-D01 — Validation steps

1. Log in as **employee** with at least one active initiative and one certificate submission.
2. Open `/` (Employee Dashboard).
3. Confirm **no** top-level "Unable to load dashboard data" error when initiatives/submissions APIs succeed.
4. Confirm **Active Initiatives** metric and list show initiative data.
5. Confirm **My Submissions** metric and list show submission data.
6. If a secondary API fails (e.g. disable projects module in test env), confirm initiatives/submissions widgets still render.
7. Network tab: verify failed secondary call does not prevent initiatives/submissions responses from populating the UI.

---

## Test Baselines (v0.6 — archived)

## Notifications — Validation History (v0.6 Infrastructure)

| Area | Status | Notes |
|------|--------|-------|
| Backend startup | **Passed** | NTF-D01 — `@Autowired` on `NotificationService` constructor |
| Notification APIs | **Passed** | List, unread-count, mark-read, mark-all-read |
| Bell and inbox UI | **Passed** | Bell, dropdown, `/notifications`, sidebar |
| Badge synchronization | **Passed** | NTF-D02 — `NotificationProvider` + `refresh()` |
| Account producers removed | **Passed** | Option B — no in-app generation from user management |
| Certificate producers (service layer) | **Passed** | Unit tests in `CertificateSubmissionServiceTest` |
| Certificate producers (application E2E) | **Passed** | v0.6.1 Phase 4 — submit/approve path validated in application UI |
| Scope classification | **Approved** | v0.6 = infrastructure; E2E = v0.6.1 **complete** |

---

## Defects — Notifications (v0.6)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| NTF-D01 | Backend failed to start; `NotificationService` `BeanInstantiationException` | Multiple constructors without `@Autowired` on production entry point | `@Autowired` on public constructor | Backend startup |
| NTF-D02 | Bell badge stale after mark-read on `/notifications` | Isolated unread-count state in bell hook vs page | `NotificationProvider` + `refresh()` after read mutations | Infrastructure validation |

---

## Notifications — Test coverage added

| Component / area | Tests |
|------------------|-------|
| `NotificationServiceTest` | Inbox, unread count, mark read, certificate + factory helpers |
| `NotificationFactoryTest` | Message templates |
| `NotificationControllerTest` | API contracts |
| `NotificationMethodSecurityTest` | `@PreAuthorize` on inbox APIs |
| `CertificateSubmissionServiceTest` | Producer calls on submit/approve/reject |
| `NotificationBell.test.tsx` | Badge render, dropdown, mark-all-read sync |
| `NotificationsPage.test.tsx` | List, filters, mark-all-read, badge sync regression |
| `MustChangePasswordFilterTest` | Notification API allowlist |

---

## Regression Checklist (Notifications — v0.6 Infrastructure)

Validated at v0.6 merge:

1. Backend starts; Flyway `V9` applied
2. `GET /notifications/unread-count` matches bell badge
3. Mark-read on `/notifications` updates badge immediately
4. Mark-read / mark-all-read in dropdown updates badge immediately
5. User create, activate, deactivate, reset-password → **no** new in-app notification
6. Historical account-type rows (if present) still list correctly
7. `mustChangePassword` user can access `/notifications` and bell APIs

## Regression Checklist (Certificate Workflow — v0.6.1 Phase 3)

Run before Phase 3 merge:

1. `/submissions/review` loads for `ADMIN`; blocked for `EMPLOYEE`
2. Pending submissions list shows employee, initiative, submitted date, comments, filename
3. Empty state when no `SUBMITTED` submissions
4. Approve → confirm dialog → success snackbar → row removed from list
5. Reject → reason required → success snackbar → row removed from list
6. Approve/reject API `400` on stale row shows error and refreshes list
7. Employee My Submissions reflects approved/rejected status after admin action
8. Employee receives `CERTIFICATE_APPROVED` / `CERTIFICATE_REJECTED` notification
9. Admin `CERTIFICATE_SUBMITTED` notification `actionPath` navigates to `/submissions/review`
10. Phase 1 Submit Certificate and Phase 2 My Submissions regression pass

---

## Regression Checklist (Notifications — v0.6.1 E2E)

Validated 2026-06-16 (Phase 4):

1. Certificate submit via Submit Certificate page → all active admins notified (`CERTIFICATE_SUBMITTED`)
2. Admin notification `actionPath` navigates to `/submissions/review`
3. Approve via Admin Review page → employee notified (`CERTIFICATE_APPROVED`)
4. Employee notification `actionPath` navigates to `/submissions`
5. Status transition `SUBMITTED` → `APPROVED` on My Submissions
6. Badge and inbox reflect new notifications; mark-read / mark-all-read keeps badge in sync
7. Employee dashboard regression (CW-D01) — pass
8. Admin dashboard regression (CW-D02) — pass

Optional (not required for v0.6.1 sign-off):

9. Reject via Admin Review page → employee notified (`CERTIFICATE_REJECTED`) with reason in message

---

## Profile Management — Validation History

| Phase | PR | Manual validation | Notes |
|-------|-----|-------------------|-------|
| Phase 1 — Profile View | #27 | **Passed** | — |
| Phase 2 — Edit Profile | #27 | **Passed** (after fix) | PM-D01 import collision |
| Phase 3 — Change Password Entry | #27 | **Passed** (after fix) | PM-D02 route redirect |
| Phase 4 — Avatar | #27 | **Passed** | — |

---

## Defects — Profile Management

| ID | Phase | Symptom | Root cause | Fix | Verified |
|----|-------|---------|------------|-----|----------|
| PM-D01 | 2 | App failed to load; `ProfileEditForm` import error | Case-insensitive FS collision: `profileEditForm.ts` vs `ProfileEditForm.tsx` | Renamed utility to `profileFormState.ts` | Phase 2 validation |
| PM-D02 | 3 | Change Password redirected to `/` | `MustChangePasswordRoute` blocked voluntary `/change-password` access | Removed redirect; added route tests | Phase 3 validation |

---

## Profile Management — Test coverage added

| Component / area | Tests |
|------------------|-------|
| `ProfileServiceTest` | Get/update profile, email duplicate, avatar upload/replace/delete, validation |
| `ProfileControllerTest` | API contracts, multipart upload, avatar GET/DELETE |
| `ProfileMethodSecurityTest` | `@PreAuthorize` on profile service |
| `ProfilePage` | Page render, view/edit integration |
| `ProfileEditForm` / `profileFormState` | Dirty guard, validation |
| `ProfileAvatar` | Initials fallback, blob display |
| `ProfileAvatarUpload` | Upload/replace/delete flows |
| `ProfileNavigation` | Sidebar link, route access |
| `MustChangePasswordRoute` | Voluntary change-password navigation |
| `profileApi` | API client contracts |
| `profileInitials` | Initials derivation |

---

## Regression Checklist (Profile Management)

Run before profile phase merge:

1. `/profile` loads for authenticated `ADMIN` and `EMPLOYEE`
2. Profile fields match `GET /api/v1/profile` response
3. Edit profile → dirty guard → snackbar → refresh; email change updates JWT
4. Duplicate email rejected with error message
5. Change Password button navigates to `/change-password` when `mustChangePassword` is false
6. Change Password button hidden when `mustChangePassword` is true
7. Avatar upload (valid image) → displays image; replace overwrites; delete reverts to initials
8. Invalid avatar type or oversize file rejected with error
9. `DELETE /avatar` when no avatar returns success (idempotent)
10. Sidebar **My Profile** link visible and active on `/profile`

---

## User Management UI — Validation History

| Phase | PR | Manual validation | Merge commit |
|-------|-----|-------------------|--------------|
| Phase 1 — List | #18 | Passed | `9aa5a25` |
| Phase 2 — Create/Edit | #19 | Passed (after 2 fix rounds + UX polish) | `d707f72` |
| Phase 3 — Activate/Deactivate/Reset | #20 | **Passed** | `7c308af` |
| Phase 4 — Bulk Import | #22–#24 | **Passed** (after blank-row + template fixes) | `5b694c0` |

---

## Defects — Phase 2 (Create & Edit)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| UM-D01 | Success snackbar not shown after create | Dialog close triggered Snackbar `clickaway` dismiss | `UserManagementSnackbar` ignores clickaway; parent closes dialog before notification | Phase 2 validation |
| UM-D02 | `Emp001` duplicate of `EMP001` allowed | Case-sensitive employee ID check | `existsByEmployeeIdIgnoreCase` + uppercase normalization | Phase 2 validation |
| UM-D03 | Edit User 500 on save | `replaceRole()` re-inserted same `(user_id, role_id)` | Idempotent `replaceRole()`, `hasRoleName()` skip, explicit `save()` | Phase 2 validation |
| UM-D04 | Create Employee ID pre-filled with admin email | Browser autofill on dialog open | Form reset on open, `autoComplete="off"`, unique field `name` attrs | Phase 2 validation |
| UM-D05 | Edit Save enabled with no changes | No dirty-state comparison | `baseline` + `isEditFormDirty()` guard | Phase 2 validation |

---

## Defects — Phase 4 (Bulk Import)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| UM-D06 | Trailing blank CSV rows reported as `Missing required values` | Comma-only lines not skipped by parser | `isBlankImportRow()` in CSV + Excel parsers | PR #23 validation |
| UM-D07 | Preview row count higher than importable rows | Frontend preview did not skip blank rows | `userImportPreview.ts` aligned with backend | PR #23 validation |
| UM-D08 | Template comment row visible as data in Excel | `# Valid role values...` line in CSV template | Header-only template (PR #24); role guidance in dialog only | PR #24 validation |
| UM-D09 | Users enter `Manager` / `Administrator` roles | No inline role hint in import UI | Dialog helper: `Valid role values: ADMIN, EMPLOYEE` | Phase 4 validation |

---

## Phase 3 — No Post-Merge Defects

Phase 3 passed manual validation on first round. No defects logged.

### Phase 3 test coverage added

| Component / area | Tests |
|------------------|-------|
| `ConfirmActionDialog` | Identity render, confirm callback, submitting state |
| `ResetPasswordDialog` | Two-step flow, password submit |
| `UserTable` | Action visibility, self-deactivate guard, reset callback |
| `UserListPage` | Activate, deactivate, reset integration flows |
| `usersApi` | `activate`, `deactivate`, `resetPassword` contracts |
| Backend | `deactivateUserRejectsSelfDeactivation`, `toResponseIncludesMustChangePassword` |

### Phase 4 test coverage added

| Component / area | Tests |
|------------------|-------|
| `BulkImportDialog` | Step flow, preview, upload lockdown, results |
| `UserListToolbar` | Download Template, Import Users actions |
| `userImportPreview` | Blank rows, comment lines, row count |
| `downloadBlob` | Blob download helper |
| `usersApi` | `importUsers`, `downloadImportTemplate` |
| `UserListPage` | Import dialog open, template download, refresh on complete |
| Backend | Blank CSV/Excel rows, invalid roles, header-only template |

---

## Regression Checklist (User Management)

Run before each phase merge:

1. List loads with default sort; URL sync round-trips
2. Create user → snackbar → list refresh preserves filters
3. Edit user → dirty guard → snackbar → refresh
4. Activate / deactivate with confirmation → status chip updates
5. Self-deactivate disabled (UI + API `400`)
6. Reset password two-step → snackbar includes must-change wording
7. Must Change Password column reflects API data
8. Download template → header-only CSV
9. Import valid file → preview count matches import → results + refresh
10. Import file with trailing blank rows → no spurious row errors
11. Invalid role values rejected with row-level message

---

## Known limitations

| Item | Notes |
|------|-------|
| Integration tests | Some backend tests use Testcontainers; require Docker |
| UM-002 / UM-004 | User details view not implemented |
| UM-006 | No downloadable import error report yet |
| Import | Create-only; no update existing users via import |
| Avatar storage | Local filesystem only; no cloud/S3 provider yet |
| Notification E2E | **Passed** — v0.6.1 Phase 4 |
| Admin Review UI | Shipped v0.6.1 |
| Dashboard fault isolation | CW-D01 (employee) and CW-D02 (admin) — **Pass** |
| `CERTIFICATE_SUBMITTED` actionPath | `/submissions/review` (updated in Phase 3) |
