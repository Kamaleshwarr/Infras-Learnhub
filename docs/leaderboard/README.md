# Leaderboard Module — Architecture Review

**Status:** Architecture review complete — **awaiting manual approval before implementation**  
**Review date:** 2026-07-10  
**Flyway baseline at review:** V19 (`project_team_and_contacts`)

## Purpose

This folder documents the architecture review and implementation plan for:

1. **Global Leaderboard** — verified employee achievement across the platform
2. **Initiative-Specific Leaderboard** — ranking within one learning initiative

## Documents

| Document | Contents |
|----------|----------|
| [architecture-review.md](./architecture-review.md) | Current-state audit, gaps, doc/code mismatches, UX/permission/data architecture, APIs, performance, tests, phases, risks |
| [scoring-model-proposal.md](./scoring-model-proposal.md) | Score sources, exclusions, duplicate prevention, reversal, backfill, ranking rules |

## Executive summary

### What already exists (production code)

- **Backend leaderboard module is shipped** (v0.2 era): three read APIs under `/api/v1/leaderboards` ranking **approved certificate submissions only**
- **No leaderboard tables**, score ledger, scheduled jobs, or notification producers
- **Ranking is dynamic SQL** against `certificate_submissions` via `LeaderboardQueryRepository`
- **Frontend leaderboard pages are placeholders**; Dashboard and Initiative Detail already consume APIs

### Recommended direction (pending approval)

| Area | Recommendation |
|------|----------------|
| **L1 (first implementation phase)** | Build Global + Initiative UI on **existing cert-based APIs** — no schema change |
| **L2** | Add **hybrid score ledger** (Option C) for Learn achievements; extend global ranking to multi-source points |
| **Initiative leaderboard** | Keep **submission-order ranking** (earliest approved submission wins) — matches current backend and initiative model |
| **Learn in global** | Stage points + one-time roadmap bonus; idempotent ledger rows keyed by source |
| **Projects** | **Exclude** from scoring (membership, KB access, env/repo navigation) |
| **Time scope** | **All Time only** in first release; defer Monthly until product need is confirmed |
| **Permissions** | All authenticated users (ADMIN + EMPLOYEE) view leaderboards; add initiative visibility checks |

### Explicitly not in scope for this review

- Implementation code or Flyway migrations
- Email notifications, AI assistant, Learn v2 expansion, final UI polish beyond leaderboard pages

## Related documentation

- `.cursor/architecture.md` — system patterns (custom SQL for leaderboards noted)
- `docs/project-roadmap.md` — backlog item: initiative leaderboard full page UI
- `docs/v0.8.0/03-business-rules.md` — Learn leaderboards explicitly deferred from v0.8.0
- `docs/development-workflow.md` — mandatory 11-step completion checklist for future phases

## Approval gate

**Do not begin leaderboard implementation until this architecture review is manually approved.**
