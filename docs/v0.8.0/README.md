# Engineering Learning Hub — v0.8.0 Product Design

**Release theme:** Learn — Learning Guidance Platform  
**Status:** Product Discovery & Design (pre-implementation)  
**Depends on:** v0.7.1 Initiative Management (complete)  
**Classification:** Product design only — no production code in this phase

---

## Purpose

v0.8.0 introduces the **Learn** module: a Learning Guidance Platform that helps engineers answer *where to start*, *what to learn next*, *which resources to trust*, *when they are ready for projects*, and *when they are ready for certification*.

Engineering Learning Hub does **not** teach, host study content, or conduct certifications. It guides learners to high-quality external resources — like Google Maps guides users to restaurants without owning them.

---

## Document index

| # | Document | Contents |
|---|----------|----------|
| 00 | [Product Design (master)](./00-product-design.md) | Comprehensive design covering all 19 deliverable areas |
| 01 | [Product Vision](./01-product-vision.md) | Vision, principles, positioning, success metrics |
| 02 | [Information Architecture](./02-information-architecture.md) | Navigation, URL structure, entity hierarchy, page inventory |
| 03 | [Business Rules](./03-business-rules.md) | Authoritative rules for content, progress, integrations |
| 04 | [User Flows](./04-user-flows.md) | Employee and admin journeys with flow diagrams |
| 05 | [Implementation Roadmap](./05-roadmap.md) | Phased F16+ delivery plan for post-approval implementation |
| 06 | [Future Enhancements](./06-future-enhancements.md) | AI opportunities, deferred features, long-term vision |

---

## Key product decisions (summary)

| Decision | Rationale |
|----------|-----------|
| Learn is guidance, not an LMS | Aligns with company philosophy; avoids content hosting burden |
| Learning Resources are curated links | Official docs and OER preferred; opens in new tab |
| Industry Certifications live in Learn | Distinct from Company Initiatives (internal campaigns) |
| Initiatives are optional encouragement | Learning never depends on an initiative being active |
| Progress is self-reported (v1) | Pragmatic MVP; avoids quiz/LMS complexity |
| Study Materials remain a separate module | Not redesigned; demoted from primary navigation |
| Learn Projects ≠ Project Knowledge Repository | Naming collision resolved via context and IA |

---

## Terminology (mandatory)

| Use | Avoid |
|-----|-------|
| Learn | LMS, Course platform |
| Career Path | Track, Program |
| Technology | Skill area (informal) |
| Roadmap | Curriculum, Syllabus |
| Stage | Lesson, Module, Chapter |
| Learning Resources | Study Materials (within Learn context) |
| Projects | Assignments (within Learn context) |
| Certification | Exam, Credential (when referring to industry certs) |
| Progress | Completion %, Grade |

---

## Relationship to existing modules

```text
┌─────────────────────────────────────────────────────────────────┐
│                    Engineering Learning Hub                      │
├─────────────┬──────────────┬──────────────┬────────────────────┤
│   Learn     │  Initiatives │ Certificates │   Leaderboards     │
│  (NEW v0.8) │  (v0.7.x)    │  (v0.6.x)    │   (foundation)     │
│             │              │              │                    │
│  Guidance   │  Internal    │  Submit &    │  Recognition       │
│  Roadmaps   │  campaigns   │  review      │  & competition     │
│  Resources  │  (optional)  │  workflow    │                    │
└─────────────┴──────────────┴──────────────┴────────────────────┘
         │              │                │
         └──────────────┴────────────────┘
              Optional cross-links;
              Learn never depends on Initiatives
```

---

## Approval gate

Implementation phases (F16+) will be created **only after** this product design is reviewed and approved by the product owner.

**Review checklist:**

- [ ] Product vision and positioning approved
- [ ] Information architecture and navigation approved
- [ ] Entity model and terminology approved
- [ ] Business rules approved
- [ ] User flows validated
- [ ] Initiative vs Certification separation confirmed
- [ ] Study Materials / Projects module boundaries confirmed
- [ ] Implementation roadmap phased and prioritized
- [ ] Risks acknowledged with mitigation plan

---

## Related documentation

- Project context: `.cursor/project-context.md`
- Architecture: `.cursor/architecture.md`
- Project roadmap: `docs/project-roadmap.md`
- v0.7.1 release: `docs/releases/release-v0.7.1.md`
