# Engineering Learning Hub v0.6.1

**Theme:** Certificate Workflow UI & Notification E2E Validation  
**Status:** In progress (PR #29)  
**Depends on:** v0.6 In-App Notification Infrastructure (merged PR #28)

---

## Purpose

Close the gap between notification **infrastructure** (v0.6) and notification **feature completeness** by delivering the minimum certificate workflow UI required to trigger and validate producers end-to-end through the application.

**Success criterion:** At least one full path verified in the app — employee submits certificate → admin receives `CERTIFICATE_SUBMITTED` in bell/inbox → admin approves → employee receives `CERTIFICATE_APPROVED`.

---

## Progress

| Phase | Deliverable | Status |
|-------|-------------|--------|
| Phase 0 | API/types/shared route prep | **Shipped** |
| Phase 1 | Submit Certificate page | **Validated** |
| Phase 2 | My Submissions page | **Validated** |
| Dropdown UX | Available-first initiative ordering | **Validated** |
| Phase 3 | Admin Review page + approve/reject UI | **Shipped** |
| Phase 4 | Notification E2E validation + docs | Not started |

**Deferred from v0.6.1:** Employee dashboard status chips, filtering, Pending Reviews metric link, and other dashboard UX refinements.

---

## Shipped — Phase 0

- `submissionsApi` — list, submit, approve, reject
- `SubmissionStatusChip`, `submissionMessages`
- Route `/submissions/review` with placeholder `AdminReviewPage`
- Tests for API client and shared components

---

## Shipped & validated — Phase 1 (Submit Certificate)

**Route:** `/submissions/new` (employee-only)

- Select active initiative from `GET /initiatives?status=ACTIVE`
- Upload certificate file (PDF, JPEG, PNG)
- Optional comments
- Submit via `POST /initiatives/{initiativeId}/submissions` (multipart)
- Success redirect to My Submissions with snackbar
- Initiative dropdown: available initiatives first, already-submitted last (disabled), sorted by `expiryDateUtc ASC` within each group

**Backend:** Employee UTC calendar-day visibility fix for initiatives; no new submission APIs.

**Investigation note:** Phase 1 initiative visibility issue was traced to test data (`DRAFT` status), not a code defect. Temporary diagnostics used during investigation were removed before release.

---

## Shipped & validated — Phase 2 (My Submissions)

**Route:** `/submissions` (employee-only)

- List via `GET /me/submissions` with pagination
- Status filter tabs: All, Submitted, Approved, Rejected
- URL query sync for page, size, status
- Refresh button and link to Submit Certificate
- Redirect snackbar from successful submit

---

## Shipped — Phase 3 (Admin Review)

**Route:** `/submissions/review` (admin-only)

- List pending submissions via `GET /submissions?status=SUBMITTED` with pagination
- Table: employee, initiative, submitted date, comments, certificate filename
- **Approve** — confirm dialog with submission summary; `POST /submissions/{id}/approve`
- **Reject** — reason dialog (required, max 2000 chars); `POST /submissions/{id}/reject`
- Success snackbars; list refresh after action; error handling for stale rows
- Refresh button

**Backend change:** `NotificationFactory.certificateSubmitted()` `actionPath` updated from `/` to `/submissions/review`.

**New frontend components:**

- `AdminReviewPage`, `AdminReviewTable`
- `ApproveSubmissionDialog`, `RejectSubmissionDialog`, `SubmissionReviewSummary`
- `adminReviewListParams`

**Deferred:** Admin dashboard Pending Reviews metric link to review page.

---

## Planned — Phase 4 (Notification E2E)

Manual validation script (application UI only):

| Step | Actor | Expected |
|------|-------|----------|
| 1 | Employee | Submit certificate on Submit Certificate page |
| 2 | Admin | Bell badge increments; `CERTIFICATE_SUBMITTED` in dropdown and `/notifications` |
| 3 | Admin | Notification click navigates to `/submissions/review` |
| 4 | Admin | Approve submission on Admin Review page |
| 5 | Employee | `CERTIFICATE_APPROVED` in bell and inbox |
| 6 | (Optional) | Reject path → `CERTIFICATE_REJECTED` with reason in copy |
| 7 | Both | Mark-read / mark-all-read keeps badge in sync |

---

## Defects resolved

| ID | Summary | Status |
|----|---------|--------|
| CW-D01 | Employee dashboard failed when secondary APIs errored | **Pass** — `Promise.allSettled` fix in `dashboardApi.ts` |

---

## Out of scope (v0.6.1)

- Initiative list/detail full UI
- Leaderboards, study materials, projects UI
- Email notifications
- Notification preferences
- WebSockets / real-time push
- Bulk certificate operations
- Certificate file preview/download in review UI
- Dashboard status chips, filtering, and Pending Reviews link

---

## Test baseline

| Area | Baseline |
|------|----------|
| Frontend | **213 tests** (`cd frontend && npm test`) |
| Backend | `NotificationFactoryTest`, `CertificateSubmissionServiceTest` |

---

## Next gate — Phase 4

Run notification E2E validation script and document results in `docs/testing-and-defect-history.md`.
