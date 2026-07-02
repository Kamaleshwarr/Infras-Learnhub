# v0.8.0 — Product Vision

**Module:** Learn  
**Release:** v0.8.0  
**Status:** Draft for approval

---

## Vision statement

Engineering Learning Hub will become the trusted **navigation system** for engineering growth — guiding every employee from aspiration to readiness through structured Career Paths, Technology Roadmaps, curated Learning Resources, hands-on Projects, and industry Certification guidance.

We guide. We do not teach.

---

## The problem

| Pain point | Who feels it | Current gap |
|------------|--------------|-------------|
| "I want to become a cloud engineer but don't know where to start" | Individual contributors | No structured starting point in the platform |
| "There are thousands of tutorials — which ones are good?" | Self-directed learners | Study Materials is a repository, not a guide |
| "Am I ready to take the AWS exam?" | Certification aspirants | Initiatives encourage certs but don't teach prerequisites |
| "What should I learn after Java basics?" | Career planners | No sequencing or progression model |
| "I completed an initiative — what now?" | Post-certification learners | No continued journey |

---

## The solution: Learn module

Learn transforms Engineering Learning Hub from a **workflow platform** (submit certs, join initiatives) into a **guidance platform** (discover, progress, achieve).

### Core value proposition

| For employees | For the organization |
|---------------|---------------------|
| Clear starting point for any growth goal | Standardized, curated learning paths aligned to business needs |
| Trusted resources without searching | Reduced time-to-competency |
| Visible progress and next steps | Better certification readiness before exam attempts |
| Optional initiative rewards on top | Initiatives become more effective when learners arrive prepared |

---

## Product positioning

### We are

- A **Learning Guidance Platform**
- A **curated map** of external learning
- A **progress companion** for self-directed growth
- An **integration layer** between learning, projects, and certification

### We are not

- An LMS or course platform
- A content hosting service
- A certification exam provider
- A replacement for official vendor training

### Metaphor

```text
Google Maps          →    Engineering Learning Hub Learn
─────────────────         ────────────────────────────────
Restaurants               Learning Resources (external)
Routes                    Roadmaps (ordered Stages)
Destinations              Certifications (industry credentials)
Your location             Progress (where you are now)
Traffic / ETA             Estimated effort per Stage
Promoted places           Featured Career Paths
```

---

## Product principles

These principles govern every design and implementation decision for v0.8.0 and beyond.

### 1. Learning Journey is the product

The employee's personal journey — enrollments, progress, next steps — is the centerpiece. Resources are means, not ends.

### 2. Study Materials are not the product

The existing Study Materials Repository remains available but is demoted from primary navigation. Learn Resources are contextually attached to Roadmap Stages, not dumped in folders.

### 3. Learning Resources are curated links

No video hosting. No PDF storage for lessons. Prefer official documentation and open educational resources. Paid resources are allowed but must be clearly labeled.

### 4. Only two user roles

`ADMIN` and `EMPLOYEE`. Managers use Admin access. No manager dashboards, no approval workflows for enrollment.

### 5. Keep everything simple

Flat Stage lists (no nested modules). One primary Roadmap per Technology in v1. Self-reported progress. Minimal configuration.

### 6. Industry Certifications ≠ Company Initiatives

| Industry Certification | Company Initiative |
|------------------------|-------------------|
| AWS Cloud Practitioner | AWS Learning Challenge — July 2027 |
| Lives in Learn module | Lives in Initiatives module |
| Permanent catalog entry | Time-bound campaign |
| Prepares the learner | Rewards the learner |

### 7. Learning never depends on Initiatives

Every Learn experience must work when zero initiatives are active. Initiatives add encouragement and rewards — they are not prerequisites.

### 8. AI is optional and future-ready

v0.8.0 ships without AI. Architecture should not block future recommendation or guidance features, but no core flow requires AI.

---

## Target users

### Primary: Employee (learner)

- Software engineers seeking growth direction
- Engineers pursuing industry certifications
- Employees joining company learning initiatives who need preparation guidance

### Secondary: Admin (curator)

- L&D administrators defining Career Paths
- Technical leads curating Technology Roadmaps and Resources
- HR / engineering managers launching Initiatives (existing module)

---

## Success criteria

### Launch criteria (v0.8.0 MVP)

| Criterion | Measure |
|-----------|---------|
| Learn module accessible from sidebar | `/learn` route live for all authenticated users |
| At least 3 published Career Paths | Admin-curated, covering major engineering directions |
| At least 10 published Technologies with Roadmaps | Each with ≥ 3 Stages and Resources |
| Certification catalog | ≥ 10 industry certifications with official links |
| Employee enrollment and progress | Start path, complete Stage, view My Journey |
| Initiative cross-link (optional) | At least 1 initiative linked to a Certification |
| No regression | Initiatives, Certificates, Leaderboards, Users unchanged |

### Post-launch success metrics (90-day)

| Metric | Description |
|--------|-------------|
| Adoption rate | % active employees with ≥ 1 enrollment |
| Engagement rate | % enrollees with ≥ 1 Stage complete |
| Continuation rate | % who return within 7 days of first Stage complete |
| Certification readiness | Count reaching READY state |
| Outbound clicks | Clicks to official provider exam pages |
| Initiative lift | Certification submissions where linked initiative exists |

---

## Competitive differentiation

| Alternative | How Learn differs |
|-------------|-------------------|
| Generic LMS (Cornerstone, Docebo) | No course authoring; lighter, guidance-focused |
| Content repositories (SharePoint, Confluence) | Structured progression, not file dumps |
| Vendor training portals | Vendor-agnostic; multi-technology Career Paths |
| Initiative-only platforms | Learn exists independent of campaigns |

---

## What changes for existing modules

| Module | Change in v0.8.0 |
|--------|------------------|
| Dashboard | Add Learn widgets (Continue Learning, featured paths) |
| Initiatives | Optional Certification link; show Roadmap progress on detail |
| My Certifications | Renamed nav label; contextual entry from Learn |
| Leaderboards | No change in v0.8.0 |
| Study Materials | Demoted from primary nav; not redesigned |
| Projects (KT) | Demoted from primary nav; not redesigned |
| Users / Auth / Profile | No change |

---

## Approval

This vision document must be approved before implementation phases F16+ begin.

**Next document:** [02-information-architecture.md](./02-information-architecture.md)
