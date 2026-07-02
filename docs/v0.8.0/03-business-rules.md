# v0.8.0 — Business Rules

**Module:** Learn  
**Status:** Draft for approval — authoritative rule set for implementation

---

## Rule categories

| Category | Prefix | Count |
|----------|--------|-------|
| Product & scope | BR-P | 5 |
| Content model | BR-C | 12 |
| Content lifecycle | BR-LC | 6 |
| Progress & enrollment | BR-PR | 10 |
| Learning Resources | BR-R | 6 |
| Learn Projects | BR-PJ | 5 |
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

### BR-P04 — Project Knowledge separation

The Project Knowledge Repository (`/projects`) is **separate** from Learn Projects. Learn Projects are practice recommendations; Project Knowledge is internal documentation.

### BR-P05 — Initiative independence

All Learn functionality must operate correctly when **zero** Company Initiatives are active. No Learn feature may require initiative participation.

---

## Content model rules

### BR-C01 — Career Path definition

A Career Path is an ordered collection of Technologies representing a professional direction. A Career Path does not contain Learning Resources or Stages directly.

### BR-C02 — Career Path minimum Technologies

A Career Path must reference **at least 2** Technologies to be published.

### BR-C03 — Career Path maximum Technologies

A Career Path may reference at most **12** Technologies. Exceeding requires product approval.

### BR-C04 — Technology uniqueness

Technology names must be unique (case-insensitive) within the catalog.

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

Publishing a Career Path requires all referenced Technologies to be `PUBLISHED` (or `ARCHIVED` with product approval exception — default: blocked).

### BR-LC06 — Archive cascade

Archiving a Technology does not auto-archive its Career Paths. Career Paths with archived required Technologies show a "Technology unavailable" indicator; new enrollments blocked.

---

## Progress & enrollment rules

### BR-PR01 — Self-reported progress

Stage and Project completion is **self-reported** by the employee in v0.8.0. No automated verification.

### BR-PR02 — Enrollment creation

Employees create enrollments via explicit action: **Start Career Path** or **Start Roadmap** (Technology). No admin assignment in v0.8.0.

### BR-PR03 — Concurrent enrollments

Employees may have **multiple active enrollments** simultaneously (e.g., one Career Path and two standalone Technologies).

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

Employees may **leave** an enrollment. Active status becomes `LEFT`. Progress data is preserved. Employee may re-enroll later.

### BR-PR10 — Admin progress immutability

Admins cannot modify employee progress records in v0.8.0.

---

## Learning Resources rules

### BR-R01 — External links only

Learning Resources are HTTPS URLs only. `javascript:`, `data:`, and `file:` schemes are rejected.

### BR-R02 — URL validation on save

Admin save validates URL format and reachability is **not** required at save time (deferred to periodic review).

### BR-R03 — New tab behaviour

All Learning Resource links open in a new browser tab with `rel="noopener noreferrer"`.

### BR-R04 — Resource attachment

A Learning Resource may be attached to multiple Stages (via resource library reuse). A Stage may have 0–20 Resources.

### BR-R05 — Resource ordering

Resources within a Stage are ordered. Default display: Official Documentation first, then OER, then others.

### BR-R06 — Paid resource disclosure

Resources marked `paid` display a visible **Paid** badge. Free resources display **Free** badge when type is known.

---

## Learn Projects rules

### BR-PJ01 — Project attachment

Learn Projects are attached to Stages. A Stage may have 0–5 Projects. Early Stages (order 1–2) should have 0 Projects (guideline, not enforced).

### BR-PJ02 — Project completion

Project completion is self-reported and independent of Stage completion. Completing all Projects does **not** auto-complete the Stage.

### BR-PJ03 — External project links

Project external links follow the same URL rules as Learning Resources (BR-R01).

### BR-PJ04 — Project difficulty

Difficulty is one of: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`.

### BR-PJ05 — Project naming

Learn Project titles must be unique within a Technology's Roadmap (case-insensitive).

---

## Certification rules

### BR-CT01 — Industry certifications only

The Learn Certification catalog contains **industry credentials** from external providers only. Internal company badges or initiative titles are not Certifications.

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

A Certification moves to `RECORDED` state for an employee only when a certificate submission is **approved** in My Certifications and optionally linked to the Certification catalog entry (future FK).

### BR-CT08 — Multiple certifications per Technology

A Technology may link to multiple Certifications (e.g., AWS CLF-C02 and AWS SAA-C03).

---

## Initiative integration rules

### BR-IN01 — Optional linkage

A Company Initiative may optionally reference one Certification catalog entry via `linkedCertificationId`. The field is nullable.

### BR-IN02 — Unlinked initiatives unchanged

Initiatives without a linked Certification behave exactly as v0.7.1. No Learn UI appears on unlinked initiative detail.

### BR-IN03 — Initiative does not gate Learn

Employees may complete Roadmaps and reach Certification readiness without any active Initiative.

### BR-IN04 — Initiative expiry

When an Initiative expires, Learn content and employee progress are unaffected. Initiative banner on Certification detail is removed.

### BR-IN05 — Initiative progress display

When linked, Initiative detail shows the authenticated employee's Roadmap progress and readiness for the linked Certification. Aggregate initiative progress (existing) is unchanged.

### BR-IN06 — Submit Certificate context

Navigating to Submit Certificate from a linked Initiative pre-fills `initiativeId`. Navigating from Learn Certification readiness pre-fills certification context (implementation detail in F23).

---

## Authorization rules

### BR-AU01 — Admin content management

All Learn content CRUD (Career Paths, Technologies, Roadmaps, Resources, Projects, Certifications) requires `ADMIN` role.

### BR-AU02 — Employee read access

All authenticated users may browse published Learn content.

### BR-AU03 — Employee progress scope

Employees may read and write only their own progress and enrollment records.

### BR-AU04 — Employee 404 for draft

Employees requesting draft or unpublished content receive **404 Not Found** (not 403), consistent with Initiative visibility pattern.

---

## Navigation & UX rules

### BR-UX01 — Sidebar rename

"My Submissions" sidebar label becomes **My Certifications** (path `/submissions` unchanged in v0.8.0).

### BR-UX02 — Admin sidebar rename

"Certificate Review" sidebar label becomes **Review Submissions** (path unchanged).

### BR-UX03 — Learn sidebar position

Learn appears as the **second** sidebar item (after Dashboard) for all authenticated users.

### BR-UX04 — Terminology enforcement

User-facing copy in the Learn module must use approved terminology (see `02-information-architecture.md` §8.1). Code identifiers may differ but Swagger descriptions should align.

### BR-UX05 — Next step prominence

Every enrolled employee viewing a Roadmap must see exactly one **Next up** Stage highlighted (first incomplete Stage by order).

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
| Learn Project | title | 150 |
| Learn Project | description | 3000 |
| Certification | name | 150 |
| Certification | description | 2000 |
| Certification | officialExamUrl | 2048 |

---

## Validation error principles

| Scenario | HTTP status | Pattern |
|----------|-------------|---------|
| Invalid field values | 400 | Field-level validation errors |
| Duplicate unique field | 409 | Business conflict message |
| Publish with invalid prerequisites | 400 | Clear rule reference (e.g., BR-C08) |
| Employee access to draft | 404 | Consistent with initiatives |
| Non-admin write attempt | 403 | Standard security response |

---

## Rules explicitly deferred (not v0.8.0)

| Topic | Reason |
|-------|--------|
| Automated link health checks | Operational tooling — post-launch |
| Quiz-based Stage completion | LMS territory |
| Manager-assigned learning | Role simplicity |
| Learn leaderboards | Scope control |
| Certificate-to-Certification FK on submission | F23+ |
| Multiple Roadmaps per Technology | Complexity |
| Bulk resource import | Admin tooling — later phase |

---

**Next document:** [04-user-flows.md](./04-user-flows.md)
