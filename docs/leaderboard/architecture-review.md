# Leaderboard Architecture Review

**Type:** Architecture review and planning only — no implementation  
**Date:** 2026-07-10  
**Reviewer scope:** Repository context, existing code, migrations, score sources, UX, permissions, data model, APIs, performance, tests, phased plan

---

## 1. Repository context reviewed

### Mandatory `.cursor/` files

| File | Notes |
|------|-------|
| `.cursor/architecture.md` | Layered backend; `LeaderboardQueryRepository` for SQL-heavy reporting; Flyway V1–V19 listed |
| `.cursor/project-context.md` | Leaderboards listed as completed backend module; initiative leaderboard UI deferred |
| `.cursor/engineering-standards.md` | Reuse patterns, 11-step workflow, minimal scope |
| `.cursor/coding-standards.md` | Language conventions (referenced via engineering standards) |
| `.cursor/templates/*` | API, Flyway, frontend page templates |
| `.cursor/agents/*` | QA, frontend, database, backend architect agent briefs |
| `.cursor/commands/*` | create-api, create-module, create-ui, review-pr |

### Core project documentation

| File | Relevance |
|------|-----------|
| `README.md` | Module index; Flyway V1–V19 |
| `docs/project-roadmap.md` | Initiative leaderboard placeholder noted in backlog |
| `docs/development-workflow.md` | 11-step mandatory workflow for future phases |
| `docs/testing-guide.md` | Test pyramid, regression checklist |
| `docs/contributing.md` | Conventions (Flyway version stale — see mismatches) |
| `docs/backend-architecture-and-roadmap.md` | Platform modules; no leaderboard section (stale) |
| `docs/testing-and-defect-history.md` | Dashboard fault isolation (CW-D01/D02); leaderboard placeholder |

### Module-specific documentation

| Area | Files reviewed |
|------|----------------|
| Learn | `docs/learn/README.md`, `database-overview.md`, `api-reference.md`, `catalog.md` |
| Initiatives | `docs/releases/release-v0.7.0.md`, `release-v0.7.1.md` |
| Certifications | `docs/releases/release-v0.6.1.md`, `release-v0.6.2.md` |
| Projects | `docs/project/README.md`, `01-current-state-audit.md` through `12-future-compatibility.md`, P1–P4 implementation reports |
| Leaderboards (prior) | None existed — created in this review |
| v0.8.0 product | `00-product-design.md`, `02-information-architecture.md`, `03-business-rules.md`, `06-future-enhancements.md`, `07-implementation-plan.md` |
| Release reports | `release-v0.2.md` (leaderboards shipped), F16–F18 Learn reports |

---

## 2. Documentation vs production code mismatches

| Topic | Documentation says | Production code / migrations say | Resolution for analysis |
|-------|-------------------|----------------------------------|-------------------------|
| **Flyway version** | `contributing.md`: V15; `development-workflow.md`: V18 | **V19** (`V19__project_team_and_contacts.sql`) | Follow **V19** |
| **Leaderboard completeness** | `project-context.md`, roadmap: backend "complete" | Backend APIs functional; **frontend pages are placeholders** | Backend partial; UI not started |
| **Global scoring model** | Product docs discuss points/gamification deferral | Backend uses **approved certification count**, not points | Cert-count is current truth |
| **Learn leaderboards** | `v0.8.0/03-business-rules.md`: deferred | No Learn integration in leaderboard code | Excluded today; proposed for L2 |
| **Initiative contribution model** | Future enhancements imply richer initiative participation | **No participant/contribution tables** — only `certificate_submissions` | Initiative LB = cert completion order |
| **Initiative visibility on LB API** | Employees get 404 for non-visible initiatives elsewhere | `GET /leaderboards/initiatives/{id}` has **no initiative visibility check** | Gap — fix in L1 |
| **Frontend `PersonalLeaderboard` type** | N/A | `leaderboardsApi.ts` shape **does not match** `PersonalLeaderboardResponse` (missing `employee`, `earliestSubmittedAtUtc`; uses `globalRank` at top level vs backend `globalRank`) | Fix types in L1 UI phase |
| **`backend-architecture-and-roadmap.md`** | Last updated 2026-06-16 | Omits Learn, Projects P1–P4, leaderboard APIs | Stale — do not rely on |

---

## 3. Existing leaderboard implementation audit

### 3.1 Backend package structure

```text
com.company.learninghub.leaderboard/
├── controller/LeaderboardController.java
├── dto/
│   ├── GlobalLeaderboardEntryResponse.java
│   ├── InitiativeLeaderboardEntryResponse.java
│   ├── LeaderboardEmployeeResponse.java
│   ├── PersonalLeaderboardResponse.java
│   └── RecentApprovalResponse.java
├── repository/LeaderboardQueryRepository.java
└── service/LeaderboardService.java
```

**Not present:** domain entities, enums, Flyway tables, mappers (DTOs built in repository), scheduled jobs, event listeners, notification integration.

### 3.2 Database model

**No dedicated leaderboard tables.** Rankings are computed at query time from:

| Table | Role in leaderboard |
|-------|---------------------|
| `certificate_submissions` | Primary source — `approval_status = 'APPROVED'` |
| `users` | Employee identity (name, email, employee_id) |
| `learning_initiatives` | Initiative title for initiative leaderboard |

Relevant constraints/indexes (V4):

- `uk_certificate_submissions_employee_initiative` — one submission per employee per initiative
- `idx_certificate_submissions_status`, `_initiative`, `_submitted_at`, `_reviewed_at`

### 3.3 APIs (functional today)

| Method | Path | Auth | Behavior |
|--------|------|------|----------|
| `GET` | `/api/v1/leaderboards/global` | ADMIN, EMPLOYEE | Paginated global rank by **count of approved certs** |
| `GET` | `/api/v1/leaderboards/initiatives/{initiativeId}` | ADMIN, EMPLOYEE | Paginated rank within initiative by **submittedAtUtc ASC** |
| `GET` | `/api/v1/leaderboards/me` | ADMIN, EMPLOYEE | Current user's global rank, cert count, recent approvals |

**Not implemented:** `/leaderboards/global/me`, `/leaderboards/initiatives/{id}/me` as separate endpoints (personal initiative rank absent).

### 3.4 Scoring logic (current)

There is **no points system**. Scoring is implicit:

| Leaderboard | "Score" | Source |
|-------------|---------|--------|
| Global | `totalApprovedCertifications` (long count) | `COUNT(approved submissions)` per employee |
| Initiative | Implicit rank position | One row per approved submission; earliest `submitted_at_utc` wins |

### 3.5 Ranking logic (current)

**Global** (`LeaderboardQueryRepository.findGlobalLeaderboard`):

1. Aggregate approved submissions per employee
2. `ROW_NUMBER()` ORDER BY:
   - `total_approved_certifications DESC`
   - `earliest_submitted_at_utc ASC` (tie-breaker)
   - `employee_user_id ASC` (deterministic)
3. **Competition ranking** (gaps on ties — standard `ROW_NUMBER`)

**Initiative** (`findInitiativeLeaderboard`):

1. Filter `approval_status = 'APPROVED'` AND `initiative_id = :id`
2. `ROW_NUMBER()` ORDER BY:
   - `submitted_at_utc ASC`
   - `reviewed_at_utc ASC`
   - `submission_id ASC`
3. Approval is eligibility only; **submission time is primary rank key**

**Personal** (`findGlobalRankingForEmployee`):

- Same global rules; returns `null` if employee has **zero** approved certifications (no rank)

### 3.6 Pagination, sorting, current-user behavior

- Uses Spring `Pageable` + `PageResponse<T>` wrapper
- Default page size: 20; default sort: `rank ASC`
- Whitelisted sort properties enforced in repository (throws on unsupported)
- **Current user outside page:** available via `GET /leaderboards/me` (global only)
- **Zero-score employees:** excluded from global leaderboard listing and personal rank (`null`)

### 3.7 Scheduled jobs / events / notifications

| Capability | Status |
|------------|--------|
| `@Scheduled` recalculation | **None** |
| Event-based score updates | **None** |
| Notification on rank change | **None** |
| Score ledger | **None** |

### 3.8 Security

- `@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")` on all service methods
- No role distinction between admin and employee for read access
- **Gap:** Initiative leaderboard does not call `LearningInitiative.isVisibleToEmployeesAt()` for employees

### 3.9 Tests (existing)

| Test class | Coverage |
|------------|----------|
| `LeaderboardQueryRepositoryTest` | SQL rules, sort whitelist, pagination params |
| `LeaderboardServiceTest` | Service delegation, personal ranking null case |
| `LeaderboardControllerTest` | PageResponse wrapping |
| `LeaderboardMethodSecurityTest` | ADMIN/EMPLOYEE access, unauthenticated denied |

---

## 4. Frontend audit

### 4.1 Routes (`AppRoutes.tsx`)

| Route | Component | Status |
|-------|-----------|--------|
| `/leaderboards/global` | `GlobalLeaderboardPage` | **Placeholder** (`PlaceholderPanel`) |
| `/leaderboards/initiatives/:initiativeId` | `InitiativeLeaderboardPage` | **Placeholder** |

Sidebar: `Leaderboards` → `/leaderboards/global` (all roles).

### 4.2 API client (`leaderboardsApi.ts`)

- `global()`, `initiative()`, `me()` implemented
- Types partially misaligned with backend `PersonalLeaderboardResponse`
- Unit test: initiative fetch only

### 4.3 Functional UI integrations (not placeholder)

| Surface | Integration |
|---------|-------------|
| **Dashboard** | `dashboardApi.ts` fetches global top 5 + `me()` for employee; fault-isolated via `Promise.allSettled` |
| **Initiative Detail** | `TopLearnerCard` — fetches `leaderboardsApi.initiative(id, { size: 1 })` |
| **Profile** | No leaderboard usage |

### 4.4 Missing UI (per backlog)

- Full global leaderboard table/podium
- Full initiative leaderboard page
- Top 3 learners on initiative detail
- Link from initiative detail → full leaderboard
- Period selector, score breakdown, current-user highlight on leaderboard pages

---

## 5. Score source audits

### 5.1 Certifications (`certificate_submissions`)

| Aspect | Finding |
|--------|---------|
| Lifecycle | SUBMITTED → APPROVED \| REJECTED |
| Duplicate prevention | `UNIQUE(employee_id, initiative_id)` — one submission slot per initiative |
| Resubmission after reject | **Blocked** — backlog item; no resubmit workflow |
| Certification type/category | **None** on submission — initiative is the only categorization |
| Difficulty level | **None** on submission or initiative |
| Expiry/revocation | **No** expiry or revocation model |
| Existing points | **Count-based** in global LB (1 row = 1 approved cert) |
| Reversal | No API to un-approve; rejection is terminal for that slot |
| Deletion | Submissions preserved; initiative delete blocked when submissions exist |

### 5.2 Learn (`learn_learning_enrollments`, `learn_stage_progress`)

| Aspect | Finding |
|--------|---------|
| Enrollment | One active enrollment per user per technology (partial unique index) |
| Stage completion | Sequential; `uk_learn_stage_progress_enrollment_stage` prevents duplicate stage rows |
| Roadmap completion | Derived when all stages complete → enrollment `status = COMPLETED`, `completed_at` set |
| Admin edit progress | **Forbidden** (BR-PR10) |
| Timestamps | `completed_at` on stage progress and enrollment — suitable for backfill |
| Double-count risk | Stage rows are independent per stage; roadmap completion is aggregate — **safe to award both** if keyed separately |
| Leaderboard integration today | **None** |

### 5.3 Initiatives (`learning_initiatives`)

| Aspect | Finding |
|--------|---------|
| Lifecycle | DRAFT → ACTIVE → EXPIRED (with return-to-draft, reactivate) |
| Participant model | **None** — participation = certificate submission |
| Contribution/tasks | **None** |
| Completion | Approved certificate submission per employee |
| Fair ranking data | **Sufficient for "first achiever" ranking** only |
| Manager-awarded points | **Not supported** |

### 5.4 Projects (P1–P4 complete)

| Data | Scoring recommendation |
|------|------------------------|
| `project_members` (access role, functional role, primary contact) | **Exclude** |
| `project_knowledge_access_events` | **Exclude** — passive navigation |
| `project_environments`, `project_repositories` | **Exclude** — link opens |
| Project creation/membership | **Exclude** — not employee achievement |

---

## 6. Recommended leaderboard purposes

### 6.1 Global Leaderboard

**Purpose:** Show verified cross-platform employee achievement for internal recognition.

**Phase L1 (existing APIs):** Rank by approved certification count — matches current backend.

**Phase L2 (after ledger approval):** Unified **points** from:

| Source | Include? | Rationale |
|--------|----------|-----------|
| Approved certifications | **Yes** | Verified, auditable, already in production |
| Learn stage completion | **Yes** | Verified self-reported progress with duplicate protection |
| Learn roadmap completion | **Yes** (bonus) | Meaningful milestone; separate idempotency key |
| Initiative participation (non-cert) | **No** | No domain model |
| Project activity | **No** | Passive / navigational |
| Study material downloads | **No** | Not verified achievement |
| Admin actions | **No** | Not employee achievement |

### 6.2 Initiative-Specific Leaderboard

**Purpose:** Rank employees who completed the initiative's certification requirement — **earliest verified completion first**.

**Uses existing data:** Approved `certificate_submissions` for the initiative.

**Does not require** participant tables or contribution points for v1.

**Future extension (optional):** If initiative managers need discretionary contribution points, requires new audited domain model (see Open Decisions).

---

## 7. UX architecture

### 7.1 Global Leaderboard page

Reuse patterns from `InitiativeListPage`, `UserListPage`, `DashboardListCard`.

```text
┌─────────────────────────────────────────────────────────────┐
│ PageHeader: Global Leaderboard                               │
│ Description: Verified achievement across the platform        │
├─────────────────────────────────────────────────────────────┤
│ [Global] [Initiative] tabs                                   │
├─────────────────────────────────────────────────────────────┤
│ Top Performers (top 3) — compact cards, not game podium      │
│  #1 Name · 12 pts · 8 certs · 4 learn milestones           │
├─────────────────────────────────────────────────────────────┤
│ Current user strip (if ranked): #14 You · 5 pts · breakdown  │
├─────────────────────────────────────────────────────────────┤
│ Ranked table: Rank | Avatar | Name | Points | Breakdown      │
│ Pagination (TablePaginationBar)                              │
└─────────────────────────────────────────────────────────────┘
```

**L1:** Points column shows `totalApprovedCertifications`; breakdown shows cert count only.  
**L2:** Add breakdown chips (Certs / Learn stages / Roadmaps).

**Period selector:** Omit in L1 (All Time only). Add Monthly in later phase if approved.

### 7.2 Initiative Leaderboard

**Recommended navigation: Option C (both entry points, shared component)**

| Entry | Path |
|-------|------|
| Leaderboards → Initiative tab | Select initiative from dropdown → `/leaderboards/initiatives/{id}` |
| Initiative Detail | "View Leaderboard" link → same route |

**Shared component:** `InitiativeLeaderboardView` used by `InitiativeLeaderboardPage` and embeddable in detail.

```text
┌─────────────────────────────────────────────────────────────┐
│ ← Back | Initiative Title                                    │
│ Ranked by earliest approved certification submission         │
├─────────────────────────────────────────────────────────────┤
│ Top 3 achievers (compact cards)                              │
├─────────────────────────────────────────────────────────────┤
│ Table: Rank | Employee | Submitted | Approved               │
│ Current user row highlighted if in list                      │
│ If not in page: fetch personal rank via future /me endpoint  │
└─────────────────────────────────────────────────────────────┘
```

**Professional tone:** Use `EmojiEventsOutlinedIcon` sparingly (already in `TopLearnerCard`); avoid XP/coins/levels.

### 7.3 Dashboard / Profile integration (L3)

| Surface | Enhancement |
|---------|-------------|
| Dashboard admin | Top Learners preview → link to `/leaderboards/global` |
| Dashboard employee | My Rank metric → link to global LB |
| Profile | Optional "Achievements" summary card using `/leaderboards/me` |

---

## 8. Permission model

| Actor | Global LB | Initiative LB | Manual score edit |
|-------|-----------|---------------|-------------------|
| ADMIN | View | View all initiatives | **No** — correct source records |
| EMPLOYEE | View | View if initiative **visible** (ACTIVE + date window) or admin | **No** |
| Non-participant employee | View global | View initiative LB for **visible** initiatives (read-only competition) | N/A |
| Initiative "owner" | N/A | **No owner role exists** — admin manages initiatives | N/A |

**Principle:** Scores derived from source-of-truth records. Corrections happen by fixing/updating source data (e.g., reject erroneous approval), not by editing leaderboard rows.

**L1 fix required:** Apply `LearningInitiativeService` visibility rules to initiative leaderboard endpoint for employees (404 when not visible).

---

## 9. Data architecture comparison

### Option A — Dynamic aggregation from source tables

| Pros | Cons |
|------|------|
| Always correct; no sync | Multi-source global queries become heavy UNIONs |
| Simple for cert-only | Reversal = re-query; harder audit trail |
| Already implemented for certs | Learn + cert union harder to index |

### Option B — Score/event ledger only

| Pros | Cons |
|------|------|
| Fast reads; full audit | Write path on every source event |
| Idempotency, reversal | Migration + backfill complexity |
| Monthly filters easy | Must keep ledger in sync |

### Option C — Hybrid (recommended for L2+)

```text
Source event (cert approve, stage complete, roadmap complete)
    → idempotent INSERT into leaderboard_score_events
    → aggregation query or materialized totals per user
    → ranking query (existing ROW_NUMBER patterns)
```

| Criterion | Option C |
|-----------|----------|
| Correctness | High — tied to source events |
| Auditability | `score_events` row per award with source reference |
| Duplicate prevention | `UNIQUE(source_type, source_id)` or equivalent |
| Reversal | Insert compensating negative event or mark voided |
| Backfill | One-time job reading historical source rows |
| Performance | Indexed aggregation at current scale |
| Complexity | Moderate — justified when Learn joins global |

**L1 recommendation:** Keep Option A for certs (no migration).  
**L2 recommendation:** Introduce Option C ledger when adding Learn to global scoring.

---

## 10. Proposed API contracts (no implementation)

### 10.1 Existing (keep in L1)

Documented in Swagger on `LeaderboardController`. Minor enhancements:

**`GET /api/v1/leaderboards/global`**

| Parameter | Type | Notes |
|-----------|------|-------|
| `page`, `size`, `sort` | standard | Unchanged |

**Response entry (L1):**

```json
{
  "rank": 1,
  "employee": { "id": "uuid", "employeeId": "EMP001", "fullName": "...", "email": "..." },
  "totalApprovedCertifications": 5,
  "earliestSubmittedAtUtc": "2026-01-15T10:00:00Z",
  "latestApprovedAtUtc": "2026-06-01T12:00:00Z"
}
```

**`GET /api/v1/leaderboards/initiatives/{initiativeId}`** — unchanged shape.

**`GET /api/v1/leaderboards/me`** — unchanged; fix frontend types to match.

### 10.2 Proposed additions (L2+)

**`GET /api/v1/leaderboards/global/me`** — Alias or enrich existing `/me` with:

```json
{
  "employee": { ... },
  "globalRank": 14,
  "totalPoints": 85,
  "breakdown": {
    "approvedCertifications": 5,
    "learnStagesCompleted": 6,
    "learnRoadmapsCompleted": 1
  },
  "recentAchievements": [ ... ]
}
```

**`GET /api/v1/leaderboards/initiatives/{initiativeId}/me`**

```json
{
  "initiativeId": "uuid",
  "initiativeTitle": "...",
  "rank": 3,
  "submittedAtUtc": "...",
  "approvedAtUtc": "...",
  "totalParticipants": 42
}
```

**Query parameters (future):**

| Param | Values | Phase |
|-------|--------|-------|
| `period` | `ALL_TIME` (default), `MONTHLY` | Post-L2 |
| `month` | `YYYY-MM` | When MONTHLY enabled |

---

## 11. Performance analysis

### Current scale assumptions

- Users: hundreds to low thousands (internal engineering platform)
- Approved certs: O(users × initiatives)
- Learn stage progress: O(users × stages)

### Expensive operations

| Query | Risk | Mitigation |
|-------|------|------------|
| Global cert aggregation | Low at current scale | Existing indexes on `approval_status`, `employee_id` |
| Window `ROW_NUMBER()` | Low | Paginated |
| Learn union (future) | Medium | Ledger with `idx_score_events_user_id`, `occurred_at` |
| Initiative LB | Low | `idx_certificate_submissions_initiative` + status |

### Indexes (future ledger — L2)

```sql
-- Illustrative only — not a migration
UNIQUE (source_type, source_id)  -- idempotency
INDEX (user_id, occurred_at DESC)
INDEX (user_id, voided_at) WHERE voided_at IS NULL
```

**Snapshots:** Not required at current scale. Revisit if global ranking query exceeds ~200ms p95.

---

## 12. Test strategy (for future implementation)

### Backend

| Area | Tests |
|------|-------|
| Cert scoring | Approved counts; rejected/submitted excluded |
| Learn scoring | Stage award; roadmap bonus; duplicate completion rejected |
| Ledger | Idempotent insert; backfill job |
| Reversal | Reject cert → global count decreases (L1: automatic via query) |
| Ranking | Ties, deterministic order, competition rank |
| Pagination | Page boundaries, sort whitelist |
| Current user | `/me` null vs ranked; initiative `/me` |
| Initiative isolation | Initiative A approvals not in Initiative B |
| Permissions | Employee 404 on hidden initiative LB |
| Visibility | MEMBERS_ONLY N/A; initiative date/status visibility |

### Frontend

| Area | Tests |
|------|-------|
| Global page | Load, table, top 3, pagination, highlight |
| Initiative page | Selector, table, empty/error states |
| Tabs | Global ↔ Initiative navigation |
| Dashboard links | Drilldown to leaderboard |
| Types | API response mapping |

### E2E

1. Approve cert → global rank updates
2. Complete learn stage → points update (L2)
3. Repeat complete → no duplicate points
4. Reject approved cert (when un-approve exists) or verify rejection exclusion
5. Initiative isolation
6. Restart backend → persistence
7. Regression: Learn, Certifications, Initiatives, Projects unchanged

---

## 13. Implementation phase plan

Each phase must complete the **11-step workflow** in `docs/development-workflow.md`.

### L1 — Global & Initiative Leaderboard UI (cert-based, no migration)

| Scope | Items |
|-------|-------|
| Database | None |
| Backend | Initiative visibility check for employees; optional OpenAPI tweaks |
| API | Use existing three endpoints; align frontend types |
| Frontend | `GlobalLeaderboardPage`, `InitiativeLeaderboardPage`, shared table/top-3 components, tabs, links from Dashboard + Initiative Detail |
| Tests | Page tests, API type tests, backend visibility test |
| E2E | Approve cert → see rank on global + initiative pages |
| Migration risk | **None** |
| Regression | Dashboard, Initiative Detail, Cert workflow, Learn |
| Manual QA | Both roles; empty LB; pagination; top learner card still works |

### L2 — Learn integration + unified global points (ledger)

| Scope | Items |
|-------|-------|
| Database | `leaderboard_score_events` (+ optional `leaderboard_user_totals`) — **V20** |
| Backend | Event writers in `CertificateSubmissionService.approve`, `LearningProgressService.completeStage`; backfill job; extended global query |
| API | Enriched global entries with `totalPoints` + `breakdown`; optional `period` param |
| Frontend | Breakdown column/chips on global LB |
| Tests | Ledger idempotency, Learn scoring, backfill integration test |
| E2E | Stage complete → points; no duplicate on retry |
| Migration risk | **Medium** — backfill on deploy |
| Regression | Learn progress, cert approval, existing LB ranks for cert-only users |

### L3 — Dashboard/Profile polish + initiative personal rank

| Scope | Items |
|-------|-------|
| Database | None (unless `/initiatives/{id}/me` needs snapshot) |
| Backend | `GET /leaderboards/initiatives/{id}/me` |
| Frontend | Dashboard drilldowns; Profile achievements card; top 3 on initiative detail |
| Tests | Drilldown navigation; profile card |
| E2E | Full user journey across modules |
| Migration risk | Low |
| Regression | All L1+L2 surfaces |

---

## 14. Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Rejected cert blocks resubmit | Medium | F21/resubmit workflow may change initiative LB; document |
| No un-approve API | Medium | Rejection only before re-review; ledger void in L2 |
| Doc/code drift | Low | Update `.cursor/architecture.md` in L1 |
| Learn + cert perceived unfairness | Medium | Transparent breakdown; tune point values |
| Initiative LB UUID guessing | Low | Visibility check in L1 |
| Scope creep (gamification) | Medium | Explicit non-goals in scoring doc |

---

## 15. Open decisions requiring approval

1. **Include Learn in global leaderboard for L2?** (Recommended: yes, with staged + roadmap bonus.)
2. **Point values** — see `scoring-model-proposal.md`; needs product sign-off.
3. **Monthly period** — defer or include post-L2?
4. **Initiative manager contribution points** — defer (no data model)?
5. **Resubmission after reject** — dependency for initiative participation fairness.
6. **Certification difficulty weighting** — defer until F20 Certification Catalog exists.
7. **L1 cert-count vs L2 points** — show as "certifications" in L1, rename to "points" in L2 with migration messaging?

---

## 16. Files reviewed (production code)

### Backend

- `leaderboard/controller/LeaderboardController.java`
- `leaderboard/service/LeaderboardService.java`
- `leaderboard/repository/LeaderboardQueryRepository.java`
- `leaderboard/dto/*`
- `submission/service/CertificateSubmissionService.java`
- `submission/domain/CertificateSubmission.java`, `ApprovalStatus.java`
- `initiative/domain/LearningInitiative.java`
- `learn/service/LearningProgressService.java`
- `db/migration/V4__create_certificate_submissions.sql`
- `db/migration/V15__learn_progress.sql`
- `db/migration/V3__create_learning_initiatives.sql`
- Tests: `LeaderboardQueryRepositoryTest`, `LeaderboardServiceTest`, `LeaderboardControllerTest`, `LeaderboardMethodSecurityTest`

### Frontend

- `pages/leaderboards/GlobalLeaderboardPage.tsx`, `InitiativeLeaderboardPage.tsx`
- `api/leaderboardsApi.ts`, `dashboardApi.ts`
- `pages/dashboard/DashboardPage.tsx`
- `pages/initiatives/InitiativeDetailPage.tsx`
- `components/initiatives/TopLearnerCard.tsx`
- `routes/AppRoutes.tsx`, `layout/navigation.tsx`

### Migrations

- V1–V19 inspected via glob; leaderboard-relevant: V3, V4, V15

---

## 17. Confirmation

- Repository context was read before architecture analysis
- Architecture review only — **no production code changed** (documentation only)
- **No Flyway migration created**
- **No API changed**
- **No frontend behavior changed**
- **No leaderboard implementation started**
