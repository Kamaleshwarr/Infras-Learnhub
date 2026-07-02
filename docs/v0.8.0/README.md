# Engineering Learning Hub — v0.8.0

**Release theme:** Learn — Learning Navigation Platform  
**Product design:** Approved v1.1 (frozen — vision unchanged)  
**Implementation plan v1:** FINAL — frozen ([07-implementation-plan.md](./07-implementation-plan.md))  
**Architecture revision v2:** **Pending approval** ([08-navigation-platform-revision.md](./08-navigation-platform-revision.md))  
**Implementation plan v2:** DRAFT ([09-implementation-plan-v2.md](./09-implementation-plan-v2.md))  
**Depends on:** v0.7.1 Initiative Management (complete); F16 shipped

---

## ⚠️ Architecture revision in progress

F16 review identified LMS-like drift in the v1 implementation plan (admin Technology/Roadmap/Resource CRUD). A **catalog-first, curation-only** revision is pending approval.

**Do not implement original F17 until revision is approved.**

---

## Document index

| # | Document | Contents |
|---|----------|----------|
| 00 | [Product Design](./00-product-design.md) | Approved product architecture (frozen) |
| 01 | [Product Vision](./01-product-vision.md) | Vision, principles, positioning |
| 02 | [Information Architecture](./02-information-architecture.md) | Navigation, URLs, entities |
| 03 | [Business Rules](./03-business-rules.md) | Authoritative business rules |
| 04 | [User Flows](./04-user-flows.md) | Employee and admin journeys |
| 05 | [Implementation Roadmap (summary)](./05-roadmap.md) | Phase overview |
| 06 | [Future Enhancements](./06-future-enhancements.md) | AI and deferred features |
| 07 | [Implementation Plan v1](./07-implementation-plan.md) | FINAL F16–F23 (admin-authoring model) |
| **08** | **[Navigation Platform Revision](./08-navigation-platform-revision.md)** | **Architecture revision v2 — pending** |
| **09** | **[Implementation Plan v2](./09-implementation-plan-v2.md)** | **Revised F16-R–F22 — pending** |

---

## Implementation phases

### Shipped

| Phase | Name | Status |
|-------|------|--------|
| F16 | Technology Discovery & Search | **Shipped** (refactor planned in F16-R) |

### Revised roadmap (pending approval)

| Phase | Name |
|-------|------|
| F16-R | Catalog Foundation Refactor |
| F17 | Roadmap Viewer & Catalog Roadmaps |
| F18 | Progress & Learning Journey |
| F19 | Career Path Catalog |
| F20 | Certification Catalog |
| F21 | Optional Initiative Association |
| F22 | Dashboard, Unified Search & Release |

---

## Frozen architecture (summary)

- **Learn** is a **Learning Navigation Platform** — guides, does not teach
- **Platform catalog** ships ~100 technologies and curated roadmaps
- **Admins curate** visibility — they do not author educational content
- **Projects** remains an independent module
- Cross-navigation is a relationship (F16)

---

## Approval gate

| Gate | Status |
|------|--------|
| Product design v1.1 | **Approved — frozen** |
| Implementation plan v1 | **Frozen** (superseded on v2 approval) |
| Architecture revision v2 | **Pending approval** |
| F16-R / revised F17 | **Blocked** |
