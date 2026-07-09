# Project Module — Architecture & Planning

**Status:** P3 complete — Environments & Repositories shipped; P4–P5 not started  
**Last updated:** 2026-07-09  
**Baseline:** Learn module v1.1 complete (v0.8.0); Project P1 + P2 + P3 complete

The Project Module will become the **internal project knowledge and navigation hub** for engineering teams. The platform owns **organization and navigation**; external systems continue to own source content.

> **Engineering Learning Hub owns guidance and navigation, not knowledge.**

---

## Documents

| # | Document | Purpose |
|---|----------|---------|
| 1 | [01-current-state-audit.md](./01-current-state-audit.md) | What exists today in backend, frontend, DB, tests |
| 2 | [02-gap-analysis.md](./02-gap-analysis.md) | Current vs target — KEEP / EXTEND / REFACTOR / REMOVE / NEW |
| 3 | [03-product-architecture-proposal.md](./03-product-architecture-proposal.md) | Product goals, core experience, overview, folders, documents |
| 4 | [04-data-model-proposal.md](./04-data-model-proposal.md) | Recommended schema — environments, items, credentials |
| 5 | [05-permissions-matrix.md](./05-permissions-matrix.md) | Role and project-membership permissions |
| 6 | [06-ux-information-architecture.md](./06-ux-information-architecture.md) | Screen IA and navigation design |
| 7 | [07-architecture-options-comparison.md](./07-architecture-options-comparison.md) | Options A/B/C comparison |
| 8 | [08-recommended-architecture.md](./08-recommended-architecture.md) | Final architecture decision |
| 9 | [09-phased-implementation-plan.md](./09-phased-implementation-plan.md) | P1–P5 phases with exit criteria |
| 10 | [10-risks-and-open-decisions.md](./10-risks-and-open-decisions.md) | Risks, sunk cost, decisions needing approval |
| 11 | [11-technology-integration-impact.md](./11-technology-integration-impact.md) | Project ↔ Technology cross-navigation |
| 12 | [12-future-compatibility.md](./12-future-compatibility.md) | Email notifications and AI Assistant hooks |

---

## Executive summary

| Area | Today | Target |
|------|-------|--------|
| Backend | Full **Project Knowledge Repository** API (V6) | Extend with overview metadata, environments, repositories, credential refs, search |
| Frontend | **P1:** Projects list + overview portal · **P2:** Knowledge Base folders + external links · **P3:** Environments + Repositories | P4+ cross-project search |
| Database | 8 project tables + Learn junction + `projects.status` (V17) + operational tables (V18) | Add review metadata (P5) |
| Learn integration | Admin-managed tech ↔ project links | Reuse junction; enrich both directions |
| Initiatives | No relationship | Document future M:N association only |
| File upload | Supported (local storage) | MVP: links only; retain file infra for later |

**Recommended architecture:** **Hybrid (Option C)** — first-class entities for Environments, Repositories, and Credential References; generic `ProjectResource` items for navigational links and documents.

**Next step:** **P4 — Project Search & Cross-Navigation** (not started). See [p3-implementation-report.md](./p3-implementation-report.md).

All implementation phases must pass the mandatory [11-step development workflow](../development-workflow.md).
