# Engineering Learning Hub v0.7.0

**Theme:** Initiatives Experience (List, Detail, Submit Integration)  
**Status:** Validated — ready for merge (PR #36)  
**Depends on:** v0.6.2 Certificate Preview & Pending Reviews Drilldown (shipped PR #32)  
**Classification:** Frontend feature release — learning initiatives employee/admin browse and certificate submit handoff

---

## Release overview

v0.7.0 delivers the first production-ready **Initiatives Experience** surfaces: searchable initiative list, initiative detail with progress and top learner preview, and seamless handoff from initiative detail to Submit Certificate with initiative pre-selection.

**Success criterion (met):** Employee or admin browses initiatives → opens detail → submits certificate with initiative pre-selected; list and detail UX match approved v0.7.0 spec including F2.1 polish.

**Validated via:** PR #36 — `cursor/f10-submit-preselect-7a10` → `main` (manual QA 2026-06-19; automated tests 292 pass)

---

## Features delivered

### F0 — Initiative types, params, and route foundation (PR #33)

| Deliverable | Capability |
|-------------|------------|
| `Initiative` type + `InitiativeSummary` alias | Shared frontend types aligned to backend contracts |
| `initiativeListParams`, `initiativeDisplay`, `initiativeMessages` | URL sync, formatting, copy |
| Route update | `/leaderboards/initiatives/:initiativeId` |
| API alignment | `initiativesApi`, `leaderboardsApi` types |

### F1 — Initiative list page (PR #34)

| Deliverable | Capability |
|-------------|------------|
| `/initiatives` | Searchable, sortable, paginated list with URL query sync |
| Admin status filter tabs | Filter by initiative status |
| Employee visibility | Backend visibility rules only (no status filter sent) |
| Responsive layouts | Desktop table + mobile card list |
| Expiry badges | Visual expiry indicators on list rows |

### F2 — Initiative detail page (PR #35)

| Deliverable | Capability |
|-------------|------------|
| `/initiatives/:initiativeId` | Title, description, status, UTC date range, reward |
| `MyProgressCard` | Employee submission status |
| `TopLearnerCard` | Rank #1 preview from initiative leaderboard API |
| `InitiativeActionBar` | Submit Certificate CTA → `/submissions/new?initiativeId=` |
| Fault-isolated loading | Primary `initiativesApi.get`; secondary via `Promise.allSettled` |
| Error handling | 404 not-found panel, primary retry, isolated secondary failures |

### F10 — Submit Certificate initiative pre-selection (PR #36)

| Deliverable | Capability |
|-------------|------------|
| `?initiativeId=` query param | Read on `/submissions/new` |
| Pre-select dropdown | Case-insensitive ID match after initiatives + submissions load |
| Guardrails | Skip when unknown, already submitted (incl. rejected), or user changes dropdown manually |
| Workflow preservation | Existing validation, file upload, and redirect behavior unchanged |

### F2.1 — Initiative UX polish (PR #36)

| ID | Deliverable | Capability |
|----|-------------|------------|
| F2.1a | List column label | **Reward / Benefits** (displays `rewardDescription`) |
| F2.1b | Detail navigation | **Back to Initiatives** link above page header |

---

## Explicitly not in v0.7.0

| Item | Target |
|------|--------|
| Initiative Management (create/edit/delete/lifecycle) | v0.7.1 |
| Rejected submission resubmission workflow | Future release (backend `UNIQUE(employee_id, initiative_id)`) |
| Top 3 learners on detail page | Future release |
| Full initiative leaderboard page UI | Future release (`InitiativeLeaderboardPage` remains placeholder) |
| Dashboard initiative drilldowns | Future release |
| Backend / API contract changes | Out of scope |
| Flyway migrations | Out of scope |

---

## User flows

### Employee — browse and submit

1. Employee opens `/initiatives` (active initiatives visible per backend rules).
2. Searches, sorts, or paginates the list.
3. Clicks a row → `/initiatives/:initiativeId`.
4. Reviews description, reward/benefits, expiry, top learner, and personal progress.
5. Clicks **Submit Certificate** → `/submissions/new?initiativeId=<id>`.
6. Initiative dropdown is pre-selected; employee uploads file and submits.
7. Redirects to My Submissions with success notification.

### Admin — browse and review context

1. Admin opens `/initiatives` with status filter tabs.
2. Opens initiative detail — sees content without employee-only progress/actions.
3. Uses **Back to Initiatives** to return to list.

---

## Validation checklist

| # | Scenario | Expected result | Status |
|---|----------|-----------------|--------|
| 1 | Initiative list loads (employee) | Active initiatives visible; search/sort/pagination work | **Pass** |
| 2 | Initiative list loads (admin) | Status filter tabs; all statuses browsable | **Pass** |
| 3 | Initiative detail from direct URL | Title, description, dates, reward, status render | **Pass** |
| 4 | Top Learner card | Rank #1 shown when leaderboard data exists | **Pass** |
| 5 | Submit Certificate CTA (no submission) | Links to `/submissions/new?initiativeId=` | **Pass** |
| 6 | F10 pre-selection | Dropdown pre-selected from query param | **Pass** |
| 7 | Existing submission behavior | View My Submission; no resubmit CTA | **Pass** |
| 8 | Rejected submission | Helper text; no resubmit CTA | **Pass** |
| 9 | F2.1a column label | Desktop table header **Reward / Benefits** | **Pass** |
| 10 | F2.1b back navigation | **Back to Initiatives** on detail (employee + admin) | **Pass** |
| 11 | Admin visibility rules | No employee progress/actions for admin | **Pass** |
| 12 | 404 initiative | Not-found panel with Browse Initiatives | **Pass** |
| 13 | Primary load failure | Retry button recovers | **Pass** |
| 14 | Secondary load failure | Detail content remains; isolated error alerts | **Pass** |
| 15 | Certificate workflow regression | Submit without query param unchanged | **Pass** |

---

## Test coverage summary

| Area | Command | Result (v0.7.0 / PR #36) |
|------|---------|--------------------------|
| Frontend unit/integration | `cd frontend && npm test` | **292 tests** — **68 files** — pass |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend | `mvn -f backend/pom.xml test` | Unchanged from v0.6.2 baseline (no backend changes in v0.7.0) |

### Frontend tests added (v0.7.0 cumulative — F0 through F10)

| Phase | Key test files |
|-------|----------------|
| F0 | `initiatives.test.ts`, `initiativeListParams.test.ts`, `initiativesApi.test.ts`, `leaderboardsApi.test.ts`, `AppRoutes.test.tsx` |
| F1 | `InitiativeListPage.test.tsx`, `InitiativeListViews.test.tsx`, `InitiativeSearchBar.test.tsx`, `InitiativeStatusFilterTabs.test.tsx` |
| F2 | `InitiativeDetailPage.test.tsx`, component tests for progress/top learner/action bar |
| F10 / F2.1 | `SubmitCertificatePage.test.tsx`, `SubmitCertificateForm.test.tsx`, `InitiativeDetailPage.test.tsx`, `InitiativeListViews.test.tsx` |

**Delta from v0.6.2 baseline:** +61 tests (+14 files).

---

## Key commits (PR #36 stack)

| Commit | PR | Description |
|--------|-----|-------------|
| `7aa692c` | #33 | F0 — initiative types, params, route foundation |
| `d0e340f` | #34 | F1 — employee and admin initiative list page |
| `f2631c1` | #35 | F2 — initiative detail page with fault-isolated loading |
| `bf13659` | #36 | F10 submit pre-select + F2.1 UX polish |

---

## Known limitations

| Item | Notes |
|------|-------|
| Initiative leaderboard page | Route exists; UI is placeholder only |
| Top learner on detail | Rank #1 only — not top 3 |
| Mobile list | Card layout has no column headers (F2.1a is desktop table only) |
| Pre-select skip conditions | Already-submitted (incl. rejected) and unknown IDs leave dropdown unselected by design |
| Initiative management | Backend CRUD exists; no admin create/edit/delete UI |
| Dashboard initiative widgets | Not linked to new initiative pages in this release |

---

## Upgrade / deployment considerations

- **No Flyway migrations** — database schema unchanged from v0.6.2
- **No backend deploy required** for initiatives experience alone (frontend-only release)
- **Frontend deploy:** rebuild and deploy static assets from merged `main`
- **Rollback:** revert frontend deploy; no schema rollback required
- **Smoke test after deploy:** `/initiatives` → detail → Submit Certificate with pre-select

---

## Related documentation

- Defect & validation history: `docs/testing-and-defect-history.md`
- Roadmap: `docs/project-roadmap.md`
- Prior release: `docs/releases/release-v0.6.2.md`
- Project context: `.cursor/project-context.md`
