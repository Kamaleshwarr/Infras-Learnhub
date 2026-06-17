# Engineering Learning Hub v0.6.1

**Theme:** Certificate Workflow UI & Notification E2E Validation  
**Status:** Ready for release (PR #29)  
**Depends on:** v0.6 In-App Notification Infrastructure (merged PR #28)  
**Classification:** Feature release — completes notification E2E for certificate workflow

---

## Release overview

v0.6.1 delivers the minimum certificate workflow UI required to submit, track, and review learning initiative certificates through the application, and validates notification producers end-to-end.

**Success criterion (met):** Employee submits certificate → admin receives `CERTIFICATE_SUBMITTED` → admin approves on Certificate Review → employee receives `CERTIFICATE_APPROVED`.

**Merge target:** PR #29 — `cursor/v0.6.1-certificate-workflow-de8d` → `main`

---

## Features delivered

### Certificate Workflow UI

| Route | Role | Capability |
|-------|------|------------|
| `/submissions/new` | Employee | Submit certificate (initiative select, file upload, comments) |
| `/submissions` | Employee | My Submissions list, status filter, pagination, refresh |
| `/submissions/review` | Admin | Certificate Review queue, approve/reject with dialogs |

### Shared components & API

- `submissionsApi` — list, submit, approve, reject
- `SubmissionStatusChip`, `submissionMessages`
- Submit Certificate dropdown UX — available initiatives first, already-submitted last (disabled), `expiryDateUtc ASC` within each group

### Notification integration

- `CERTIFICATE_SUBMITTED` → all active admins; `actionPath` **`/submissions/review`**
- `CERTIFICATE_APPROVED` → submitting employee; `actionPath` **`/submissions`**
- `CERTIFICATE_REJECTED` → submitting employee (reject path available; optional E2E)

### Dashboard improvements

- **CW-D01:** Employee dashboard fault-isolated loading (`Promise.allSettled` for secondary widgets)
- **CW-D02:** Admin dashboard fault-isolated loading (primary: initiatives + pending reviews; secondary: leaderboard, materials, projects)

### Backend (v0.6.1 delta)

- Employee UTC calendar-day initiative visibility fix
- `NotificationFactory.certificateSubmitted()` `actionPath` → `/submissions/review`
- No new Flyway migrations

---

## Validation results

| Phase / area | Status | Notes |
|--------------|--------|-------|
| Phase 0 — API/types/route prep | **Passed** | |
| Phase 1 — Submit Certificate | **Passed** | Initiative visibility issue was test data (`DRAFT`), not code defect |
| Phase 2 — My Submissions | **Passed** | |
| Dropdown UX | **Passed** | Available-first ordering |
| Phase 3 — Admin Review | **Passed** | Approve/reject UI |
| Phase 4 — Notification E2E | **Passed** | Submit → admin notify → approve → employee notify; badge sync |
| CW-D01 regression | **Passed** | Employee dashboard |
| CW-D02 regression | **Passed** | Admin dashboard |

### Phase 4 E2E (validated)

1. Employee certificate submission
2. Admin `CERTIFICATE_SUBMITTED` notification
3. Navigation to Certificate Review from notification (`/submissions/review`)
4. Approve workflow
5. Employee `CERTIFICATE_APPROVED` notification
6. Navigation to My Submissions from notification (`/submissions`)
7. Status transition `SUBMITTED` → `APPROVED`
8. Notification badge synchronization
9. Employee dashboard regression (CW-D01)
10. Admin dashboard regression (CW-D02)

---

## Defects fixed

| ID | Summary | Fix commit area |
|----|---------|-----------------|
| **CW-D01** | Employee dashboard blanked when secondary APIs failed | `dashboardApi.ts` — `b7bf697`, `e9132c9` |
| **CW-D02** | Admin dashboard blanked when secondary APIs failed | `dashboardApi.ts` — `7212365` |

Temporary Phase 1 investigation diagnostics were removed before release (`e2b3290`).

---

## Test coverage summary

| Area | Command | Baseline (v0.6.1) |
|------|---------|-------------------|
| Frontend | `cd frontend && npm test` | **220 tests** — pass |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend | `mvn -f backend/pom.xml test` | **211 tests** run; **12 skipped** (Testcontainers/Docker); unit tests pass in standard CI |

### Notable frontend test areas added (v0.6.1)

- `SubmitCertificatePage`, `SubmitCertificateForm`, `submissionInitiativeFilter`
- `MySubmissionsPage`, `MySubmissionsTable`, `mySubmissionsListParams`
- `AdminReviewPage`, `AdminReviewTable`, approve/reject dialogs
- `dashboardApi` — employee and admin fault-isolation

### Backend tests (unchanged scope)

- `CertificateSubmissionServiceTest` — submit/approve/reject + notification producers
- `NotificationFactoryTest` — message templates and `actionPath`
- `NotificationServiceTest` — inbox and certificate notification delivery

---

## Key commits (PR #29)

| Commit | Description |
|--------|-------------|
| `8ec9782` | Phase 0 — API, types, shared route prep |
| `acc0bb8` | Phase 1 — Submit Certificate page |
| `632c26d` | Decouple initiative/submission loading |
| `e9132c9` | Phase 2 — My Submissions; CW-D01 |
| `db8149e` | Dropdown UX — available initiatives first |
| `e2b3290` | Remove Phase 1 diagnostics; doc updates |
| `9f67db7` | Phase 3 — Admin Review page |
| `7212365` | CW-D02 — admin dashboard fault isolation |

---

## Known limitations

| Item | Notes |
|------|-------|
| Initiative list/detail full UI | Not in v0.6.1; initiative select uses API only |
| Certificate file preview/download in review | Not implemented |
| Dashboard status chips / filtering | Deferred |
| Admin Pending Reviews metric link | Deferred (metric displays; no deep link) |
| Notification badge refresh | Polls every 60s + window focus; not real-time push |
| `CERTIFICATE_REJECTED` E2E | Reject UI shipped; optional manual validation |
| Expiring Initiatives metric | Client-side 14-day window over first 50 ACTIVE initiatives |
| Email notifications | Deferred |
| Bulk certificate operations | Out of scope |

---

## Deferred to future releases

- Dashboard UX enhancements (status chips, filtering, Pending Reviews link)
- Email notification channel (account lifecycle)
- Initiative / leaderboard / study materials / projects full UI
- WebSockets / notification preferences
- Global Search

---

## Upgrade / deployment considerations

- **No Flyway migrations** in v0.6.1 — database schema unchanged from v0.6
- **Backend deploy:** standard Spring Boot artifact; no new environment variables
- **Frontend deploy:** rebuild and deploy static assets
- **Rollback:** revert application deploy; no schema rollback required
- **Smoke test after deploy:** one certificate submit → admin notification → approve → employee notification (Phase 4 script)

---

## Related documentation

- Defect & validation history: `docs/testing-and-defect-history.md`
- Roadmap: `docs/project-roadmap.md`
- Architecture: `docs/backend-architecture-and-roadmap.md`
- Prior release: `docs/releases/release-v0.6.md`
- Notification infrastructure: `docs/releases/notification-infrastructure-final-summary.md`
