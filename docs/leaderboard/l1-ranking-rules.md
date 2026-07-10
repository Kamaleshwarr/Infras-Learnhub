# Leaderboard L1 Ranking Rules

**Status:** Implemented in L1 (certification-based only)

## Global Leaderboard

### SQL function

`ROW_NUMBER()` — **not** `RANK()` or `DENSE_RANK()`.

### Semantics

**Unique sequential positions with deterministic tie-breakers.**

Employees with the same approved certification count receive **different** rank numbers. The employee with the earlier `earliest_submitted_at_utc` receives the higher position.

| Order | Column | Direction |
|-------|--------|-----------|
| 1 | `total_approved_certifications` | DESC |
| 2 | `earliest_submitted_at_utc` | ASC |
| 3 | `employee_user_id` | ASC |

### Example

| Employee | Approved Certs | Earliest Submission | Rank |
|----------|----------------|---------------------|------|
| Alice | 5 | 2026-03-01 | 1 |
| Bob | 5 | 2026-04-01 | 2 |
| Carol | 4 | 2026-01-01 | 3 |

Bob and Alice have the same count, but Alice ranks higher because she reached that count first.

### Zero achievements

Employees with zero approved certifications:

- Are **excluded** from `GET /leaderboards/global` results
- Receive `globalRank: null` from `GET /leaderboards/me`

### Why not RANK()?

`RANK()` would assign the same rank number to tied employees (e.g., both #1) and skip the next position (#3). The existing production SQL uses `ROW_NUMBER()` with tie-breakers. L1 preserves this behavior to avoid silent semantic changes.

---

## Initiative Leaderboard

### SQL function

`ROW_NUMBER()` over approved submissions within one initiative.

### Semantics

**Completion order by certification submission time.**

Approval is eligibility only. The primary ranking timestamp is `submitted_at_utc`.

| Order | Column | Direction |
|-------|--------|-----------|
| 1 | `submitted_at_utc` | ASC |
| 2 | `reviewed_at_utc` (approved) | ASC |
| 3 | `submission_id` | ASC |

### Business meaning

The employee who **submitted their approved certification earliest** ranks highest. This reflects initiative completion speed, not admin approval speed.

### Constraints

- One approved submission per employee per initiative (`UNIQUE(employee_id, initiative_id)`)
- Only `approval_status = 'APPROVED'` rows are ranked

---

## Time scope

**All Time only** in L1. No weekly, monthly, or quarterly filters.

---

## Deferred (L2+)

- Unified points model
- Learn stage / roadmap scoring
- Monthly period filtering
