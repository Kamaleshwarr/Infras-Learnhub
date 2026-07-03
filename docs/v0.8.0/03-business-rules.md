# v0.8.0 — Business Rules

**Module:** Learn (+ cross-module references)  
**Status:** Design refinement v1.1 — authoritative rule set for implementation

---

## Rule categories

| Category | Prefix | Count |
|----------|--------|-------|
| Product & scope | BR-P | 6 |
| Module separation | BR-M | 5 |
| Content model | BR-C | 12 |
| Content lifecycle | BR-LC | 6 |
| Progress & enrollment | BR-PR | 10 |
| Learning Resources | BR-LR | 6 |
| Practice Resources | BR-PA | 6 |
| Cross-navigation | BR-XN | 5 |
| Certifications | BR-CT | 8 |
| Initiative integration | BR-IN | 6 |
| Authorization | BR-AU | 4 |
| Navigation & UX | BR-UX | 5 |

---

## Product & scope rules

### BR-P01 — Guidance only

Learn module content is **guidance only**. The platform does not host lessons, stream video, or deliver assessments. All learning content is accessed via external links.

### BR-P02 — Not an LMS

Features that would constitute an LMS (course authoring, quizzes, grades, SCORM, lesson playback) are **explicitly out of scope** for v0.8.0 and require a new product approval cycle.

### BR-P03 — Study Materials separation

The Study Materials Repository (`/study-materials`) is a **separate module**. Learn Learning Resources are not synchronized with Study Materials in v0.8.0. Admins may manually cross-reference.

### BR-P04 — Learn scope boundary

The Learn module contains only: Technologies, Career Paths, Roadmaps, Learning Resources, Practice Resources, Industry Certifications, and Learning Progress. Nothing more.

### BR-P05 — Initiative independence

All Learn functionality must operate correctly when **zero** Company Initiatives are active. No Learn feature may require initiative participation.

### BR-P06 — Projects independence

All Learn functionality must operate correctly when **zero** Technology ↔ Project cross-links exist. Cross-navigation is optional context only.

---

## Module separation rules

### BR-M01 — Projects is independent

The **Projects** module is a separate, independent module. It is not a child of Learn, not managed under `/learn/manage`, and not owned by the Learn module.

### BR-M02 — Projects purpose

Projects maintain **organizational project knowledge** — documentation, architecture, KT, and engineering assets for real company systems (e.g., Banking Platform, Insurance Portal).

### BR-M03 — Not learning projects

Organizational Projects are **not** learning projects, practice projects, or portfolio projects. Learn does not create, store, or manage them.

### BR-M04 — Practice Resources are not Projects

**Practice Resources** in Learn are curated **external links** for hands-on exercises. They are not organizational Projects and must not use the label "Project" in the Learn UI.

### BR-M05 — Separate admin surfaces

Learn content is administered under `/learn/manage`. Organizational Projects are administered under `/projects` (Projects module). No unified "project" admin in Learn.

---

## Content model rules

### BR-C01 — Career Path definition

A Career Path is an ordered collection of Technologies representing a professional direction. A Career Path does not contain Learning Resources or Stages directly.

### BR-C02 — Career Path minimum Technologies

A Career Path must reference **at least 2** Technologies to be published.

### BR-C03 — Career Path maximum Technologies

A Career Path may reference at most **12** Technologies. Exceeding requires product approval.

### BR-C04 — Technology uniqueness

Technology names must be unique (case-insensitive) within the Learn catalog.

### BR-C05 — One primary Roadmap per Technology

Each Technology has exactly **one primary Roadmap** in v0.8.0. Multiple Roadmaps per Technology are deferred.

### BR-C06 — Roadmap belongs to Technology

Every Roadmap is owned by exactly one Technology. Roadmaps cannot be shared across Technologies.

### BR-C07 — Stage ordering

Stages within a Roadmap have a unique, contiguous order starting at 1. Reordering updates order values; no gaps permitted in published Roadmaps.

### BR-C08 — Stage minimum count

A Roadmap must have **at least 3 Stages** to be published.

### BR-C09 — Stage maximum count

A Roadmap may have at most **20** Stages. Exceeding requires product approval.

### BR-C10 — Technology in multiple Career Paths

A Technology may appear in multiple Career Paths. Progress on a Technology Roadmap is **shared** across all Career Paths that include it.

### BR-C11 — Prerequisite Technologies

Prerequisites are **soft recommendations** only. Employees may enroll in a Technology without completing prerequisite Technologies.

### BR-C12 — Elective Technologies

Career Path Technologies may be marked **required** or **elective**. Career Path completion requires all **required** Technologies' Roadmaps to be complete.

---

## Content lifecycle rules

### BR-LC01 — Content statuses

Learn catalog entities (Career Path, Technology, Certification) use statuses: `DRAFT`, `PUBLISHED`, `ARCHIVED`.

### BR-LC02 — Draft visibility

`DRAFT` content is visible only to `ADMIN` users (preview mode). Employees receive **404** for draft content.

### BR-LC03 — Published visibility

`PUBLISHED` content is visible to all authenticated users in browse and search surfaces.

### BR-LC04 — Archived visibility

`ARCHIVED` content is hidden from browse/search. Existing enrollments retain **read-only** access to content and progress. No new enrollments allowed on archived content.

### BR-LC05 — Publish validation

Publishing a Career Path requires all referenced Technologies to be `PUBLISHED`.

### BR-LC06 — Archive cascade

Archiving a Technology does not auto-archive its Career Paths. Career Paths with archived required Technologies show a "Technology unavailable" indicator; new enrollments blocked.

---

## Progress & enrollment rules

### BR-PR01 — Self-reported progress

Stage completion and Practice Resource completion are **self-reported** by the employee in v0.8.0. No automated verification.

### BR-PR02 — Enrollment creation

Employees create enrollments via explicit action: **Start Career Path** or **Start Roadmap** (Technology). No admin assignment in v0.8.0.

### BR-PR03 — Concurrent enrollments

Employees may have **multiple active enrollments** simultaneously.

### BR-PR04 — Duplicate enrollment prevention

An employee may not have two active enrollments for the same Career Path or the same Technology. Re-enrollment after leaving creates a new enrollment; prior progress history is preserved but inactive.

### BR-PR05 — Stage completion

Marking a Stage complete sets `completedAt` to current UTC timestamp. Stages may be completed in any order (soft sequence recommended in UI).

### BR-PR06 — Stage uncomplete

Employees may mark a completed Stage as incomplete (toggle). `completedAt` is cleared. Certification readiness recalculates immediately.

### BR-PR07 — Roadmap completion

A Technology Roadmap is complete when all Stages are marked complete.

### BR-PR08 — Career Path completion

A Career Path is complete when all **required** Technologies' Roadmaps are complete.

### BR-PR09 — Leave enrollment

Employees may **leave** an enrollment. Active status becomes `LEFT`. Progress data is preserved.

### BR-PR10 — Admin progress immutability

Admins cannot modify employee progress records in v0.8.0.

---

## Learning Resources rules

### BR-LR01 — External links only

Learning Resources are HTTPS URLs only. `javascript:`, `data:`, and `file:` schemes are rejected.

### BR-LR02 — URL validation on save

Admin save validates URL format. Reachability is **not** required at save time.

### BR-LR03 — New tab behaviour

All Learning Resource links open in a new browser tab with `rel="noopener noreferrer"`.

### BR-LR04 — Resource attachment

A Learning Resource may be attached to multiple Stages. A Stage may have 0–20 Learning Resources.

### BR-LR05 — Resource ordering

Resources within a Stage are ordered. Default display: Official Documentation first, then OER, then others.

### BR-LR06 — Paid resource disclosure

Resources marked `paid` display a visible **Paid** badge.

---

## Practice Resources rules

### BR-PA01 — External links only

Practice Resources are HTTPS URLs only, following the same URL rules as Learning Resources (BR-LR01).

### BR-PA02 — Practice purpose

Practice Resources are for hands-on exercises, labs, and coding challenges — not for organizational project documentation.

### BR-PA03 — Terminology

User-facing label is **Practice Resource**. Do not label as "Project", "Learning Project", or "Practice Project" in the Learn module.

### BR-PA04 — Stage attachment

Practice Resources are attached to Stages. A Stage may have 0–10 Practice Resources. Early Stages (order 1–2) should have 0–1 (guideline).

### BR-PA05 — Completion independent of Stage

Marking a Practice Resource complete is self-reported and independent of Stage completion.

### BR-PA06 — Practice Resource library

Practice Resources may be created in a shared library and reused across Stages, similar to Learning Resources.

---

## Cross-navigation rules

### BR-XN01 — Optional links

Technology ↔ Organizational Project associations are **optional**. Both entity types may exist with zero links.

### BR-XN02 — Bidirectional display

A link between Technology T and Project P appears on both T's detail (Related Organization Projects) and P's detail (Related Technologies).

### BR-XN03 — Cross-nav only

Cross-links provide **navigation only**. Learn does not embed Project content. Projects does not embed Roadmap content.

### BR-XN04 — Admin maintenance

Links may be created or removed by admin from either the Learn Technology editor or the Projects module. Both views reflect the same junction data.

### BR-XN05 — No progress coupling

Project membership or Project knowledge access does not affect Learn progress. Learn enrollment does not affect Project access.

---

## Certification rules

### BR-CT01 — Industry certifications only

The Learn Certification catalog contains **industry credentials** from external providers only.

### BR-CT02 — Official exam link required

Every published Certification must have an official exam information URL from the provider.

### BR-CT03 — Certification-Technology linkage

A Certification must link to at least one Technology. Readiness is evaluated against the linked Technology's primary Roadmap.

### BR-CT04 — Readiness criteria (v1)

Certification readiness state `READY` is achieved when the employee has an active enrollment in a linked Technology and **all Stages** in that Technology's Roadmap are marked complete.

### BR-CT05 — Readiness is guidance

Readiness state is **guidance only**. Engineering Learning Hub does not block exam registration at the provider.

### BR-CT06 — No exam delivery

Engineering Learning Hub does not host, proctor, or score certification exams.

### BR-CT07 — Recorded certification

A Certification moves to `RECORDED` state for an employee only when a certificate submission is **approved** in My Certifications.

### BR-CT08 — Multiple certifications per Technology

A Technology may link to multiple Certifications.

---

## Initiative integration rules

### BR-IN01 — Optional linkage

A Company Initiative may optionally reference one Certification catalog entry via `linkedCertificationId`. The field is nullable.

### BR-IN02 — Unlinked initiatives unchanged

Initiatives without a linked Certification behave exactly as v0.7.1.

### BR-IN03 — Initiative does not gate Learn

Employees may complete Roadmaps and reach Certification readiness without any active Initiative.

### BR-IN04 — Initiative expiry

When an Initiative expires, Learn content and employee progress are unaffected.

### BR-IN05 — Initiative progress display

When linked, Initiative detail shows the authenticated employee's Roadmap progress and readiness for the linked Certification.

### BR-IN06 — Submit Certificate context

Navigating to Submit Certificate from a linked Initiative pre-fills `initiativeId`.

---

## Authorization rules

### BR-AU01 — Admin content management

All Learn content CRUD requires `ADMIN` role. Projects module CRUD follows existing Projects authorization (separate module).

### BR-AU02 — Employee read access

All authenticated users may browse published Learn content.

### BR-AU03 — Employee progress scope

Employees may read and write only their own progress and enrollment records.

### BR-AU04 — Employee 404 for draft

Employees requesting draft Learn content receive **404 Not Found** (not 403).

---

## Navigation & UX rules

### BR-UX01 — Sidebar rename

"My Submissions" sidebar label becomes **My Certifications** (path `/submissions` unchanged in v0.8.0).

### BR-UX02 — Admin sidebar rename

"Certificate Review" sidebar label becomes **Review Submissions** (path unchanged).

### BR-UX03 — Learn and Projects in primary nav

Both **Learn** and **Projects** appear in the primary sidebar as independent modules. Learn is position 2; Projects is position 3 (employee and admin).

### BR-UX04 — Terminology enforcement

User-facing copy in the Learn module must use approved terminology. The word "Project" in Learn UI refers only to **Related Organization Projects** (cross-nav links to the Projects module) — never to Practice Resources or owned entities.

### BR-UX05 — Next step prominence

Every enrolled employee viewing a Roadmap must see exactly one **Next up** Stage highlighted.

---

## Field limits (proposed)

| Entity | Field | Max length |
|--------|-------|------------|
| Career Path | title | 100 |
| Career Path | description | 2000 |
| Technology | name | 100 |
| Technology | shortName | 30 |
| Technology | description | 2000 |
| Stage | title | 150 |
| Stage | description | 3000 |
| Learning Resource | title | 200 |
| Learning Resource | url | 2048 |
| Practice Resource | title | 200 |
| Practice Resource | url | 2048 |
| Certification | name | 150 |
| Certification | description | 2000 |
| Certification | officialExamUrl | 2048 |

---

## Rules explicitly deferred (not v0.8.0)

| Topic | Reason |
|-------|--------|
| Automated link health checks | Operational tooling — post-launch |
| Quiz-based Stage completion | LMS territory |
| Manager-assigned learning | Role simplicity |
| Learn leaderboards | Scope control |
| Full Projects module UI redesign | Independent release track |
| Bulk resource import | Admin tooling — later phase |

---

**Next document:** [04-user-flows.md](./04-user-flows.md)
