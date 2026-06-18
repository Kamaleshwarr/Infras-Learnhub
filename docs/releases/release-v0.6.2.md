# Engineering Learning Hub v0.6.2

**Theme:** Certificate Preview, Download & Pending Reviews Dashboard Drilldown  
**Status:** Ready for release  
**Depends on:** v0.6.1 Certificate Workflow UI & Notification E2E Validation (merged PR #29)  
**Classification:** Feature release — admin certificate document access and dashboard navigation

---

## Release overview

v0.6.2 completes the admin certificate review experience by letting reviewers preview and download submitted certificate files before approving or rejecting, and links the Admin Dashboard **Pending Reviews** metric directly to the Certificate Review queue.

**Success criterion (met):** Admin opens Certificate Review → previews or downloads a submitted certificate → approves or rejects with full document context; Admin Dashboard **Pending Reviews** card navigates to `/submissions/review`.

**Merge target:** `cursor/v0.6.2-certificate-review-de8d` → `main`

---

## Features delivered

### Backend — Certificate file streaming API

| Method | Path | Auth | Capability |
|--------|------|------|------------|
| `GET` | `/api/v1/submissions/{submissionId}/certificate?disposition=inline\|attachment` | Admin (any submission); Employee (own only) | Stream stored certificate file |

- `disposition=inline` — preview-friendly (`Content-Disposition: inline`)
- `disposition=attachment` — download (`Content-Disposition: attachment`)
- Authorization matches `GET /submissions/{id}` — unauthorized callers receive 404
- No new Flyway migrations

### Frontend — Admin review document actions (F1–F3)

| Component | Capability |
|-----------|------------|
| `submissionsApi.getCertificateBlob()` | Blob fetch with disposition param |
| `CertificateDocumentMetadata` | Filename, content type, file size display |
| `CertificateFileActions` | Preview and Download buttons |
| `CertificatePreviewDialog` | PDF iframe; PNG/JPEG image preview |
| `AdminReviewTable` | Metadata + actions per pending row |
| `SubmissionReviewSummary` | Same actions inside approve/reject dialogs |

**Scope:** Admin Certificate Review workflow only — employee self-service download from My Submissions is **not** included.

### Frontend — Pending Reviews dashboard drilldown (F4)

| Surface | Behavior |
|---------|----------|
| Admin Dashboard **Pending Reviews** metric | Clickable card → `/submissions/review` (ADMIN role-guarded route) |
| Active Initiatives, Expiring Initiatives, Top Learners | Unchanged — static metrics, no drilldown |
| Employee Dashboard | Unchanged |

`DashboardWidget` extended with optional `href` and `linkAriaLabel`; uses `RouterLink` following existing navigation patterns.

---

## User flow

### Dashboard → Pending Reviews → Certificate Review

1. Admin logs in and lands on the Admin Dashboard (`/`).
2. The **Pending Reviews** metric card shows the count from `GET /api/v1/submissions?status=SUBMITTED&size=1` (`totalElements`).
3. Admin clicks the **Pending Reviews** card.
4. App navigates to `/submissions/review` (ADMIN route guard).
5. Certificate Review queue lists pending submissions with document metadata.
6. Admin clicks **Preview** to open `CertificatePreviewDialog` (PDF/PNG/JPEG) or **Download** to save the file.
7. Admin approves or rejects from the row or dialog with full document context.

### Notification path (unchanged from v0.6.1)

`CERTIFICATE_SUBMITTED` notifications still link to `/submissions/review` via `actionPath`.

---

## Validation checklist

| # | Scenario | Expected result | Status |
|---|----------|-----------------|--------|
| 1 | Employee submits PDF certificate | Submission appears in admin review queue | **Pass** (regression on v0.6.1 flow) |
| 2 | Employee submits PNG certificate | Preview renders image in dialog | **Pass** |
| 3 | Admin previews PDF in review table | Inline PDF in dialog iframe | **Pass** |
| 4 | Admin downloads certificate from review table | File saved with original filename | **Pass** |
| 5 | Admin previews inside approve dialog | Preview/Download available in `SubmissionReviewSummary` | **Pass** |
| 6 | Admin rejects after preview | Reject workflow completes; status updates | **Pass** |
| 7 | Admin clicks Pending Reviews on dashboard | Navigates to `/submissions/review` | **Pass** |
| 8 | Active Initiatives metric | Not clickable (no `href`) | **Pass** |
| 9 | Expiring Initiatives metric | Not clickable | **Pass** |
| 10 | Top Learners metric | Not clickable | **Pass** |
| 11 | Employee dashboard metrics | No new drilldowns | **Pass** |
| 12 | CW-D01 employee dashboard fault isolation | Primary widgets render when secondary APIs fail | **Pass** (regression) |
| 13 | CW-D02 admin dashboard fault isolation | Primary metrics render when secondary APIs fail | **Pass** (regression) |
| 14 | Notification E2E (submit → approve) | `CERTIFICATE_SUBMITTED` → review → `CERTIFICATE_APPROVED` | **Pass** (regression) |
| 15 | Unauthorized certificate access | Employee cannot fetch another user's certificate (404) | **Pass** (backend tests) |

---

## Test coverage summary

| Area | Command | Result (v0.6.2) |
|------|---------|-----------------|
| Frontend unit/integration | `cd frontend && npm test` | **231 tests** — **54 files** — pass |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend (full suite) | `mvn -f backend/pom.xml test` | **224 run**; **12 skipped** (Testcontainers); **4 pre-existing failures** (not introduced by v0.6.2) |
| Backend (certificate-scoped) | `mvn test -Dtest='Certificate*Test,CertificateFileStorageServiceTest,CertificateContentDispositionTest'` | **34/34 pass** |

### Frontend tests added (v0.6.2)

| Area | Files |
|------|-------|
| Certificate API client | `submissionsApi.test.ts` — `getCertificateBlob` |
| Document display helpers | `certificateDocumentDisplay.test.ts` |
| Preview/Download UI | `CertificateFileActions.test.tsx`, `CertificatePreviewDialog.test.tsx` |
| Admin review integration | `AdminReviewTable.test.tsx` |
| Dashboard drilldown | `DashboardWidget.test.tsx`, `DashboardPage.test.tsx` |

**Delta from v0.6.1 baseline:** +11 tests (+4 files).

### Backend tests added (v0.6.2)

| File | Coverage |
|------|----------|
| `CertificateFileStorageServiceTest` | Load resource from storage |
| `CertificateSubmissionServiceTest` | `getCertificateContent` authz and streaming |
| `CertificateSubmissionMethodSecurityTest` | Method-level security |
| `CertificateSubmissionControllerTest` | HTTP disposition headers, status codes |
| `CertificateContentDispositionTest` | Disposition enum parsing |

**Delta from v0.6.1 baseline:** +14 certificate-scoped tests (34 total in scope).

### Pre-existing backend failures (not v0.6.2 regressions)

| Test | Issue |
|------|-------|
| `NotificationControllerTest` | MockMvc + Jackson `Instant` serialization mismatch |
| `LearningInitiativeServiceTest.getByIdHidesFutureActiveInitiativeFromEmployee` | Instant offset vs UTC calendar-day visibility |
| `UserManagementServiceTest.activateDeactivateAndResetPasswordWork` | Incomplete Mockito stubbing → NPE |

Verified on `origin/main` without v0.6.2 changes.

---

## Key commits

| Commit | Description |
|--------|-------------|
| `24c3a0a` | Backend certificate preview and download API (B1–B4) |
| `f35695e` | Admin certificate preview, download, and metadata UI (F1–F3) |
| `7a1de44` | Pending Reviews dashboard drilldown (F4) |
| *(F5–F6)* | Documentation, validation checklist, release notes |

---

## Known limitations

| Item | Notes |
|------|-------|
| Employee self-service certificate download | Deferred — My Submissions has no Preview/Download in v0.6.2 |
| Preview-before-approve enforcement | Not required — preview is optional |
| Audit logging for certificate access | Not implemented |
| Dashboard drilldowns (other metrics) | Active Initiatives, Expiring Initiatives, Top Learners remain static |
| Initiative list/detail full UI | Not in scope |
| Email notifications | Deferred |
| Notification badge refresh | Polls every 60s + window focus; not real-time push |

---

## Deferred to future releases

- Employee certificate download from My Submissions
- Dashboard drilldowns for Active/Expiring Initiatives and Top Learners
- Dashboard status chips and filtering
- Certificate access audit logging
- Email notification channel
- Initiative / leaderboard / study materials / projects full UI
- Global Search

---

## Upgrade / deployment considerations

- **No Flyway migrations** — database schema unchanged from v0.6.1
- **Backend deploy:** standard Spring Boot artifact; certificate files served from existing local storage path
- **Frontend deploy:** rebuild and deploy static assets
- **Rollback:** revert application deploy; no schema rollback required
- **Smoke test after deploy:** admin dashboard → Pending Reviews click → preview one certificate → approve

---

## Related documentation

- Defect & validation history: `docs/testing-and-defect-history.md`
- Roadmap: `docs/project-roadmap.md`
- Prior release: `docs/releases/release-v0.6.1.md`
- Architecture: `docs/backend-architecture-and-roadmap.md`
