# Project Module — Architecture Options Comparison

**Date:** 2026-07-06

Three realistic options evaluated against product requirements.

---

## Option A — Generic folder + generic item model

### Description

Single `project_knowledge_folders` hierarchy and single `project_knowledge_items` table with a large `resource_type` enum. Repositories, environments, and credentials are **just items** in special folders (e.g., folder "Environments/QA").

### Structure

```text
Project → Folder (any depth) → Item (typed enum)
```

### Evaluation

| Criterion | Score | Notes |
|-----------|-------|-------|
| Complexity | ★★☆☆☆ Low initial | Grows messy as types diverge |
| Maintainability | ★★☆☆☆ | Validation switches on enum; god service |
| Queryability | ★★☆☆☆ | "All repos" = filter folder path — fragile |
| Validation | ★★☆☆☆ | Hard to enforce env-specific rules |
| Frontend complexity | ★★★☆☆ | One component, many conditionals |
| Search | ★★☆☆☆ | Mixed types in one index |
| Permissions | ★★★★☆ | Uniform |
| AI compatibility | ★★☆☆☆ | Ambiguous structure |
| Email events | ★★★☆☆ | Generic item events only |
| Migration from current | ★★★★★ | Minimal change |

---

## Option B — Strongly typed separate entities

### Description

Independent tables: `repositories`, `environments`, `environment_urls`, `documents`, `requirements`, `credential_refs`, etc. No generic item table.

### Structure

```text
Project → Repository[]
Project → Environment[] → EnvironmentUrl[]
Project → Requirement[]
Project → ...
```

### Evaluation

| Criterion | Score | Notes |
|-----------|-------|-------|
| Complexity | ★★★★☆ High | Many entities, many APIs |
| Maintainability | ★★★☆☆ | Clear per domain, but verbose |
| Queryability | ★★★★★ | Excellent per type |
| Validation | ★★★★★ | Strong per entity |
| Frontend complexity | ★★★★☆ | Many screens/services |
| Search | ★★★★☆ | Union queries across tables |
| Permissions | ★★★☆☆ | Repeat checks per service |
| AI compatibility | ★★★★★ | Very explicit schema |
| Email events | ★★★★★ | Typed events per entity |
| Migration from current | ★★☆☆☆ | Large refactor |

---

## Option C — Hybrid model (recommended)

### Description

**First-class entities** for highly structured, frequently queried concepts:

- `project_environments` + `project_environment_references`
- `project_repositories`
- `project_credential_references`

**Generic `project_resources`** (evolved from `project_knowledge_items`) for navigational links and documents in folders.

**Fixed portal sections** in UI map to routes, not only folders.

### Structure

```text
Project
  ├── Repositories (typed table)
  ├── Environments (typed table)
  │     └── References (typed table)
  ├── Credential References (typed table)
  └── Knowledge Base
        └── Folders (max 2 levels UX)
              └── Resources (generic, typed enum)
```

### Evaluation

| Criterion | Score | Notes |
|-----------|-------|-------|
| Complexity | ★★★☆☆ Moderate | Balanced surface area |
| Maintainability | ★★★★☆ | Clear boundaries |
| Queryability | ★★★★☆ | Typed for ops; generic for docs |
| Validation | ★★★★☆ | Strong where needed |
| Frontend complexity | ★★★☆☆ | Section-based pages + KB browser |
| Search | ★★★★☆ | Index typed + generic tables |
| Permissions | ★★★★☆ | Reuse project role checks |
| AI compatibility | ★★★★☆ | Structured env/repo; typed resources |
| Email events | ★★★★☆ | Typed events per entity kind |
| Migration from current | ★★★★☆ | Extend items; add tables |

---

## Side-by-side summary

| Criterion | A Generic | B Typed | C Hybrid |
|-----------|-----------|---------|----------|
| Time to MVP portal | Fast | Slow | **Medium** |
| Long-term fit | Poor | Excellent | **Strong** |
| Reuse existing V6 | High | Low | **High** |
| Environment UX | Weak | Strong | **Strong** |
| Flexibility for odd links | High | Low | **High** |
| Sunk cost risk | Low | High | **Low** |

---

## Folder nesting decision (cross-cutting)

| Option | MVP nesting |
|--------|-------------|
| A | Often unlimited — tends toward explorer UX |
| B | N/A or minimal folders |
| **C** | **2-level KB folders** + typed top sections |

---

## Document handling by option

| Option | MVP docs | Future files |
|--------|----------|--------------|
| A | Items in folders | Existing file upload |
| B | Document entity | Separate upload entity |
| **C** | **Link resources** | Reuse item file path optionally |

---

## Recommendation preview

**Option C — Hybrid** is the recommended architecture. See [08-recommended-architecture.md](./08-recommended-architecture.md).

### Why not A?

Closest to current code, but environments and repositories are **first-class operational concepts** — forcing them into folder paths does not survive search, AI, or email event requirements.

### Why not B?

Over-engineered for a navigation hub. Most content is a titled URL — a generic resource with type enum is sufficient.

### Why C?

Matches product mental model (Overview → Environments → Repos → Knowledge Base), reuses V6 folder/items for flexible docs, and structures operational data for search and future automation.
