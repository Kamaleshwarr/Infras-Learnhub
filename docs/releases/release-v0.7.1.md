# Engineering Learning Hub v0.7.1

**Theme:** Initiative Management (Create, Edit, Lifecycle, Delete)  
**Status:** Validated — ready for merge (PR #43)  
**Depends on:** v0.7.0 Initiatives Experience (validated, PR #36)  
**Classification:** Full-stack feature release — admin initiative management UI with backend lifecycle and delete enforcement

---

## Release overview

v0.7.1 delivers the complete **Initiative Management** module on top of the v0.7.0 Initiatives Experience. Administrators can create, edit, transition lifecycle status, and permanently delete initiatives from the list and detail pages. Employees continue to browse and submit certificates without seeing management controls.

**Success criterion (met):** Admin creates a draft initiative → publishes → edits metadata → manages lifecycle transitions → deletes when no submissions exist; blocked delete when submissions present; employees never see delete or lifecycle controls; business rules enforced server-side.

**Validated via:** PR #43 — `cursor/f15-delete-initiative-7a10` → `main` (engineering review 2026-06-19; manual QA passed; automated tests 388 frontend pass)

---

## Features delivered

### F11 / Phase 0 — Initiative management foundation

| Deliverable | Capability |
|-------------|------------|
| `Initiative` types + request DTOs | Shared frontend types aligned to backend contracts |
| `initiativesApi` | CRUD + lifecycle HTTP client |
| `initiativeFormState` | Shared form values, validation, dirty baseline, request builders |
| `initiativeDateUtils` | UTC date input helpers |
| `initiativeMessages` | Centralized copy for all initiative management surfaces |
| `InitiativeFormFields` | Reusable form fields with field limits |
| `InitiativeListToolbar` | Admin create entry point |
| `initiativeListParams` | URL query parse/build, admin vs employee API param mapping |

### F12 — Create Initiative (PR stack)

| Deliverable | Capability |
|-------------|------------|
| `CreateInitiativeDialog` | Admin create dialog with validation and discard guard |
| List integration | Toolbar + snackbar + list refresh on success |
| Business rules | Always creates as DRAFT; start ≥ today (UTC); field limits enforced |

### F13 — Edit Initiative

| Deliverable | Capability |
|-------------|------------|
| `EditInitiativeDialog` | Admin edit from list (icon) and detail (button) |
| `InitiativeMetadataPanel` | Created-by, created-on, last-updated display |
| Date rules | Unchanged past start preserved; modified start ≥ today (UTC) |
| Status-aware expiry | Draft — none; Active — countdown; Expired — "Expired" label |
| Field limits | Title 100, description 2000, reward 500 characters |

### F14 — Initiative Lifecycle Management (PR #42)

| Deliverable | Capability |
|-------------|------------|
| `InitiativeLifecycleActions` | Publish, Return to Draft, Mark Expired, Reactivate |
| `InitiativeLifecycleConfirmDialog` | Shared confirm dialog with status-specific messaging |
| Backend enforcement | Dedicated `POST` endpoints; transition matrix in service layer |
| Status read-only | Removed from Create/Edit forms; chip display only |

**Transition matrix:**

| From | To | Action |
|------|-----|--------|
| DRAFT | ACTIVE | Publish |
| ACTIVE | DRAFT | Return to Draft |
| ACTIVE | EXPIRED | Mark Expired |
| EXPIRED | ACTIVE | Reactivate |

Blocked: DRAFT → EXPIRED, EXPIRED → DRAFT, any status change via metadata PUT.

### F15 — Delete Initiative (PR #43)

| Deliverable | Capability |
|-------------|------------|
| `InitiativeDeleteAction` | Admin delete from list (icon) and detail (button) |
| Eligibility check | Submission count only (`0` = allow, `>0` = block) |
| Blocked dialog | Informational — title, status, count, alternatives, Close only |
| Confirm dialog | Permanent delete warning with status-specific messaging |
| Backend enforcement | `assertDeletable()` before hard delete; HTTP 409 when blocked |
| `BusinessConflictException` | Structured 409 response; `DataIntegrityViolationException` fallback |

**Also delivered (v0.7.1 stack):**

- App-wide long-text display standard (`TruncatedTextWithTooltip`, `WrappingText`, `TEXT_DISPLAY_LIMITS`)
- Flyway `V10__relax_learning_initiative_date_constraint.sql` — expiry ≥ start (same-day allowed)
- Flyway `V11__tighten_learning_initiative_text_limits.sql` — title `VARCHAR(100)`

---

## Business rules introduced

### Create (F12)

- Always creates as **DRAFT**
- Start date must be today (UTC) or later
- Expiry date must be on or after start date
- Field limits: title 100, description 2000, reward 500

### Edit (F13)

- Status is read-only — never changed via edit form
- **Unchanged start date:** stored start preserved (even if in the past)
- **Modified start date:** new start must be today (UTC) or later
- Mark Expired moved to lifecycle action (not edit form)

### Lifecycle (F14)

- Transitions only via dedicated actions and backend `POST` endpoints
- Publish validates full metadata before DRAFT → ACTIVE
- Return to Draft allowed with existing submissions; employees hidden; history preserved
- Mark Expired sets expiry to today (UTC); employees lose access
- Reactivate requires expiry ≥ today (UTC) and ≥ start date

### Delete (F15)

- Eligibility depends **only** on certificate submission count
- Hard delete only — no soft delete, cascade delete, clone, or typed confirmation
- Backend validates before `repository.delete()`; returns HTTP **409** when blocked
- Structured INFO log on successful delete
- Title reuse allowed after successful delete

---

## User flows

### Admin — create and publish

1. Admin opens `/initiatives` → clicks **Create Initiative**.
2. Fills form → initiative created as DRAFT → list refreshes.
3. Clicks **Publish** → confirmation dialog → initiative becomes ACTIVE.
4. Employees gain access per configured start date.

### Admin — edit metadata

1. Admin clicks edit icon (list) or **Edit** button (detail).
2. Modifies title, description, reward, or dates per F13 rules.
3. Saves → snackbar confirmation → list or detail refreshes.

### Admin — lifecycle management

1. Admin uses lifecycle actions on list or detail.
2. Confirms transition in dialog.
3. Status chip and expiry banners update; employees see/hide initiative per rules.

### Admin — delete

1. Admin clicks delete (list icon or detail button).
2. System checks submission count.
3. If blocked → informational dialog with alternatives (Close only).
4. If allowed → confirmation dialog → permanent delete.
5. Detail delete navigates to list; list delete refreshes in place.

### Employee — unchanged experience

1. Employee browses `/initiatives` — no management controls visible.
2. Opens detail — progress, top learner, submit CTA only.
3. Cannot access create, edit, lifecycle, or delete actions.

---

## Validation checklist

### F12–F13 (manual QA passed)

| # | Scenario | Status |
|---|----------|--------|
| 1 | Create initiative as DRAFT | **Pass** |
| 2 | Create — start before today rejected | **Pass** |
| 3 | Edit — past start preserved when unchanged | **Pass** |
| 4 | Edit — modified start ≥ today enforced | **Pass** |
| 5 | Field limits enforced | **Pass** |
| 6 | Status-aware expiry banners | **Pass** |

### F14 (manual QA passed — PR #42)

| # | Scenario | Status |
|---|----------|--------|
| 1 | Publish draft → ACTIVE | **Pass** |
| 2 | Return to Draft — employees hidden, submissions preserved | **Pass** |
| 3 | Mark Expired — expiry = today (UTC) | **Pass** |
| 4 | Reactivate — expiry ≥ today required | **Pass** |
| 5 | Lifecycle buttons shown only for valid transitions | **Pass** |
| 6 | Edit saves metadata without changing status | **Pass** |
| 7 | Create/Edit regression | **Pass** |

### F15 (manual QA passed — PR #43)

| # | Scenario | Status |
|---|----------|--------|
| 1 | Create Draft → Delete → succeeds | **Pass** |
| 2 | After delete, create initiative with same title → succeeds | **Pass** |
| 3 | Delete allowed — confirmation dialog (title, status, submissions: 0) | **Pass** |
| 4 | Delete blocked — informational dialog, Close only | **Pass** |
| 5 | Blocked dialog — status-specific lifecycle alternatives | **Pass** |
| 6 | Active delete — employee access warning | **Pass** |
| 7 | Expired delete — historical record note | **Pass** |
| 8 | List page delete (compact icon) | **Pass** |
| 9 | Detail page delete (button) → navigates to list | **Pass** |
| 10 | Backend HTTP 409 when delete blocked | **Pass** |
| 11 | Employee list — no delete controls | **Pass** |
| 12 | Employee detail — no delete controls | **Pass** |
| 13 | Lifecycle regression unchanged | **Pass** |
| 14 | Edit initiative regression | **Pass** |
| 15 | 409 race — frontend switches to blocked dialog | **Pass** |

---

## Test coverage summary

| Area | Command | Result (v0.7.1) |
|------|---------|-----------------|
| Frontend unit/integration | `cd frontend && npm test` | **388 tests** — **85 files** — pass |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend initiative-scoped | `mvn test -Dtest='com.company.learninghub.initiative.**'` | Pass |
| Backend full | `mvn -f backend/pom.xml test` | **247 run**; **12 skipped**; **3 pre-existing failures** (unrelated) |
| Backend package | `mvn -f backend/pom.xml package -DskipTests` | Pass |

### Frontend tests added (v0.7.1 cumulative — F11 through F15)

| Phase | Key test files |
|-------|----------------|
| F11 | `initiativeFormState.test.ts`, `initiativeDateUtils.test.ts`, `initiativesApi.test.ts` |
| F12 | `CreateInitiativeDialog.test.tsx`, `InitiativeListPage.test.tsx` |
| F13 | `EditInitiativeDialog.test.tsx`, `InitiativeMetadataPanel.test.tsx`, `InitiativeExpiryBadge.test.tsx` |
| F14 | `InitiativeLifecycleActions.test.tsx` |
| F15 | `InitiativeDeleteAction.test.tsx`, `apiErrors.test.ts` (`isConflictError`) |

**Delta from v0.7.0 baseline:** +96 tests (+17 files).

### Backend tests added (v0.7.1)

| Area | Key test files |
|------|----------------|
| Lifecycle | `LearningInitiativeServiceTest` — transition matrix, publish, mark-expired, reactivate |
| Delete | `LearningInitiativeServiceTest` — delete allowed/blocked; `GlobalExceptionHandlerTest` — 409 |
| Validation | `InitiativeRequestValidationTest` — DTO date-range, field limits |
| Visibility | `LearningInitiativeEmployeeVisibilityTest` |

---

## Manual QA summary

| Phase | Manual QA | Sign-off |
|-------|-----------|----------|
| F12 Create | Passed | Engineering review |
| F13 Edit | Passed | Engineering review |
| F14 Lifecycle | Passed | Engineering review (PR #42) |
| F15 Delete | Passed | Engineering review (PR #43) |

All 15 F15 manual validation scenarios passed during release-readiness sign-off.

---

## Known limitations

| Item | Notes |
|------|-------|
| Status/date via PUT | Admin can set expiry in the past on ACTIVE via edit without `markExpired`; employee visibility still governed by date rules |
| Delete eligibility API | Frontend uses `submissionsApi.listAll({ size: 1 })` for count; no dedicated count endpoint |
| Dual-role users | Users with both ADMIN and EMPLOYEE roles see admin controls and employee CTAs |
| Route-level admin guard | Initiative pages require auth only; admin controls hidden client-side |
| Pre-existing backend failures | 3 unrelated test failures in full suite (notifications, user management) |
| Initiative leaderboard page | Route exists; UI remains placeholder |

---

## Deferred features (not in v0.7.1)

| Item | Target |
|------|--------|
| Clone Initiative | Future release (deferred from F14) |
| Soft delete / cascade delete / typed confirmation | Explicitly out of scope for F15 |
| Initiative leaderboard full page UI | Post v0.7.1 |
| Top 3 learners on detail | Post v0.7.1 |
| Rejected submission resubmission | Future — backend `UNIQUE(employee_id, initiative_id)` |
| Dashboard initiative drilldowns | Post v0.7.1 |

---

## Merge information

| PR | Branch | Scope | Status |
|----|--------|-------|--------|
| #42 | `cursor/f14-initiative-lifecycle-7a10` | F14 Lifecycle Management | Ready for merge |
| #43 | `cursor/f15-delete-initiative-7a10` | F15 Delete Initiative | Ready for merge |

**Recommended merge order:** v0.7.0 (PR #36) → F14 (PR #42) → F15 (PR #43) → tag `v0.7.1`

**Post-merge smoke test:**

1. Admin: create draft → publish → edit → return to draft → publish → mark expired → reactivate
2. Admin: create draft → delete → recreate with same title
3. Admin: attempt delete on initiative with submissions → blocked dialog + HTTP 409
4. Employee: verify no management controls on list or detail
5. Employee: submit certificate workflow regression

---

## Upgrade / deployment considerations

- **Flyway migrations:** `V10` (date constraint) and `V11` (title length) — apply before backend deploy
- **Backend deploy required** — lifecycle endpoints, delete validation, `BusinessConflictException`
- **Frontend deploy required** — initiative management UI components
- **Rollback:** revert deploys; V10/V11 migrations are additive constraints (review before rollback)
- **No breaking API changes** — existing GET/list endpoints unchanged; new POST lifecycle endpoints additive

---

## Related documentation

- Defect & validation history: `docs/testing-and-defect-history.md`
- Roadmap: `docs/project-roadmap.md`
- Architecture: `.cursor/architecture.md`
- Prior release: `docs/releases/release-v0.7.0.md`
- Project context: `.cursor/project-context.md`
