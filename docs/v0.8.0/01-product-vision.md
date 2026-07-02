# v0.8.0 — Product Vision

**Module:** Learn  
**Release:** v0.8.0  
**Status:** Design refinement v1.1 — draft for approval

---

## Vision statement

Engineering Learning Hub will become the trusted **navigation system** for engineering growth — guiding every employee from aspiration to readiness through structured Career Paths, Technology Roadmaps, curated Learning Resources, curated Practice Resources, and industry Certification guidance.

We guide. We do not teach.

---

## Platform architecture

Engineering Learning Hub consists of **independent modules**:

| Module | Question it answers |
|--------|---------------------|
| **Learn** | How do I learn this technology? |
| **Projects** | How does our organization build and maintain real systems? |
| **Initiatives** | What is the company encouraging right now? |
| **Leaderboards** | Who is being recognized? |

Learn and Projects may **reference** each other. Learn does **not** own Projects.

---

## The problem

| Pain point | Who feels it | Current gap |
|------------|--------------|-------------|
| "I want to become a cloud engineer but don't know where to start" | Individual contributors | No structured starting point in the platform |
| "There are thousands of tutorials — which ones are good?" | Self-directed learners | Study Materials is a repository, not a guide |
| "Am I ready to take the AWS exam?" | Certification aspirants | Initiatives encourage certs but don't teach prerequisites |
| "What should I learn after Java basics?" | Career planners | No sequencing or progression model |
| "I learned Spring Boot — which real projects use it?" | Engineers joining teams | No link between learning and organizational systems |

---

## The solution: Learn module

Learn transforms Engineering Learning Hub from a **workflow platform** (submit certs, join initiatives) into a **guidance platform** (discover, progress, achieve).

### What Learn contains

- Technologies
- Career Paths
- Roadmaps
- Learning Resources (curated external study links)
- Practice Resources (curated external hands-on exercise links)
- Industry Certifications
- Learning Progress

### What Learn does NOT contain

- Organizational Projects (those live in the **Projects** module)
- Hosted lessons or assessments
- Certification exam delivery

### Core value proposition

| For employees | For the organization |
|---------------|---------------------|
| Clear starting point for any growth goal | Standardized, curated learning paths aligned to business needs |
| Trusted resources without searching | Reduced time-to-competency |
| Visible progress and next steps | Better certification readiness before exam attempts |
| Optional link to real company projects | Connects learning to organizational engineering context |
| Optional initiative rewards on top | Initiatives become more effective when learners arrive prepared |

---

## Product positioning

### We are

- A **Learning Guidance Platform** (Learn module)
- An **Engineering Knowledge Base** (Projects module — independent)
- A **curated map** of external learning
- A **progress companion** for self-directed growth
- An **integration layer** between learning, organizational projects, and certification

### We are not

- An LMS or course platform
- A content hosting service
- A certification exam provider
- A replacement for official vendor training
- A module where Learn owns or manages Projects

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
Local landmarks           Organizational Projects (separate module)
```

---

## Product principles

### 1. Learning Journey is the product

The employee's personal journey — enrollments, progress, next steps — is the centerpiece. Resources are means, not ends.

### 2. Study Materials are not the product

The existing Study Materials Repository remains available but is demoted from primary navigation. Learn Resources are contextually attached to Roadmap Stages, not dumped in folders.

### 3. Learning Resources and Practice Resources are curated links

No video hosting. No PDF storage for lessons. Learning Resources are for study; Practice Resources are for hands-on exercises — both external links.

### 4. Projects is an independent module

Organizational Projects (Banking Platform, Insurance Portal, etc.) maintain internal engineering knowledge. Learn may link to them for context but never owns them.

### 5. Only two user roles

`ADMIN` and `EMPLOYEE`. Managers use Admin access. No manager dashboards, no approval workflows for enrollment.

### 6. Keep everything simple

Flat Stage lists (no nested modules). One primary Roadmap per Technology in v1. Self-reported progress. Minimal configuration.

### 7. Industry Certifications ≠ Company Initiatives

| Industry Certification | Company Initiative |
|------------------------|-------------------|
| AWS Cloud Practitioner | AWS Learning Challenge — July 2027 |
| Lives in Learn module | Lives in Initiatives module |
| Permanent catalog entry | Time-bound campaign |
| Prepares the learner | Rewards the learner |

### 8. Learning never depends on Initiatives

Every Learn experience must work when zero initiatives are active. Initiatives add encouragement and rewards — they are not prerequisites.

### 9. AI is optional and future-ready

v0.8.0 ships without AI. Architecture should not block future recommendation or guidance features, but no core flow requires AI.

---

## Target users

### Primary: Employee (learner)

- Software engineers seeking growth direction
- Engineers pursuing industry certifications
- Employees joining company learning initiatives who need preparation guidance
- Engineers onboarding to organizational projects who need to learn related technologies

### Secondary: Admin (curator)

- L&D administrators defining Career Paths
- Technical leads curating Technology Roadmaps and Resources
- Engineering leads maintaining organizational Project knowledge (Projects module)
- HR / engineering managers launching Initiatives (existing module)

---

## Success criteria

### Launch criteria (v0.8.0 MVP)

| Criterion | Measure |
|-----------|---------|
| Learn module accessible from sidebar | `/learn` route live for all authenticated users |
| Projects remains independent in sidebar | `/projects` not under Learn Manage |
| At least 3 published Career Paths | Admin-curated, covering major engineering directions |
| At least 10 published Technologies with Roadmaps | Each with ≥ 3 Stages, Resources, and Practice Resources |
| Certification catalog | ≥ 10 industry certifications with official links |
| Employee enrollment and progress | Start path, complete Stage, view My Journey |
| Technology ↔ Project cross-links (optional) | At least 1 Technology linked to organizational Projects |
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
| Cross-nav usage | Clicks from Learn Technology → Projects detail |
| Initiative lift | Certification submissions where linked initiative exists |

---

## Competitive differentiation

| Alternative | How Learn differs |
|-------------|-------------------|
| Generic LMS (Cornerstone, Docebo) | No course authoring; lighter, guidance-focused |
| Content repositories (SharePoint, Confluence) | Structured progression, not file dumps |
| Vendor training portals | Vendor-agnostic; multi-technology Career Paths |
| Initiative-only platforms | Learn exists independent of campaigns |
| Confluence project spaces | Projects module is purpose-built; Learn links for skill context |

---

## What changes for existing modules

| Module | Change in v0.8.0 |
|--------|------------------|
| Dashboard | Add Learn widgets (Continue Learning, featured paths) |
| Learn | **New module** |
| Projects | Remains independent; optional cross-links from Learn Technologies |
| Initiatives | Optional Certification link; show Roadmap progress on detail |
| My Certifications | Renamed nav label; contextual entry from Learn |
| Leaderboards | No change in v0.8.0 |
| Study Materials | Demoted from primary nav; not redesigned |
| Users / Auth / Profile | No change |

---

## Approval

This vision document must be approved before implementation phases F16+ begin.

**Next document:** [02-information-architecture.md](./02-information-architecture.md)
