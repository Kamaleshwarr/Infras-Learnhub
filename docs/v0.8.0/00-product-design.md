# v0.8.0 Learn Module — Comprehensive Product Design

**Version:** 1.0 (draft for approval)  
**Last updated:** 2026-07-02  
**Authors:** Product Discovery phase  
**Status:** Awaiting product owner approval

---

## Table of contents

1. [Product Vision](#1-product-vision)
2. [Information Architecture](#2-information-architecture)
3. [Learn Module Navigation](#3-learn-module-navigation)
4. [Learning Journey Concept](#4-learning-journey-concept)
5. [Career Path Concept](#5-career-path-concept)
6. [Technology Concept](#6-technology-concept)
7. [Roadmap Concept](#7-roadmap-concept)
8. [Learning Resources Concept](#8-learning-resources-concept)
9. [Project Integration](#9-project-integration)
10. [Industry Certification Integration](#10-industry-certification-integration)
11. [Initiative Integration](#11-initiative-integration)
12. [Progress Tracking](#12-progress-tracking)
13. [Admin Responsibilities](#13-admin-responsibilities)
14. [Employee Experience](#14-employee-experience)
15. [Business Rules](#15-business-rules)
16. [Non-Functional Considerations](#16-non-functional-considerations)
17. [Risks](#17-risks)
18. [Future AI Opportunities](#18-future-ai-opportunities)
19. [Product Roadmap for Implementation](#19-product-roadmap-for-implementation)

Detailed sections are also split into focused documents under `docs/v0.8.0/`. This document is the authoritative master reference.

---

## 1. Product Vision

### 1.1 Problem statement

Many engineers know **what they want to become** but lack clarity on:

- Where to start
- What to learn first and what comes next
- Which external resources are trustworthy
- When they are ready for hands-on projects
- When they are ready to pursue industry certification

Existing enterprise learning tools tend to be either content repositories (file dumps) or full LMS platforms (courses, quizzes, grading). Neither solves the *navigation* problem.

### 1.2 Solution

**Learn** is a Learning Guidance Platform embedded in Engineering Learning Hub. It provides:

- Structured **Career Paths** for professional directions
- **Technology**-scoped **Roadmaps** with ordered **Stages**
- Curated **Learning Resources** (external links only)
- Recommended **Projects** for hands-on practice
- **Certification** readiness guidance pointing to official providers
- **Progress** tracking across the learner's journey
- Optional ties to **Company Initiatives** when administrators run internal campaigns

### 1.3 Positioning metaphor

> Engineering Learning Hub is to learning what Google Maps is to dining.
>
> Google Maps does not own restaurants. It helps users reach the best ones.
>
> Engineering Learning Hub does not own learning content. It helps engineers reach the best learning resources.

### 1.4 What we are NOT building

| Out of scope | Reason |
|--------------|--------|
| LMS / course platform | Violates product philosophy; high maintenance |
| Hosted video or document lessons | Resources are external links |
| Quizzes and graded assessments | Progress is self-reported in v1 |
| Certification exam delivery | Direct users to official providers |
| Mandatory learning tied to initiatives | Initiatives are optional encouragement |
| AI tutor (v1) | Future-ready architecture; not required for launch |

### 1.5 Success metrics (post-launch)

| Metric | Target (indicative) |
|--------|---------------------|
| Employees who start at least one Roadmap within 90 days | ≥ 40% of active employees |
| Stage completion rate (started → ≥1 stage complete) | ≥ 70% |
| Resource link click-through from Roadmap | Measurable baseline in month 1 |
| Certification readiness views → official provider clicks | Trackable via outbound analytics |
| Initiative-linked certification submissions (where applicable) | Increase vs pre-Learn baseline |
| Admin content publish cycle | First 3 Career Paths live at launch |

---

## 2. Information Architecture

### 2.1 Application-level navigation (v0.8.0 target)

**Employee sidebar**

| Order | Label | Path | Notes |
|-------|-------|------|-------|
| 1 | Dashboard | `/` | Adds Learn widgets |
| 2 | Learn | `/learn` | New primary module |
| 3 | Initiatives | `/initiatives` | Unchanged |
| 4 | Leaderboards | `/leaderboards/global` | Unchanged |
| 5 | My Certifications | `/submissions` | Renamed from "My Submissions" |
| 6 | Profile | `/profile` | Unchanged |

**Admin sidebar**

| Order | Label | Path | Notes |
|-------|-------|------|-------|
| 1 | Dashboard | `/` | Adds Learn admin metrics |
| 2 | Learn | `/learn` | Includes admin management entry |
| 3 | Initiatives | `/initiatives` | Unchanged |
| 4 | Users | `/users` | Unchanged |
| 5 | Review Submissions | `/submissions/review` | Renamed from "Certificate Review" |
| 6 | Leaderboards | `/leaderboards/global` | Unchanged |
| 7 | Settings | `/settings` | New; Learn config subset in v1 |

**Demoted from primary navigation (not removed)**

| Former label | Path | Treatment |
|--------------|------|-----------|
| Study Materials | `/study-materials` | Accessible via Settings or footer link; not primary IA |
| Projects | `/projects` | Project Knowledge Repository; separate from Learn Projects |
| Submit Certificate | `/submissions/new` | Accessible from Learn Certification CTAs and My Certifications |
| Notifications | `/notifications` | Retained in header bell; removed from sidebar clutter |

> **Rationale:** The user's specified navigation prioritizes the learning journey. Notifications remain in the header. Submit Certificate becomes contextual (Certification readiness, Initiatives) rather than a top-level nav item.

### 2.2 Entity hierarchy

```text
Career Path
└── Technology (ordered membership)
    └── Roadmap (one primary per Technology in v1)
        └── Stage (ordered)
            ├── Learning Resources (0..n curated links)
            ├── Projects (0..n recommendations)
            └── Certification readiness note (optional)

Certification (catalog entry)
└── linked Technologies / Roadmaps
└── optional link to active Initiatives

Employee Learning Journey
└── Enrollment (Career Path and/or Technology Roadmap)
    └── Stage Progress (per enrollment)
```

### 2.3 Content ownership model

| Entity | Created by | Consumed by |
|--------|------------|-------------|
| Career Path | Admin | Employee |
| Technology | Admin | Employee |
| Roadmap & Stages | Admin | Employee |
| Learning Resource | Admin (curated link) | Employee (opens externally) |
| Learn Project | Admin (recommendation) | Employee |
| Certification catalog entry | Admin | Employee |
| Progress | Employee (self-reported) | Employee (+ aggregate admin views) |
| Initiative | Admin | Employee (optional layer) |

---

## 3. Learn Module Navigation

### 3.1 Learn home (`/learn`)

Entry point for all users. Role-aware content.

**Employee view**

- Hero: "Where do you want to grow?"
- **Continue Learning** — active enrollments with progress
- **Explore Career Paths** — featured and browsable paths
- **Browse Technologies** — A–Z or category filter
- **Certifications** — popular / recommended industry certs
- Quick link: My Learning Journey (`/learn/journey`)

**Admin view**

- Same discovery surfaces (preview as employee)
- **Manage Content** toolbar → admin sub-routes
- Draft / published content status summary

### 3.2 Learn sub-navigation (tabs or secondary nav)

| Tab | Path | Audience |
|-----|------|----------|
| Home | `/learn` | All |
| Career Paths | `/learn/career-paths` | All |
| Technologies | `/learn/technologies` | All |
| Certifications | `/learn/certifications` | All |
| My Journey | `/learn/journey` | Employee |
| Manage | `/learn/manage` | Admin |

Admin **Manage** expands to:

```text
/learn/manage
├── /learn/manage/career-paths
├── /learn/manage/technologies
├── /learn/manage/roadmaps
├── /learn/manage/resources
├── /learn/manage/projects
└── /learn/manage/certifications
```

### 3.3 Deep-link URL patterns

| Page | URL pattern |
|------|-------------|
| Career Path detail | `/learn/career-paths/:careerPathId` |
| Technology detail | `/learn/technologies/:technologyId` |
| Roadmap view | `/learn/technologies/:technologyId/roadmap` |
| Stage detail (anchor or sub-route) | `/learn/technologies/:technologyId/roadmap#stage-:stageId` |
| Certification detail | `/learn/certifications/:certificationId` |
| Learn Project detail | `/learn/projects/:projectId` |
| Admin edit Career Path | `/learn/manage/career-paths/:careerPathId` |
| Admin Roadmap editor | `/learn/manage/technologies/:technologyId/roadmap` |

---

## 4. Learning Journey Concept

### 4.1 Definition

A **Learning Journey** is the employee's personal, ongoing experience of guided learning across one or more Career Paths and Technology Roadmaps. It is the product — not the resource links themselves.

### 4.2 Journey states

| State | Description |
|-------|-------------|
| **Exploring** | Browsing without enrollment |
| **Enrolled** | Committed to a Career Path and/or Technology Roadmap |
| **In Progress** | At least one Stage started |
| **Stage Complete** | Self-reported completion of individual Stages |
| **Roadmap Complete** | All Stages in a Technology Roadmap marked complete |
| **Certification Ready** | Meets readiness criteria (all required Stages complete) |
| **Certified** | External certification achieved; optionally recorded via My Certifications |

### 4.3 Journey principles

1. **One clear next step** — Every enrolled learner always sees a recommended next Stage.
2. **No dead ends** — Completing a Roadmap suggests adjacent Technologies or Certifications.
3. **Initiative-agnostic** — Journey works with zero active initiatives.
4. **Resumable** — Progress persists; "Continue Learning" on Dashboard and Learn home.
5. **Low friction** — Enrollment is one click; no approval workflow.

### 4.4 Journey UI surfaces

| Surface | Purpose |
|---------|---------|
| Learn home — Continue Learning | Resume primary enrollment |
| My Journey (`/learn/journey`) | All enrollments, progress, history |
| Dashboard widget | At-a-glance next Stage |
| Technology Roadmap | Stage list with progress indicators |
| Certification detail | Readiness banner based on linked Roadmap progress |

---

## 5. Career Path Concept

### 5.1 Definition

A **Career Path** is a curated, ordered collection of **Technologies** that together describe a professional direction (e.g., Cloud Engineer, Backend Java Developer, DevOps Engineer).

A Career Path answers: *"If I want to become X, what Technologies should I learn, and in what order?"*

### 5.2 Attributes (conceptual)

| Attribute | Description |
|-----------|-------------|
| Title | e.g., "Cloud Engineer" |
| Description | Who this path is for, outcomes, prerequisites |
| Icon / visual | Distinctive identifier (MUI icon or uploaded image) |
| Technologies | Ordered list with optional "required" vs "elective" |
| Estimated duration | Admin-provided range (e.g., "6–12 months part-time") |
| Status | DRAFT / PUBLISHED / ARCHIVED |
| Featured flag | Surfaces on Learn home |

### 5.3 Employee interactions

- Browse and search Career Paths
- View detail: description, Technology list, aggregate progress if enrolled
- **Start Career Path** → enrolls in path and optionally auto-enrolls in first Technology Roadmap
- View per-Technology progress within the path
- Complete path when all required Technologies' Roadmaps are complete

### 5.4 Design constraints

- A Technology may appear in multiple Career Paths
- Career Paths do not contain content — they contain Technology references
- Minimum 2 Technologies to publish a Career Path
- Maximum 12 Technologies per Career Path (guidance, not hard block)

---

## 6. Technology Concept

### 6.1 Definition

A **Technology** represents a specific skill domain (AWS, Docker, Java, Kubernetes, Terraform). Each published Technology has exactly **one primary Roadmap** in v1.

### 6.2 Attributes (conceptual)

| Attribute | Description |
|-----------|-------------|
| Name | e.g., "Amazon Web Services (AWS)" |
| Short name | e.g., "AWS" |
| Description | Scope of the Technology |
| Category | Cloud, Languages, DevOps, Data, Security, etc. |
| Difficulty level | Beginner / Intermediate / Advanced |
| Prerequisites | Other Technologies recommended first (soft guidance) |
| Primary Roadmap | One-to-one in v1 |
| Related Certifications | 0..n catalog links |
| Status | DRAFT / PUBLISHED / ARCHIVED |

### 6.3 Relationship to Career Path

```text
Career Path "Cloud Engineer"
  1. Linux Fundamentals (Technology)
  2. Networking Basics (Technology)
  3. AWS (Technology)
  4. Terraform (Technology)
  5. Kubernetes (Technology)
```

Employees may also enroll directly in a Technology without a Career Path.

---

## 7. Roadmap Concept

### 7.1 Definition

A **Roadmap** is an ordered sequence of **Stages** that guides a learner through a Technology from foundations to readiness. It is the core navigational artifact — the "route" in the Google Maps metaphor.

### 7.2 Stage structure

Each **Stage** contains:

| Element | Description |
|---------|-------------|
| Title | e.g., "IAM and Core Services" |
| Description | What the learner will understand after this Stage |
| Order | Position in Roadmap (1-based) |
| Learning Resources | Curated links for this Stage |
| Projects | Recommended hands-on work (may be empty for early Stages) |
| Estimated effort | e.g., "1–2 weeks" |
| Certification relevance | Optional note: "Covers ~30% of AZ-900" |

### 7.3 Roadmap rules

| Rule | Detail |
|------|--------|
| Minimum Stages | 3 per published Roadmap |
| Maximum Stages | 20 per Roadmap (guidance) |
| Ordering | Strict sequence; later Stages assume earlier ones |
| Stage naming | Action-oriented titles ("Understand X", "Build Y") |
| No nested Stages | Flat list only in v1 |
| Unlocking | Soft — later Stages visible but "recommended after Stage N" |

### 7.4 Roadmap UX

- Vertical timeline or stepped stepper (Material UI Stepper pattern)
- Each Stage expandable: Resources, Projects, Mark Complete
- Progress bar: `completedStages / totalStages`
- **Next up** callout on current Stage
- Completed Stages show checkmark and completion date

---

## 8. Learning Resources Concept

### 8.1 Definition

**Learning Resources** are admin-curated **external links** attached to Roadmap Stages. They are not hosted, streamed, or downloaded within Engineering Learning Hub.

### 8.2 Resource types

| Type | Examples | Priority |
|------|----------|----------|
| Official Documentation | docs.aws.amazon.com, kubernetes.io/docs | Highest |
| Open Educational Resource | freeCodeCamp, MDN, OpenJDK guides | High |
| Official Training | AWS Skill Builder (free tier), Microsoft Learn | High |
| Recommended Article / Blog | Curated engineering blogs | Medium |
| Video (external) | YouTube, vendor channels — link only | Medium |
| Paid resource (disclosed) | Udemy, Pluralsight — label clearly | Low (optional) |

### 8.3 Resource attributes

| Attribute | Description |
|-----------|-------------|
| Title | Display name |
| URL | Validated HTTPS URL |
| Type | From enum above |
| Provider | e.g., "AWS", "freeCodeCamp" |
| Estimated time | Optional (minutes/hours) |
| Free / Paid | Badge |
| Admin notes | Internal curation rationale (admin only) |
| Order | Within Stage resource list |

### 8.4 Resource UX

- Opens in **new tab** (`target="_blank"`, `rel="noopener noreferrer"`)
- Optional "Visited" toggle (employee) — feeds progress signals
- Broken-link reporting (employee, future) — admin notification
- No iframe embedding of third-party content in v1

### 8.5 Distinction from Study Materials module

| | Learn Learning Resources | Study Materials Repository |
|---|--------------------------|----------------------------|
| Purpose | Guided learning along a Roadmap | General file/link repository |
| Context | Attached to Roadmap Stages | Folder hierarchy |
| Ownership | Admin-curated for learning paths | Admin-uploaded files + links |
| Navigation | Within Learn module | Separate, demoted nav |
| v0.8.0 scope | **In scope** | **Not redesigned** |

---

## 9. Project Integration

### 9.1 Definition (Learn Projects)

**Learn Projects** are hands-on practice recommendations attached to Roadmap Stages. They tell the learner *what to build* to reinforce skills — not where files are stored.

> **Critical distinction:** Learn **Projects** ≠ **Project Knowledge Repository** (`/projects`). The latter is an internal KT document store. Learn Projects are external or brief-based practice guidance.

### 9.2 Project attributes

| Attribute | Description |
|-----------|-------------|
| Title | e.g., "Deploy a 3-tier app to AWS" |
| Description | Objectives, scope, success criteria |
| Difficulty | Beginner / Intermediate / Advanced |
| Estimated time | e.g., "4–8 hours" |
| External link | Optional (GitHub template, brief URL) |
| Prerequisites | Minimum Stage order required |
| Skills practiced | Tags for discoverability |

### 9.3 Placement in Roadmap

- Early Stages: 0 Projects (foundations first)
- Middle Stages: 1–2 guided Projects
- Late Stages: Capstone-style Projects before Certification readiness

### 9.4 Employee interactions

- View Project on Stage detail
- **Mark Project Complete** (self-reported)
- Optional link to external starter repo
- No file upload to Engineering Learning Hub for project deliverables in v1

### 9.5 Future: link to Project Knowledge Repository

When an internal project documents relevant patterns, admin may add an optional cross-link. This is informational only — not a dependency.

---

## 10. Industry Certification Integration

### 10.1 Definition

**Certifications** in the Learn module are **industry credentials** offered by external providers (AWS, Microsoft, Oracle, CNCF, Docker, etc.). Engineering Learning Hub catalogs them, maps them to Roadmaps, and guides readiness — but never delivers the exam.

### 10.2 Certification catalog attributes

| Attribute | Description |
|-----------|-------------|
| Name | e.g., "AWS Certified Cloud Practitioner" |
| Provider | e.g., "Amazon Web Services" |
| Official exam URL | Link to provider exam page |
| Description | What the certification validates |
| Level | Foundational / Associate / Professional / Specialty |
| Linked Technologies | Roadmaps that prepare for this cert |
| Readiness criteria | e.g., "All Stages complete in linked Roadmap" |
| Estimated exam cost | Informational disclaimer |
| Status | DRAFT / PUBLISHED |

### 10.3 Readiness model (v1)

```text
NOT STARTED → IN PROGRESS → READY → (external exam) → RECORDED
```

| Readiness state | Condition |
|-----------------|-----------|
| Not Started | No linked Roadmap enrollment |
| In Progress | Enrolled; Stages incomplete |
| Ready | All required Stages marked complete |
| Recorded | Certificate submitted via My Certifications (optional) |

### 10.4 Employee CTAs by readiness

| State | Primary CTA | Secondary CTA |
|-------|-------------|---------------|
| Not Started | Start Roadmap | View official exam info |
| In Progress | Continue Roadmap | View exam outline (external) |
| Ready | **Go to official provider** (external) | Submit Certificate (if initiative active) |
| Recorded | View in My Certifications | Explore next Certification |

### 10.5 Distinction from Company Initiatives

| | Industry Certification (Learn) | Company Initiative |
|---|-------------------------------|-------------------|
| Owner | External provider | Internal admin |
| Purpose | Professional credential | Internal campaign / reward |
| Required? | Never | Always optional |
| Example | AWS Cloud Practitioner | "AWS Learning Challenge — July 2027" |
| Submission | My Certifications (when employee chooses) | Submit Certificate linked to initiative |

---

## 11. Initiative Integration

### 11.1 Integration model

Initiatives and Learn are **loosely coupled**. An Initiative may reference a Certification catalog entry, but Learn functions fully without any Initiative.

```text
Learn Module                    Initiative Module
─────────────                   ─────────────────
Certification: AWS CLF-C02  ←── optional link ──→  Initiative: "AWS Challenge 2027"
Roadmap progress                                        Reward / deadline / leaderboard
Readiness: READY                                        Submit Certificate CTA
```

### 11.2 Cross-module surfaces

| Surface | Behaviour |
|---------|-----------|
| Certification detail (Learn) | If matching active Initiative exists, show banner: "Company initiative active — earn rewards" with link to Initiative detail |
| Initiative detail (existing) | If linked Certification exists, show learner's Roadmap progress and readiness |
| Dashboard | Separate widgets; Initiative widget does not gate Learn widget |
| Submit Certificate | Pre-fill initiative when navigated from Initiative; pre-fill certification context when from Learn |

### 11.3 New Initiative attribute (future schema)

| Attribute | Description |
|-----------|-------------|
| `linkedCertificationId` | Optional FK to Learn Certification catalog |
| Unlinked initiatives | Continue to work exactly as v0.7.1 (title-only campaigns) |

### 11.4 Rules

1. Learning never depends on an initiative being active
2. Initiative expiry does not affect Learn content or progress
3. Initiative delete rules (F15) unchanged — submission count governs
4. Employees see initiative encouragement as additive, not mandatory

---

## 12. Progress Tracking

### 12.1 Progress granularity

| Level | Tracked | Method |
|-------|---------|--------|
| Enrollment | Career Path / Technology | User action: Start |
| Stage | Per Roadmap Stage | User action: Mark Complete |
| Resource | Per Learning Resource | Optional: Mark Visited |
| Project | Per Learn Project | User action: Mark Complete |
| Certification readiness | Derived | All required Stages complete |
| Certification recorded | My Certifications | Existing submission workflow |

### 12.2 Progress data (conceptual)

```text
LearningEnrollment
  userId, careerPathId?, technologyId, enrolledAt, status

StageProgress
  enrollmentId, stageId, status (NOT_STARTED | IN_PROGRESS | COMPLETE), completedAt?

ResourceVisit (optional v1)
  userId, resourceId, visitedAt

ProjectProgress
  enrollmentId, projectId, status, completedAt?
```

### 12.3 Derived metrics

| Metric | Calculation |
|--------|-------------|
| Roadmap % | `completeStages / totalStages × 100` |
| Career Path % | `completeRequiredTechnologies / totalRequired × 100` |
| Next Stage | First incomplete Stage by order |
| Certification readiness | Boolean from linked Roadmap completion |

### 12.4 Progress UX

- Checkmarks on Stage stepper
- Progress rings on Career Path cards
- "Continue where you left off" on Dashboard
- My Journey timeline of completions
- No leaderboards for Learn progress in v1 (Initiative leaderboards unchanged)

### 12.5 Reset and unenroll

- Employee may **leave** a Career Path or Technology enrollment
- Leaving preserves history but hides from active journey
- Employee may **reset** Stage progress (confirm dialog) — rare edge case
- Admin cannot modify employee progress in v1

---

## 13. Admin Responsibilities

### 13.1 Content curation (primary duty)

| Task | Description |
|------|-------------|
| Define Career Paths | Professional directions for the organization |
| Define Technologies | Skill domains aligned to business needs |
| Build Roadmaps | Stage sequences with learning logic |
| Curate Learning Resources | Vet and link external resources |
| Define Learn Projects | Hands-on recommendations |
| Maintain Certification catalog | Map certs to Roadmaps |
| Publish / archive content | Lifecycle management |

### 13.2 Content lifecycle

```text
DRAFT → PUBLISHED → ARCHIVED
```

| Status | Employee visibility |
|--------|---------------------|
| DRAFT | Admin only (preview mode) |
| PUBLISHED | Visible in Learn |
| ARCHIVED | Hidden from browse; existing enrollments retain read-only access |

### 13.3 Admin workflows

- Create Technology before Roadmap
- Roadmap must have ≥ 3 Stages before publish
- Career Path must reference only PUBLISHED Technologies (or DRAFT in preview)
- Bulk import of resources deferred (manual curation v1)
- Content review cadence: quarterly link validation recommended

### 13.4 What admins do NOT do in Learn

- Upload video lessons
- Create quizzes or exams
- Grade learners
- Issue certifications
- Force enrollment

---

## 14. Employee Experience

### 14.1 First visit

1. Employee lands on Dashboard or Learn (configurable default post-login, future)
2. Learn home: "Where do you want to grow?"
3. Browse Career Paths or Technologies
4. Select path → view detail → **Start Learning**
5. Land on first Roadmap Stage with clear next step

### 14.2 Returning visit

1. Dashboard shows **Continue Learning** widget
2. One-click resume to current Stage
3. My Journey shows all active and completed enrollments

### 14.3 Certification pursuit flow

1. Discover Certification in Learn catalog
2. View linked Roadmap and readiness criteria
3. Enroll and complete Stages
4. Readiness banner: "You're ready"
5. Click through to official provider
6. After passing, submit via My Certifications (optionally linked to Initiative)

### 14.4 UX principles

| Principle | Implementation |
|-----------|----------------|
| Clarity | Always show one recommended next action |
| Trust | Prefer official docs; label paid resources |
| Autonomy | Self-paced; no deadlines unless Initiative |
| Simplicity | Two roles only; no manager-specific views |
| Consistency | Match Initiative module patterns (list, detail, chips) |

---

## 15. Business Rules

See `docs/v0.8.0/03-business-rules.md` for the authoritative numbered rule set. Summary of critical rules:

| ID | Rule |
|----|------|
| BR-L01 | Learn content is guidance only — no hosted lessons |
| BR-L02 | Learning Resources must be valid HTTPS URLs |
| BR-L03 | Official documentation preferred in curation guidelines |
| BR-L04 | One primary Roadmap per Technology in v1 |
| BR-L05 | Minimum 3 Stages to publish a Roadmap |
| BR-L06 | Progress is self-reported; no automated assessment in v1 |
| BR-L07 | Learning never depends on an active Initiative |
| BR-L08 | Industry Certifications are cataloged in Learn, not Initiatives |
| BR-L09 | Initiatives may optionally link to a Certification |
| BR-L10 | Learn Projects are distinct from Project Knowledge Repository |
| BR-L11 | Archived content: no new enrollments; existing enrollments read-only |
| BR-L12 | Only ADMIN role manages Learn content |
| BR-L13 | All authenticated users may enroll and track progress |
| BR-L14 | External links open in new tab |
| BR-L15 | Certification readiness is derived from Stage completion |

---

## 16. Non-Functional Considerations

### 16.1 Performance

| Area | Target |
|------|--------|
| Learn home load | < 2s on corporate network |
| Roadmap detail | < 1.5s; Stages paginated if > 10 |
| Progress update | Optimistic UI; < 500ms perceived |

### 16.2 Security

| Area | Approach |
|------|----------|
| Admin content APIs | `@PreAuthorize("hasRole('ADMIN')")` |
| Employee progress APIs | Scoped to authenticated user |
| External URLs | Validated on save; `javascript:` blocked |
| XSS | Sanitize admin text fields; never embed arbitrary HTML from URLs |

### 16.3 Accessibility

- Roadmap stepper keyboard-navigable
- Stage expand/collapse with `aria-expanded`
- Progress announced to screen readers
- Color not sole indicator of completion (icons + text)

### 16.4 Scalability (content)

| Entity | Expected volume (year 1) |
|--------|--------------------------|
| Career Paths | 5–15 |
| Technologies | 20–50 |
| Stages (total) | 100–300 |
| Learning Resources | 500–1500 links |
| Learn Projects | 50–150 |
| Certifications | 20–40 |

### 16.5 Audit and logging

- Admin publish/archive actions logged (structured INFO)
- No PII in progress logs beyond user ID
- Link validation job results logged (future)

### 16.6 Internationalization

- v1: English only
- Future: external resource language tags for filtering

---

## 17. Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Scope creep toward LMS | High | High | Strict terminology; approval gate; no upload/quiz features in v1 |
| Study Materials / Learn confusion | Medium | Medium | Clear IA; demote Study Materials nav; document distinction |
| Learn Projects vs Projects module naming | Medium | Medium | Always qualify: "Learn Project" in UI; separate routes |
| External link rot | High | Medium | Quarterly admin review; future automated link checker |
| Low admin curation capacity | Medium | High | Launch with 3 Career Paths; template library for Roadmaps |
| Self-reported progress inaccuracy | Medium | Low | Accept for v1; readiness is guidance not gatekeeping |
| Initiative-Certification coupling too tight | Low | High | Optional FK only; Learn works standalone |
| Employee overwhelm (too many paths) | Medium | Medium | Featured paths; recommendations; start with focused catalog |
| Certification provider URL changes | Medium | Low | Admin-maintained catalog; easy edit |
| Dual-role users (ADMIN+EMPLOYEE) | Low | Low | Existing pattern: show both admin manage and employee journey |

---

## 18. Future AI Opportunities

Ideas only — **not in v0.8.0 scope**. See `docs/v0.8.0/06-future-enhancements.md`.

| Opportunity | Description |
|-------------|-------------|
| Personalized path recommendation | Suggest Career Path based on role, submissions, progress |
| Skill gap analysis | Compare completed Stages to job profile or team needs |
| Resource summarization | AI-generated Stage summaries from linked docs |
| Conversational guide | Chat-based "what should I learn next?" |
| Smart readiness | ML-enhanced certification readiness beyond Stage completion |
| Link quality scoring | Auto-detect outdated or low-quality resources |
| Initiative matching | Suggest relevant Initiatives based on journey |

Architecture should keep AI integration points optional (e.g., recommendation service interface) without requiring AI for core flows.

---

## 19. Product Roadmap for Implementation

See `docs/v0.8.0/05-roadmap.md` for phased F16+ plan. Summary:

| Phase | Theme | Deliverable |
|-------|-------|-------------|
| F16 | Foundation | Types, routes, empty Learn shell, sidebar nav update |
| F17 | Technology & Roadmap (read) | Employee browse Technology, view Roadmap |
| F18 | Career Paths (read) | Browse and detail Career Paths |
| F19 | Progress & Journey | Enrollment, Stage completion, My Journey |
| F20 | Learning Resources & Projects | Resource links, Learn Projects on Stages |
| F21 | Certification catalog | Certification browse, readiness, external CTAs |
| F22 | Admin content management | CRUD for all Learn entities |
| F23 | Initiative integration | Optional Certification link on Initiatives |
| F24 | Dashboard & polish | Widgets, nav rename, empty states, QA |

**Estimated feature count:** 9 phases (F16–F24) for v0.8.0 MVP.

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| Career Path | Ordered collection of Technologies for a professional direction |
| Technology | Skill domain with one primary Roadmap |
| Roadmap | Ordered Stages guiding learning for a Technology |
| Stage | Single step in a Roadmap with Resources and Projects |
| Learning Resource | Curated external link |
| Learn Project | Hands-on practice recommendation |
| Certification | Industry credential from external provider |
| Learning Journey | Employee's personal progress across enrollments |
| Progress | Self-reported completion state |
| Initiative | Internal admin campaign (separate module) |

---

## Appendix B: Open questions for approval

| # | Question | Recommendation |
|---|----------|----------------|
| 1 | Rename "My Submissions" → "My Certifications"? | Yes — aligns with user-specified nav |
| 2 | Remove Notifications from sidebar? | Yes — keep header bell only |
| 3 | Demote Study Materials and Projects from nav? | Yes — reduce confusion with Learn |
| 4 | Allow multiple Roadmaps per Technology in v1? | No — one primary; simplify |
| 5 | Require Stage sequential completion? | Soft recommendation only |
| 6 | Initiative `linkedCertificationId` in v0.8.0 or later? | F23 — after Learn catalog exists |

---

## Document approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | | | Pending |
| Engineering Lead | | | Pending |
| UX Lead | | | Pending |
