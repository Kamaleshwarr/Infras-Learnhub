# Learn Module Documentation

**Version:** 1.1 (F16–F18 + resource overrides)  
**Status:** Shipped — catalog-first learning navigation with employee-owned progress

The Learn module is a **Learning Navigation Platform**. The platform owns guidance (catalog, roadmaps, progress); external providers own content (videos, docs, labs).

## Documentation index

| Document | Description |
|----------|-------------|
| [Database overview](database-overview.md) | ER diagram, tables, relationships |
| [API reference](api-reference.md) | Every Learn REST endpoint |
| [Catalog](catalog.md) | manifest, packages, import, versioning |
| [Contributing](../contributing.md) | How to add technologies, roadmaps, migrations |
| [Testing guide](../testing-guide.md) | Backend, frontend, integration, QA |
| [Development workflow](../development-workflow.md) | Mandatory 11-step feature workflow |

## Product design (frozen)

| Document | Location |
|----------|----------|
| Product vision | `docs/v0.8.0/00-product-design.md` |
| Architecture revision v2 | `docs/v0.8.0/08-navigation-platform-revision.md` |
| Catalog specification | `docs/v0.8.0/10-catalog-specification.md` |
| Implementation plan v2 | `docs/v0.8.0/09-implementation-plan-v2.md` |

## Phase summary

| Phase | Status | Scope |
|-------|--------|-------|
| F16 | ✓ Complete | Technology discovery, search, detail, Projects cross-nav |
| F16-R | ✓ Complete | Catalog foundation, import pipeline, admin curation |
| F17 | ✓ Complete | Roadmap viewer (read-only catalog roadmaps) |
| F18 | ✓ Complete | Enrollments, sequential stage progress, Continue Learning |
| Learn v1.1 | ✓ Complete | Organization resource overrides (URL, disable, restore, org-only) |
| F19+ | Not started | Career paths, certifications, initiatives overlay |

## Key principles

1. **Catalog-owned roadmaps** — Roadmap content is imported from JSON; never edited via admin UI.
2. **Progress overlay** — Employee progress references catalog stage IDs; catalog rows are not mutated.
3. **Employee-owned journey** — Each user has independent enrollments; admins cannot edit progress.
4. **Sequential progression** — Stages complete in order only.
5. **Curation, not authoring** — Admins publish, hide, feature, link projects, and override resource URLs; they do not write roadmap structure or edit catalog metadata.
6. **Effective resources** — Employees receive merged catalog + override resources transparently.

## Implementation reports

| Phase | Report |
|-------|--------|
| F16 | `docs/releases/release-v0.8.0-f16-implementation-report.md` |
| F16-R | `docs/releases/release-v0.8.0-f16-r-implementation-report.md` |
| F17 | `docs/releases/release-v0.8.0-f17-implementation-report.md` |
| F18 | `docs/releases/release-v0.8.0-f18-implementation-report.md` |
| Learn v1.1 | `docs/releases/release-v0.8.0-learn-v11-resource-overrides-report.md` |

**UI polish (resource override dialog):** `docs/screenshots/learn-v11-resource-overrides-ui-polish/`
