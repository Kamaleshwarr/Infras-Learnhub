# Leaderboard Scoring Model Proposal

**Status:** Proposal — awaiting approval  
**Applies to:** Global Leaderboard (L2+); Initiative Leaderboard (L1 uses submission order, not points)

---

## Design principles

1. **Understandable** — employees can explain how they earned recognition
2. **Verifiable** — every point ties to a source-of-truth record
3. **Non-gameable** — no passive activity, navigation, or membership farming
4. **Idempotent** — repeating the same achievement never double-awards
5. **No gamification expansion** — no badges, streaks, XP levels, coins, or leaderboards-as-game

---

## Phase alignment

| Phase | Model |
|-------|-------|
| **L1** | Global: **approved certification count** (existing). Initiative: **submission-time rank** (existing). |
| **L2** | Global: **unified points** via score ledger. Initiative: unchanged (completion order). |

---

## Score sources (L2 global)

### 1. Approved certification

| Attribute | Value |
|-----------|-------|
| **Source event** | `CertificateSubmission` transitions to `APPROVED` |
| **Points** | **10** per approved certification (proposed; equals weight of one roadmap bonus) |
| **Repeatable** | Once per initiative per employee (`UNIQUE(employee_id, initiative_id)`) |
| **Frequency limit** | One per initiative |
| **Verification** | Admin approval required |
| **Duplicate prevention key** | `(CERTIFICATION, submission_id)` |
| **Reversal** | If approval could be reversed: void ledger row. **Today:** no un-approve API — rejection only from SUBMITTED |
| **Backfill** | `SELECT id, employee_id, reviewed_at_utc FROM certificate_submissions WHERE approval_status = 'APPROVED'` |

**Answers:**

- **Award once?** Yes — one submission per initiative.
- **Difficulty affect points?** **No** in L2 — no difficulty on submissions. Revisit when F20 Certification Catalog ships.
- **Admin actions counted?** No — only employee submissions that pass review.

---

### 2. Learn stage completion

| Attribute | Value |
|-----------|-------|
| **Source event** | `LearnStageProgress` row created via `completeStage` |
| **Points** | **2** per stage completed |
| **Repeatable** | Once per stage per enrollment (`uk_learn_stage_progress_enrollment_stage`) |
| **Frequency limit** | One per catalog stage per active/completed enrollment |
| **Verification** | Employee self-reported sequential completion (platform rule) |
| **Duplicate prevention key** | `(LEARN_STAGE, stage_progress_id)` |
| **Reversal** | No admin delete of progress (BR-PR10). If enrollment deleted (CASCADE): void related events |
| **Backfill** | `SELECT id, enrollment.user_id, completed_at FROM learn_stage_progress` |

**Should stages award points?** **Yes** — rewards incremental learning.

---

### 3. Learn roadmap completion (bonus)

| Attribute | Value |
|-----------|-------|
| **Source event** | Enrollment `status` → `COMPLETED` (all stages done) |
| **Points** | **10** one-time bonus per technology roadmap |
| **Repeatable** | Once per enrollment completion |
| **Frequency limit** | One per `(user_id, technology_slug)` per completed enrollment |
| **Verification** | Derived from stage progress completeness |
| **Duplicate prevention key** | `(LEARN_ROADMAP, enrollment_id)` |
| **Reversal** | Enrollment `LEFT` does not revoke completed history; no un-complete API |
| **Backfill** | `SELECT id, user_id, completed_at FROM learn_learning_enrollments WHERE status = 'COMPLETED'` |

**Should roadmap award bonus?** **Yes** — recognizes full journey without replacing stage points.

**Double counting?** **No** — stage points reward steps; roadmap bonus rewards finishing. Different keys. Example: 5-stage roadmap = 5×2 + 10 = **20 points** total.

---

## Explicitly excluded sources

| Source | Reason |
|--------|--------|
| Project membership (`project_members`) | Passive assignment |
| Project access role / functional role / primary contact | Organizational metadata |
| Opening knowledge base resources (`project_knowledge_access_events`) | Navigation, not achievement |
| Opening environment/repo links | Navigation |
| Study material downloads | Not verified learning |
| Learn enrollment without stage completion | Intent only |
| Initiative DRAFT/EXPIRED visibility | Not employee-visible achievement |
| Notification reads, login counts | Activity metrics |
| Admin curation (publish tech, approve others' certs) | Admin duty, not personal achievement |
| Rejected or pending certifications | Not verified |

**Projects:** Explicitly excluded from all leaderboard scoring in this version.

---

## Initiative leaderboard scoring (L1 — not points-based)

Initiative ranking uses **completion order**, not points:

| Measure | Rule |
|---------|------|
| Rank key | `submitted_at_utc ASC` among APPROVED submissions |
| Tie-breaker 1 | `reviewed_at_utc ASC` |
| Tie-breaker 2 | `submission_id ASC` |
| Points column | **N/A** — show Submitted / Approved timestamps |

**Rationale:** Matches existing backend, initiative reward model (certification race), and available data.

**Initiative manager contribution points?** **Not in v1** — no audited contribution model. Smallest safe extension if needed later:

```text
initiative_contribution_awards (
  id, initiative_id, employee_id, points, reason, awarded_by, awarded_at
)
```

Defer until product requires discretionary scoring.

---

## Duplicate prevention strategy

### L1 (cert-only dynamic SQL)

- Database: `UNIQUE(employee_id, initiative_id)` on submissions
- Query: `COUNT` / `ROW_NUMBER` over `approval_status = 'APPROVED'`

### L2 (ledger)

```text
leaderboard_score_events
├── id (UUID PK)
├── user_id (FK users)
├── source_type (CERTIFICATION | LEARN_STAGE | LEARN_ROADMAP)
├── source_id (UUID — submission_id, stage_progress_id, enrollment_id)
├── points (INT — positive award)
├── occurred_at (TIMESTAMPTZ — from source record)
├── voided_at (TIMESTAMPTZ NULL)
├── created_at
└── UNIQUE (source_type, source_id) WHERE voided_at IS NULL
```

**Award path:** Insert on source event; `ON CONFLICT DO NOTHING` or catch unique violation.

**Backfill:** Same insert logic in one-time `ApplicationRunner` or admin-triggered job; idempotent.

---

## Reversal and correction strategy

| Scenario | L1 behavior | L2 proposed behavior |
|----------|-------------|----------------------|
| Cert rejected from SUBMITTED | Never ranked | N/A |
| Cert was APPROVED, need undo | **No API today** | Set `voided_at` on ledger row; totals recalculate |
| Learn stage wrongly completed | No admin undo | Not supported (BR-PR10) |
| Source record deleted | CASCADE may remove learn rows | Void events on CASCADE |
| Admin "fix" leaderboard | **Forbidden** | Correct source record or void event |

**Preferred principle:** Never direct score editing.

---

## Historical backfill strategy

| Source | Backfillable? | User action required? |
|--------|---------------|----------------------|
| Approved certifications | **Yes** — all historical APPROVED rows | None |
| Learn stage progress | **Yes** — all `learn_stage_progress` rows | None |
| Completed roadmaps | **Yes** — enrollments with `status = COMPLETED` | None |
| Initiative LB | **Yes** — existing approved submissions | None |

**Deploy L2:** Run idempotent backfill after migration. Employees **do not** redo achievements.

**Ordering:** Use source timestamps (`reviewed_at_utc`, `completed_at`) for `occurred_at` to preserve historical fairness.

---

## Ranking and tie rules

### Global (L1 — cert count)

| Rule | Value |
|------|-------|
| Primary sort | `total_approved_certifications DESC` |
| Tie-breaker 1 | `earliest_submitted_at_utc ASC` |
| Tie-breaker 2 | `employee_user_id ASC` |
| Ranking style | **Competition ranking** (`ROW_NUMBER` — ties get same number, next rank skips) |
| Zero score | Excluded from list; `/me` returns `globalRank: null` |

### Global (L2 — points)

| Rule | Value |
|------|-------|
| Primary sort | `total_points DESC` |
| Tie-breaker 1 | `earliest_achievement_at ASC` |
| Tie-breaker 2 | `user_id ASC` |
| Ranking style | Competition ranking |
| Zero score | Excluded |

### Initiative (L1 — unchanged)

| Rule | Value |
|------|-------|
| Primary sort | `submitted_at_utc ASC` |
| Tie-breaker 1 | `approved_at_utc ASC` |
| Tie-breaker 2 | `submission_id ASC` |
| Ranking style | Competition ranking |
| Zero score | No approved submission → not listed |

### Pagination

- Default `size = 20`, max recommend 100
- Default sort: `rank,asc`

### Current user outside visible page

| Endpoint | L1 | L2 |
|----------|----|----|
| Global | `GET /leaderboards/me` | Enriched `/me` or `/global/me` |
| Initiative | Not implemented | `GET /leaderboards/initiatives/{id}/me` |

---

## Time period recommendation

| Option | Recommendation |
|--------|----------------|
| **A: All Time only** | **✓ L1 and L2 initial release** |
| **B: All Time + Monthly** | Defer — requires `occurred_at` filtering on ledger; product decision |

**Rationale:** Existing data supports All Time. Monthly adds UX and QA scope without strong current requirement. Cert `reviewed_at_utc` and Learn `completed_at` enable Monthly later without schema redesign if ledger includes `occurred_at`.

---

## Point budget reference (illustrative)

| Achievement | Points |
|-------------|--------|
| 1 approved certification | 10 |
| 1 Learn stage | 2 |
| 1 Learn roadmap completion | 10 (bonus) |

**Typical active employee (illustrative):** 3 certs + 2 roadmaps (10 stages each) = 30 + 40 + 20 = **90 points**.

Values are **proposed** — adjust before L2 implementation.

---

## Activities explicitly excluded (summary)

- Project membership and roles
- Knowledge / environment / repository navigation
- Study material downloads
- Enrollment without completion
- Pending/rejected certifications
- Gamification mechanics (badges, streaks, levels, coins)
- Manager discretionary points (until domain model exists)

---

## Approval checklist

- [ ] L1 cert-count global + submission-order initiative approved
- [ ] L2 Learn inclusion approved
- [ ] Point values approved
- [ ] Ledger schema (Option C) approved
- [ ] Monthly period deferral confirmed
- [ ] Project exclusion confirmed
