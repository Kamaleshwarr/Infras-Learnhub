# Engineering Learning Hub v0.6.1 (Proposed)

**Theme:** Certificate Workflow UI & Notification E2E Validation  
**Status:** Proposed — **awaiting approval** (no implementation started)  
**Depends on:** v0.6 In-App Notification Infrastructure (merged PR #28)

---

## Purpose

Close the gap between notification **infrastructure** (v0.6) and notification **feature completeness** by delivering the minimum certificate workflow UI required to trigger and validate producers end-to-end through the application.

**Success criterion:** At least one full path verified in the app — e.g. employee submits certificate → admin receives `CERTIFICATE_SUBMITTED` in bell/inbox → admin approves → employee receives `CERTIFICATE_APPROVED`.

---

## Proposed scope

### 1. Submit Certificate page (`/submissions/new`)

**Route:** Employee-only (existing `RoleRoute`)

- Select active initiative (dropdown from initiatives API)
- Upload certificate file (PDF, JPEG, PNG — match backend allowlist)
- Optional comments
- Submit via `POST /initiatives/{initiativeId}/submissions` (multipart)
- Success feedback and navigation to My Submissions

**Backend:** No changes expected — `CertificateSubmissionService.submit()` and `CERTIFICATE_SUBMITTED` producer already exist.

---

### 2. My Submissions page (`/submissions`)

**Route:** Employee-only

- List current user's submissions via `GET /me/submissions`
- Display status (`SUBMITTED`, `APPROVED`, `REJECTED`), submitted date, rejection reason when present
- Pagination and optional status filter
- Link to Submit Certificate

**Backend:** No changes expected.

---

### 3. Admin Review page (new route, e.g. `/submissions/review`)

**Route:** Admin-only

- List pending submissions via `GET /submissions?status=SUBMITTED`
- Show employee, initiative, submitted date, comments
- Row actions: **Approve**, **Reject** (with required rejection reason dialog)

**Backend:** No changes expected — approve/reject endpoints exist.

**Frontend API additions:**

- `submissionsApi.approve(submissionId)`
- `submissionsApi.reject(submissionId, { rejectionReason })`

---

### 4. Approve / Reject actions

- Confirm dialog for approve
- Reject dialog with validated reason (required, max length per backend)
- Refresh list and surface errors from API
- Optional: link from admin dashboard pending-reviews widget to review page

---

### 5. Notification end-to-end validation

Manual validation script (application UI only — not Swagger):

| Step | Actor | Expected |
|------|-------|----------|
| 1 | Employee | Submit certificate on Submit Certificate page |
| 2 | Admin | Bell badge increments; `CERTIFICATE_SUBMITTED` in dropdown and `/notifications` |
| 3 | Admin | Approve submission on Admin Review page |
| 4 | Employee | `CERTIFICATE_APPROVED` in bell and inbox |
| 5 | (Optional) | Reject path → `CERTIFICATE_REJECTED` with reason in copy |
| 6 | Both | Mark-read / mark-all-read keeps badge in sync |

Document results in `docs/testing-and-defect-history.md` under v0.6.1.

---

## Out of scope (v0.6.1)

- Initiative list/detail full UI (initiative select may use existing API; full initiative pages remain separate backlog)
- Leaderboards, study materials, projects UI
- Email notifications
- Notification preferences
- WebSockets / real-time push
- Bulk certificate operations
- Certificate file preview/download in review UI (optional stretch — not required for E2E)

---

## Implementation phases (proposed)

| Phase | Deliverable | Enables |
|-------|-------------|---------|
| **1** | `submissionsApi` approve/reject + Submit Certificate page | `CERTIFICATE_SUBMITTED` E2E |
| **2** | My Submissions page | Employee visibility of status |
| **3** | Admin Review page + approve/reject UI | `CERTIFICATE_APPROVED` / `CERTIFICATE_REJECTED` E2E |
| **4** | E2E validation + docs update | Feature-complete notification milestone |

---

## Testing expectations

### Frontend

- Submit form validation (initiative required, file required, content type)
- My Submissions list render and pagination
- Admin review approve/reject flows (dialogs, API error handling)
- Regression: notification bell/badge still syncs after certificate actions

### Backend

- No new modules expected; existing `CertificateSubmissionServiceTest` producer coverage remains sufficient unless API contracts change

---

## Approval gate

**Do not start implementation until this plan is approved.**

After approval:

1. Create branch `cursor/v0.6.1-certificate-workflow-dd41` (or agreed naming)
2. Implement phases 1–3
3. Run E2E validation script
4. Update release docs and merge when validation passes
