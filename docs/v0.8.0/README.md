# Engineering Learning Hub — v0.8.0

**Release theme:** Learn — Learning Navigation Platform  
**Product philosophy:** *Engineering Learning Hub owns guidance, not knowledge.*  
**Product design:** Approved v1.1 (frozen — vision unchanged)  
**Architecture revision v2:** **APPROVED — FROZEN** ([08-navigation-platform-revision.md](./08-navigation-platform-revision.md))  
**Catalog specification:** **APPROVED — FROZEN** ([10-catalog-specification.md](./10-catalog-specification.md))  
**Implementation plan v2:** **FINAL — FROZEN** ([09-implementation-plan-v2.md](./09-implementation-plan-v2.md))  
**Depends on:** v0.7.1 Initiative Management (complete); F16 shipped

---

## Document index

| # | Document | Contents | Status |
|---|----------|----------|--------|
| 00 | [Product Design](./00-product-design.md) | Approved product architecture | Frozen |
| 01 | [Product Vision](./01-product-vision.md) | Vision, principles, positioning | Frozen |
| 02 | [Information Architecture](./02-information-architecture.md) | Navigation, URLs, entities | Frozen |
| 03 | [Business Rules](./03-business-rules.md) | Authoritative business rules | Frozen |
| 04 | [User Flows](./04-user-flows.md) | Employee and admin journeys | Frozen |
| 05 | [Implementation Roadmap (summary)](./05-roadmap.md) | Phase overview | Frozen |
| 06 | [Future Enhancements](./06-future-enhancements.md) | AI and deferred features | Frozen |
| 07 | [Implementation Plan v1](./07-implementation-plan.md) | F16–F23 (superseded) | Superseded |
| 08 | [Navigation Platform Revision](./08-navigation-platform-revision.md) | Architecture revision v2 | **Frozen** |
| 09 | [Implementation Plan v2](./09-implementation-plan-v2.md) | F16-R–F22 | **Frozen** |
| **10** | **[Catalog Specification](./10-catalog-specification.md)** | **Directory, schemas, import lifecycle** | **Frozen** |

---

## Implementation phases

### Shipped

| Phase | Name | Status |
|-------|------|--------|
| F16 | Technology Discovery & Search | **Shipped** (refactor in F16-R) |

### Active roadmap (frozen)

| Phase | Name | Status |
|-------|------|--------|
| **F16-R** | **Catalog Foundation Refactor** | **Ready to begin** |
| F17 | Roadmap Viewer & Catalog Roadmaps | Blocked until F16-R |
| F18 | Progress & Learning Journey | Not started |
| F19 | Career Path Catalog | Not started |
| F20 | Certification Catalog | Not started |
| F21 | Optional Initiative Association | Not started |
| F22 | Dashboard, Unified Search & Release | Not started |

---

## Frozen architecture (summary)

> **Engineering Learning Hub owns guidance, not knowledge.**

- **Learn** is a **Learning Navigation Platform** — guides, does not teach
- **Platform catalog** ships ~100 technologies and curated roadmaps (JSON packages)
- **Admins curate** visibility — they do not author educational content
- **External providers** own learning material at curated URLs
- **Projects** remains an independent module
- Cross-navigation is a relationship (F16)

---

## Approval gate

| Gate | Status |
|------|--------|
| Product design v1.1 | **Approved — frozen** |
| Architecture revision v2 | **Approved — frozen** |
| Catalog specification v1.0 | **Approved — frozen** |
| Implementation plan v2 | **Approved — frozen** |
| **F16-R implementation** | **Ready to begin** |
| Revised F17 | Blocked until F16-R complete |
| Original F17 (admin CRUD) | **Cancelled** |
