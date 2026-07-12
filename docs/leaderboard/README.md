# Leaderboard Module

**Status:** L1 complete (2026-07-10) — **awaiting manual QA approval**  
**Flyway:** V19 (no leaderboard migration in L1)

## Purpose

1. **Global Leaderboard** — employees ranked by approved certification count (all time)
2. **Initiative Leaderboard** — employees ranked by earliest approved certification submission within one initiative

## Documents

| Document | Contents |
|----------|----------|
| [architecture-review.md](./architecture-review.md) | Original audit and phased plan |
| [scoring-model-proposal.md](./scoring-model-proposal.md) | L2+ scoring proposal (**deferred**) |
| [l1-ranking-rules.md](./l1-ranking-rules.md) | L1 ranking semantics (`ROW_NUMBER` + tie-breakers) |
| [../releases/release-leaderboard-l1-implementation-report.md](../releases/release-leaderboard-l1-implementation-report.md) | L1 implementation report |

## L1 scope (shipped)

| Area | Detail |
|------|--------|
| Scoring | **Approved certifications only** — no points abstraction |
| Backend | Existing `/api/v1/leaderboards/*` + initiative visibility fix |
| Frontend | Global page, Initiative page (picker + view), Dashboard/Detail integration |
| Database | **No migration** |
| Time period | All Time only |

## Deferred (L2+)

Learn stage/roadmap points, score ledger, monthly periods, manager contribution points, project scoring, gamification.

## Routes

| Route | Page |
|-------|------|
| `/leaderboards/global` | Global Leaderboard |
| `/leaderboards/initiatives` | Initiative picker |
| `/leaderboards/initiatives/:initiativeId` | Initiative Leaderboard |
