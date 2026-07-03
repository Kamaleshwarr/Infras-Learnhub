# v0.8.0 Learn Module — Implementation Plan

**Status:** **FINAL — frozen** (approved for F16 implementation after sign-off)  
**Feature numbering:** F16–F23 (continues from F15)  
**Depends on:** v0.7.1 Initiative Management (complete)  
**Classification:** Implementation planning — **no production code**

---

## Document purpose

This is the authoritative implementation plan for v0.8.0 Learn. It follows the same engineering process used for Initiative Management (F11–F15): each phase is independently implementable, testable, reviewable, and releasable.

**Product design reference:** `docs/v0.8.0/00-product-design.md` (approved v1.1, frozen)  
**Business rules reference:** `docs/v0.8.0/03-business-rules.md`

---

## Implementation guiding principles

### Engineering principles (from `.cursor/engineering-standards.md`)

1. **Vertical slices over horizontal layers** — every phase ships employee-visible and/or admin-usable value.
2. **Correctness before speed** — backend enforces business rules; frontend mirrors for fast feedback.
3. **Reuse before create** — follow `InitiativeListPage`, `CreateInitiativeDialog`, `ConfirmActionDialog`, `*Messages.ts`, `*FormState.ts` patterns.
4. **Minimal scope** — implement only the approved phase; no AI, no LMS features, no Projects ownership in Learn.
5. **Independently releasable** — each phase can merge without waiting for a future phase.

### Learn-specific principles

| Principle | Implementation implication |
|-----------|---------------------------|
| **Technologies are primary** | First employee slice is Technology browse/search; Career Paths come later as a complement |
| **Search is first-class** | Technology list search in F16; unified Learn search in F23; URL-synced query params throughout |
| **Progress grows incrementally** | F18 adds enrollment/progress; F21 adds certification readiness; each phase adds guidance layers |
| **Practice Resources are external links** | Never labeled "Project"; separate tables and UI sections from Learning Resources |
| **Certifications ≠ Initiatives** | Certification catalog in Learn (F21); optional Initiative association in F22 |
| **Projects stay independent** | Cross-navigation is a **relationship**, delivered in F16 with Technology pages — not a standalone phase |
| **Admin–employee parity** | Every phase pairs employee capability with admin management (except employee-only progress per BR-PR10) |
| **A learner should never wonder what to do next** | Every phase ships a **Next step** affordance (see per-phase table below) |

### UX principles (mandatory)

#### Principle 1 — Never wonder what to do next

> **"A learner should never wonder what to do next."**

Every completed feature must guide users to the next logical learning step.

#### Principle 2 — Four questions every page must answer

Every Learn page (and cross-linked surfaces) must answer:

| # | Question | Implementation guidance |
|---|----------|-------------------------|
| 1 | **Where am I?** | Breadcrumbs, tab highlight, clear `PageHeader` title |
| 2 | **What is this?** | Description, metadata chips, context copy |
| 3 | **What should I do next?** | Primary CTA, **Next up**, readiness state |
| 4 | **Where do I go next?** | Links to Roadmap, Technology, Certification, Projects, or Continue Learning |

Apply this checklist in every phase's manual QA review.

| Phase | Next-step affordance | Four-questions focus |
|-------|---------------------|----------------------|
| F16 | Technology detail → **View Roadmap**; optional → Related Organization Projects | Breadcrumbs on Technology detail |
| F17 | Roadmap → **Start with Stage 1** | Stage expand; resource links |
| F18 | **Next up** Stage; **Continue Learning** | My Journey context |
| F19 | Practice Resources after study resources | Separate section labels |
| F20 | Career Path → **Start with first Technology** | Path → Technology links |
| F21 | **Continue Roadmap** or **Visit official provider** | Readiness badge |
| F23 | Dashboard **Continue Learning** widget | Dashboard entry point |

### Admin–employee parity (mandatory)

Each phase delivers **employee and admin capabilities together** in one vertical slice. Do not defer admin CRUD to a later admin-only phase.

| Phase | Employee | Admin |
|-------|----------|-------|
| **F16** | Browse/search Technologies; Technology detail; optional Related Organization Projects | Create/edit/publish/archive Technology; manage Technology ↔ Project links |
| **F17** | View Roadmap, Stages, Learning Resources | Roadmap editor; Stage CRUD; Learning Resource curation |
| **F18** | Enroll; Stage progress; My Journey; Continue Learning | *N/A — progress is employee self-service (BR-PR10)* |
| **F19** | View Practice Resources on Stages | Practice Resource curation |
| **F20** | Browse/start Career Paths | Career Path CRUD; publish/archive |
| **F21** | Browse Certifications; readiness CTAs | Certification CRUD; link Technologies |
| **F22** | Initiative progress card; optional Certification banner | Link Certification on Initiative (optional field) |
| **F23** | Dashboard widgets; unified search | Settings shell; release QA |

---

## Roadmap revision summary

The prior proposal (all schema in F16, read-only employee slices before admin, monolithic F22 admin) was **reorganized**:

| Issue in prior plan | Resolution |
|---------------------|------------|
| F16 foundation-only (no user value) | **Eliminated** — F16 is first vertical slice (Technology discovery + admin create) |
| Employee read long before admin write | **Admin write paired** with each employee slice from F16 onward |
| Career Paths before Progress (F18 before F19) | **Reordered** — Progress (F18) before Career Paths (F20) |
| Monolithic admin-only phase | **Eliminated** — admin CRUD distributed F16–F21; cross-nav merged into F16 |
| Standalone cross-navigation phase | **Eliminated** — Technology ↔ Project links are a relationship, not a business feature |
| Search deferred to v0.9 | **Elevated** — list search F16; unified Learn search F23 |
| Learning Resources separate from Roadmap (F20 after F17) | **Merged** — F17 delivers Roadmap + Learning Resources together |

---

## Phase overview

| Phase | Name | Employee value | Admin value | Complexity |
|-------|------|----------------|-------------|------------|
| **F16** | Technology Discovery & Search | Browse/search Technologies; optional Related Organization Projects | Create/edit/publish Technology; Technology ↔ Project links | **M** |
| **F17** | Roadmap & Learning Resources | View Roadmap, Stages, study links | Roadmap editor, Learning Resource curation | **L** |
| **F18** | Progress & Learning Journey | Enroll, complete Stages, My Journey | *(employee self-service only — BR-PR10)* | **L** |
| **F19** | Practice Resources | Hands-on external links on Stages | Practice Resource curation | **M** |
| **F20** | Career Paths | Browse/start Career Paths | Career Path CRUD | **M** |
| **F21** | Industry Certifications | Catalog, readiness, provider CTA | Certification CRUD | **M** |
| **F22** | Optional Initiative Association | Initiative progress; optional Certification banner | Link Certification on Initiative (optional) | **S** |
| **F23** | Dashboard, Unified Search & Release | Dashboard widgets, Learn home polish | Settings shell; release readiness | **M** |

**Total phases:** 8 (F16–F23)  
**Flyway migrations:** Incremental per phase (no big-bang schema)

```mermaid
flowchart LR
    F16[F16 Technologies + cross-nav] --> F17[F17 Roadmap]
    F17 --> F18[F18 Progress]
    F18 --> F19[F19 Practice]
    F18 --> F20[F20 Career Paths]
    F18 --> F21[F21 Certifications]
    F21 --> F22[F22 Optional Initiative Assoc]
    F19 --> F23[F23 Release]
    F20 --> F23
    F21 --> F23
    F22 --> F23
```

---

## Shared conventions (all phases)

### Backend package

```text
com.company.learninghub.learn/
├── controller/
├── domain/
├── dto/
├── mapper/
├── repository/
└── service/
```

### Frontend structure

```text
frontend/src/
├── api/learnApi.ts
├── types/learn.ts
├── pages/learn/
├── components/learn/
│   ├── learnMessages.ts
│   ├── learnFormState.ts
│   └── learnListParams.ts
```

### API base path

`/api/v1/learn/...` — employee read paths  
`/api/v1/learn/manage/...` — admin write paths

### Content status enum

`DRAFT` | `PUBLISHED` | `ARCHIVED` — employees see `PUBLISHED` only (404 for draft)

### Standard PR deliverables (each phase)

1. Files changed  
2. Tests added/updated  
3. Test results  
4. Build results  
5. Risks/issues  
6. Acceptance checklist  
7. Manual QA checklist  

---

# F16 — Technology Discovery & Search

## Objective

Deliver the first usable Learn vertical slice: employees browse and search Technologies; admins create and publish Technologies. Includes **Technology ↔ Project cross-navigation** as an optional relationship (not a standalone feature). Establishes Learn navigation, module shell, and search-first Technology browsing.

## Scope

First end-to-end Learn capability centered on **Technologies as the primary entry point**, including lightweight cross-links to the independent Projects module.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| Browse/search Technologies | Create Technology |
| Technology detail | Edit Technology |
| Related Organization Projects (read) | Publish / Archive Technology |
| Navigate to `/projects/{id}` | Manage Technology ↔ Project links in Edit dialog |

## In scope

- Sidebar navigation update (Learn position 2, Projects position 3, label renames)
- Learn module shell: routes, tab navigation, `PageHeader` pages
- `learnApi.ts`, `types/learn.ts`, `learnMessages.ts`, `learnListParams.ts`
- Technology entity, repository, service, controller
- Employee: Technology list (search, category, difficulty filters, pagination, URL query sync)
- Employee: Technology detail (metadata, **View Roadmap** CTA — placeholder until F17 if roadmap missing)
- Employee: **Related Organization Projects** on Technology detail (optional; links to `/projects/{id}`)
- Employee: Learn home with Technology-first discovery and search entry
- Admin: Technology list with status filter tabs (DRAFT / PUBLISHED / ARCHIVED)
- Admin: Create Technology dialog, Edit Technology dialog
- Admin: Publish / Archive Technology (dedicated actions)
- Admin: **Technology ↔ Project link picker** in Edit Technology dialog
- **Cross-navigation (relationship, not a feature):**
  - `learn_technology_project_links` junction (FK → existing `projects` table)
  - Projects module: **Related Technologies** read-only card on project detail (minimal addition to existing `/projects` page — links to `/learn/technologies/{id}`)
- Flyway `V12__learn_technologies.sql` (includes `learn_technologies` + `learn_technology_project_links`)
- Dev seed: 2–3 sample Technologies for QA (Flyway seed or test fixtures — not production)

## Out of scope

- Roadmaps, Stages, Resources (F17)
- Progress, enrollment (F18)
- Career Paths (F20)
- Certifications (F21)
- Creating organizational Projects from Learn (Projects module owns Projects)
- Unified cross-entity search (F23 — list search only here)
- Settings page (F23)
- Dashboard widgets (F23)
- AI features

## Backend work

| Item | Detail |
|------|--------|
| Migration `V12` | `learn_technologies` + `learn_technology_project_links` (technology_id, project_id; FK → `projects`) |
| `LearnTechnologyService` | CRUD, publish, archive, employee visibility filter |
| `GET /api/v1/learn/technologies` | Paginated; `search`, `category`, `difficulty`; employees: PUBLISHED only |
| `GET /api/v1/learn/technologies/{id}` | Detail with `relatedProjects[]`; employee 404 if not PUBLISHED |
| `POST/DELETE .../manage/technologies/{id}/project-links` | Admin link/unlink organizational Projects |
| `GET /api/v1/learn/manage/technologies` | Admin list with status filter |
| `POST /api/v1/learn/manage/technologies` | Create (DRAFT) |
| `PUT /api/v1/learn/manage/technologies/{id}` | Update metadata |
| `POST /api/v1/learn/manage/technologies/{id}/publish` | Publish validation |
| `POST /api/v1/learn/manage/technologies/{id}/archive` | Archive |
| Projects API (coordinate) | Extend project detail `GET` to include `relatedTechnologies[]` summaries |
| Authorization | Employee read authenticated; admin write `@PreAuthorize("hasRole('ADMIN')")` |
| OpenAPI | `@Tag`, `@Operation` on all endpoints |

## Frontend work

| Item | Detail |
|------|--------|
| `AppRoutes` | `/learn`, `/learn/technologies`, `/learn/technologies/:id`, `/learn/manage`, `/learn/manage/technologies` |
| `navigation.tsx` | Learn, Projects retained, My Certifications / Review Submissions renames |
| `LearnLayout` | Tab bar: Home, Technologies, (placeholders for later tabs) |
| `TechnologyListPage` | Reuse `InitiativeListPage` patterns: toolbar, filters, URL sync, desktop table + mobile cards |
| `TechnologyDetailPage` | Metadata, category/difficulty chips, **View Roadmap** CTA, **RelatedOrganizationProjectsCard** |
| `RelatedTechnologiesCard` | On existing Projects detail page (`/projects/:id`) — minimal read-only card |
| `TechnologyProjectLinkPicker` | Admin multi-select in Edit Technology dialog |
| `LearnHomePage` | Hero, Technology search input, featured Technologies grid |
| `CreateTechnologyDialog` / `EditTechnologyDialog` | `maxWidth="md"`, validation, dirty guard |
| `TechnologyStatusChip` | DRAFT / PUBLISHED / ARCHIVED |
| `LearnManagementSnackbar` | Success feedback |
| Admin Manage tab | Visible only when `isAdmin` |

## Shared models

| Type | Fields (indicative) |
|------|---------------------|
| `TechnologyResponse` | id, name, shortName, description, category, difficulty, status, featured, relatedProjects[], createdAtUtc, updatedAtUtc |
| `RelatedProjectSummary` | id, name, businessDomain? |
| `RelatedTechnologySummary` | id, name, shortName |
| `TechnologyCreateRequest` | name, shortName, description, category, difficulty |
| `TechnologyUpdateRequest` | same as create |
| `TechnologyListParams` | search, category, difficulty, status (admin), page, size, sort |

## Validation rules

| Field | Rule |
|-------|------|
| name | Required; max 100; unique case-insensitive |
| shortName | Required; max 30 |
| description | Optional; max 2000 |
| category | Required; enum |
| difficulty | Required; BEGINNER \| INTERMEDIATE \| ADVANCED |

## Business rules

| ID | Rule |
|----|------|
| BR-C04 | Technology name unique (case-insensitive) |
| BR-LC01–03 | Status DRAFT / PUBLISHED / ARCHIVED |
| BR-LC02 | Employees 404 on DRAFT |
| BR-AU01 | Admin-only write |
| BR-AU02 | All authenticated users browse published |
| BR-UX03 | Learn position 2, Projects position 3 |
| BR-UX04 | Terminology: Technology, not Course |
| BR-XN01–05 | Cross-navigation as optional relationship |
| BR-M01–05 | Projects module independence |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend service | Create, update, publish, archive, uniqueness, employee visibility, project link CRUD |
| Backend controller | List filters, 404 draft for employee, project link 409 on duplicate |
| Backend security | `@PreAuthorize` on manage endpoints |
| Frontend unit | `learnListParams` parse/build, form validation |
| Frontend component | Technology list render, create dialog validation, admin-only controls hidden for employee |
| Regression | Initiatives, submissions, sidebar unchanged paths |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Employee opens `/learn` — sees Technology-first home |
| 2 | Employee searches Technologies — results filter; URL updates |
| 3 | Employee opens Technology detail — sees metadata and View Roadmap CTA |
| 4 | Employee cannot see DRAFT Technologies |
| 5 | Admin creates Technology as DRAFT — appears in admin list |
| 6 | Admin publishes Technology — visible to employee |
| 7 | Admin archives Technology — hidden from employee browse |
| 8 | Employee sidebar: Learn, Projects, no admin Manage tab |
| 9 | Admin sidebar: Learn with Manage tab |
| 10 | Duplicate Technology name rejected |
| 11 | Technology with linked Projects shows Related Organization Projects |
| 12 | Click project link navigates to `/projects/{id}` |
| 13 | Project detail shows Related Technologies (when links exist) |
| 14 | Admin adds/removes Technology ↔ Project link in Edit dialog |
| 15 | Zero links — empty state; Learn fully functional |
| 16 | Four-questions UX: breadcrumb, description, View Roadmap CTA, cross-nav links |

## Risks

| Risk | Mitigation |
|------|------------|
| Nav rename confusion | Release notes; optional one-time tooltip |
| Empty catalog at first deploy | Dev seed + admin publish guide in PR |
| Scope creep into Roadmap | Strict phase gate; View Roadmap can 404 until F17 |

## Dependencies

- v0.7.1 merged (stable Initiatives, auth, layout)
- None from future Learn phases

## Acceptance criteria

- [ ] Employee can browse and search published Technologies
- [ ] Admin can create, edit, publish, archive Technologies
- [ ] Optional Technology ↔ Project cross-nav works (both directions)
- [ ] Learn appears in sidebar; Projects remains independent
- [ ] Four-questions UX checklist passed on Technology pages
- [ ] All automated tests pass; build passes
- [ ] Manual QA checklist complete
- [ ] PR references `feat(v0.8.0): F16 technology discovery and search`

## Estimated complexity

**M (Medium)** — new module bootstrap + one entity CRUD + list/search UI; patterns exist from Initiatives.

---

# F17 — Roadmap & Learning Resources

## Objective

Employees view a full Technology Roadmap with ordered Stages and curated Learning Resources (external links). Admins build Roadmaps and curate resources. Delivers core "how do I learn this?" guidance.

## Scope

Roadmap read/write for a Technology, Stage sequence, Learning Resource library and Stage attachment.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| View Roadmap, Stages, Learning Resources | Roadmap editor; Stage CRUD; reorder |
| Start with Stage 1 guidance | Learning Resource library CRUD; attach to Stages |
| | Publish Technology (with roadmap validation) |

## In scope

- `learn_roadmaps`, `learn_roadmap_stages`, `learn_learning_resources`, `learn_stage_learning_resources`
- Flyway `V13__learn_roadmaps_and_learning_resources.sql`
- Employee: Roadmap page with vertical stepper, Stage expand/collapse
- Employee: Learning Resources per Stage — external links, new tab, type badges
- Employee: **Start with Stage 1** / first Stage expanded by default (next-step guidance)
- Admin: Roadmap editor (split panel: Stage list + Stage detail)
- Admin: Stage CRUD, drag reorder
- Admin: Learning Resource library CRUD
- Admin: Attach/detach resources to Stages
- Publish validation: ≥ 3 Stages, each Stage ≥ 1 Learning Resource to publish Technology (BR-C08, BR-LC05)
- Technology detail **View Roadmap** CTA active

## Out of scope

- Practice Resources (F19)
- Enrollment / progress checkmarks (F18)
- Career Paths (F20)
- Certifications (F21)
- Projects cross-links (merged into F16)
- Resource visit tracking (optional — defer to F18 if needed)

## Backend work

| Item | Detail |
|------|--------|
| Migration `V13` | roadmaps (1:1 technology), stages, learning_resources, stage_learning_resources junction |
| `GET .../technologies/{id}/roadmap` | Ordered stages with nested learning resources |
| Manage stage endpoints | CRUD + reorder `PUT .../stages/reorder` |
| Manage resource endpoints | CRUD library + attach/detach |
| Publish Technology | Extended validation: roadmap ≥ 3 stages, each stage ≥ 1 resource |
| URL validation | BR-LR01 on resource save |

## Frontend work

| Item | Detail |
|------|--------|
| `RoadmapPage` | MUI Stepper or timeline; Stage cards |
| `StageLearningResourcesList` | Type badge, free/paid, external link icon |
| `RoadmapEditorPage` (admin) | Split panel; Stage list reorder; resource attachment |
| `LearningResourceFormDialog` | URL, title, type, provider, paid flag |
| `learnMessages` | Stage, resource, publish validation copy |
| Empty states | "No resources on this Stage" with admin hint |

## Shared models

| Type | Fields |
|------|--------|
| `RoadmapResponse` | technologyId, stages[] |
| `StageResponse` | id, title, description, order, estimatedEffort, learningResources[] |
| `LearningResourceResponse` | id, title, url, type, provider, freePaid, estimatedMinutes, order |
| `StageCreateRequest` | title, description, order, estimatedEffort |

## Validation rules

| Field | Rule |
|-------|------|
| Stage title | Required; max 150 |
| Stage description | Optional; max 3000 |
| Resource URL | HTTPS; max 2048; BR-LR01 |
| Resource title | Required; max 200 |
| Stage order | Contiguous 1..n |

## Business rules

| ID | Rule |
|----|------|
| BR-C05–09 | One roadmap per technology; 3–20 stages |
| BR-C07 | Contiguous stage order |
| BR-LR01–06 | Learning Resource rules |
| BR-LC05 | Publish requires published technologies in career path (N/A here) |
| BR-UX05 | First incomplete stage = Next up (static: Stage 1 pre-F18) |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend | Stage ordering, publish validation, resource URL rejection, employee roadmap 404 when draft |
| Frontend | Stepper render, external links `target="_blank"`, editor reorder |
| Integration | Publish blocked with < 3 stages |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Employee opens Roadmap — sees ordered Stages |
| 2 | Stage 1 expanded by default with Next step label |
| 3 | Learning Resource opens in new tab |
| 4 | Admin creates 3 Stages with resources and publishes |
| 5 | Publish blocked with 2 Stages — clear error |
| 6 | Admin reorders Stages — employee view reflects order |
| 7 | Paid resource shows Paid badge |
| 8 | Technology without roadmap shows meaningful empty state |

## Risks

| Risk | Mitigation |
|------|------------|
| Roadmap editor UX complexity | Split panel; one Stage at a time; follow Initiative dialog patterns |
| External link security | URL scheme allowlist server-side |

## Dependencies

- **F16** complete (Technology entity, publish flow)

## Acceptance criteria

- [ ] Employee views full Roadmap with Learning Resources
- [ ] Admin builds Roadmap and publishes Technology with validation
- [ ] Stage 1 guidance visible ("Start here" / Next up)
- [ ] No Practice Resources or progress in this phase

## Estimated complexity

**L (Large)** — roadmap editor + nested resources + publish validation.

---

# F18 — Progress & Learning Journey

## Objective

Employees enroll in Technologies, mark Stages complete, and track progress via My Journey and Continue Learning. Delivers the **Learning Journey** as the product centerpiece.

## Scope

Enrollment, stage progress, My Journey page, next-step guidance based on progress.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| Enroll; mark Stages complete; My Journey; Continue Learning | *N/A — employee self-service only (BR-PR10)* |

> Progress is intentionally employee-owned. Admins manage content in F16–F17; they do not edit learner progress.

## In scope

- Flyway `V14__learn_progress.sql`
- `learn_learning_enrollments`, `learn_stage_progress`
- Employee: **Start Roadmap** / enroll on Technology detail or Roadmap
- Employee: Mark Stage complete / incomplete (toggle)
- Employee: Progress bar, checkmarks on stepper, **Next up** (first incomplete Stage)
- Employee: **My Journey** page (`/learn/journey`)
- Employee: **Continue Learning** card on Learn home
- Leave enrollment with confirm dialog
- Optional: `learn_resource_visits` (mark visited — lightweight)

## Out of scope

- Career Path enrollment (F20)
- Certification readiness (F21)
- Practice Resource completion tracking (F19)
- Admin progress editing (forbidden BR-PR10)
- Dashboard widget (F23)

## Backend work

| Item | Detail |
|------|--------|
| `POST /api/v1/learn/enrollments` | technologyId and/or careerPathId (careerPathId inactive until F20) |
| `DELETE /api/v1/learn/enrollments/{id}` | Leave enrollment |
| `POST /api/v1/learn/stage-progress` | Mark complete/incomplete |
| `GET /api/v1/learn/journey` | Active, completed, left enrollments with progress % |
| Business logic | BR-PR01–PR10 |

## Frontend work

| Item | Detail |
|------|--------|
| `MyJourneyPage` | Enrollment cards, progress rings |
| Roadmap progress UI | Checkmarks, progress bar, Next up highlight |
| `ContinueLearningCard` | Learn home — resume link to next Stage |
| Enroll / Leave CTAs | Confirm dialogs |
| `learnMessages` | Enrollment, progress, leave copy |

## Shared models

| Type | Fields |
|------|--------|
| `EnrollmentResponse` | id, technologyId, careerPathId?, status, enrolledAt, progressPercent, nextStageId? |
| `StageProgressRequest` | stageId, complete: boolean |
| `JourneyResponse` | active[], completed[], left[] |

## Validation rules

| Rule | Detail |
|------|--------|
| Duplicate enrollment | 409 if active enrollment exists for same technology |
| Stage belongs to enrolled technology | 400 if stage not in technology roadmap |
| Archived technology | No new enrollments |

## Business rules

| ID | Rule |
|----|------|
| BR-PR01–10 | All progress rules |
| BR-UX05 | Next up = first incomplete stage by order |
| UX principle | Never wonder what's next — Continue Learning always shows next Stage when enrolled |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend | Enrollment create/leave, duplicate block, progress toggle, progress % calculation |
| Frontend | Enroll flow, checkmark update, My Journey lists |
| Regression | F16–F17 read paths unchanged for non-enrolled users |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Employee starts Roadmap — enrollment created |
| 2 | Mark Stage 1 complete — progress updates; Next up moves to Stage 2 |
| 3 | Toggle Stage back to incomplete — readiness recalculates |
| 4 | My Journey shows active enrollment |
| 5 | Continue Learning on Learn home links to next Stage |
| 6 | Leave enrollment — moves to Left section; can re-enroll |
| 7 | Duplicate enroll blocked |
| 8 | Non-enrolled user sees Start Roadmap, not checkmarks |

## Risks

| Risk | Mitigation |
|------|------------|
| Optimistic UI race | Pessimistic refresh on progress toggle or version field |
| Empty Journey on first visit | Clear CTA to browse Technologies |

## Dependencies

- **F17** complete (Roadmap with Stages)

## Acceptance criteria

- [ ] Full enroll → progress → My Journey loop works
- [ ] Next up and Continue Learning always visible when enrolled
- [ ] BR-PR10: admin cannot edit employee progress

## Estimated complexity

**L (Large)** — new progress domain + multiple UI surfaces.

---

# F19 — Practice Resources

## Objective

Add curated **Practice Resources** (external hands-on links) to Roadmap Stages. Admins curate; employees access practice exercises separately from study resources.

## Scope

Practice Resource entity, Stage attachment, employee display, optional completion toggle.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| View Practice Resources on Stages (separate section) | Practice Resource library CRUD |
| Mark Completed (optional) | Attach Practice Resources to Stages in roadmap editor |

## In scope

- Flyway `V15__learn_practice_resources.sql`
- Practice Resource library + Stage attachment (mirror Learning Resource pattern)
- Employee: Practice Resources section per Stage (separate from Learning Resources)
- Employee: Optional Mark Completed toggle
- Admin: Practice Resource CRUD in library and Roadmap editor
- Terminology enforcement: **Practice Resource** never "Project"

## Out of scope

- Organizational Projects / cross-nav (delivered in F16)
- Certification readiness changes
- Hosting practice content

## Backend work

| Item | Detail |
|------|--------|
| Tables | `learn_practice_resources`, `learn_stage_practice_resources`, optional `learn_practice_resource_progress` |
| APIs | CRUD manage + include in roadmap GET response |
| URL validation | BR-PA01 |
| Types enum | LAB, CODING_EXERCISE, GUIDED_TUTORIAL, SANDBOX |

## Frontend work

| Item | Detail |
|------|--------|
| `StagePracticeResourcesList` | Difficulty badge, time estimate, external link |
| Roadmap editor tab | Practice Resources on Stage detail |
| `PracticeResourceFormDialog` | Separate from Learning Resource dialog |

## Shared models

| Type | Fields |
|------|--------|
| `PracticeResourceResponse` | id, title, url, type, difficulty, estimatedMinutes, order |

## Validation rules

| Field | Rule |
|-------|------|
| title | Required; max 200 |
| url | HTTPS; BR-PA01 |
| difficulty | BEGINNER \| INTERMEDIATE \| ADVANCED |

## Business rules

| ID | Rule |
|----|------|
| BR-PA01–06 | All Practice Resource rules |
| BR-M04 | Not organizational Projects |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend | URL validation, attach/detach, separate from learning resources |
| Frontend | Separate UI sections; no "Project" label in copy tests |
| Copy audit | `learnMessages` snapshot for terminology |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Stage shows Learning Resources and Practice Resources in separate sections |
| 2 | Practice link opens new tab |
| 3 | Mark Completed toggles independently of Stage completion |
| 4 | Admin adds practice resource via editor |
| 5 | UI never uses "Project" for practice items |
| 6 | Stage with zero practice resources shows appropriate empty state |

## Risks

| Risk | Mitigation |
|------|------------|
| Terminology confusion with Projects module | BR-PA03; QA checklist item 5 |

## Dependencies

- **F17** (Roadmap editor)
- **F18** recommended (progress context) but not blocking

## Acceptance criteria

- [ ] Practice Resources live on Stages as external links
- [ ] Clear separation from Learning Resources and Projects module

## Estimated complexity

**M (Medium)** — mirrors Learning Resource pattern.

---

# F20 — Career Paths

## Objective

Add **Career Paths** as a complement to Technology-first browsing. Employees discover multi-technology paths; admins curate ordered Technology collections.

## Scope

Career Path entity, employee browse/detail/start, admin CRUD, Learn home featured paths.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| Browse/search Career Paths | Create/edit Career Paths |
| Start Career Path | Publish/archive; order Technologies (required/elective) |

## In scope

- Flyway `V16__learn_career_paths.sql`
- Career Path list, detail, featured filter
- Employee: browse, search (within career path list), start career path
- Start career path → enroll in career path + first required Technology (BR-PR02)
- Admin: Career Path CRUD, Technology ordering, required/elective flags
- Learn home: Featured Career Paths section (secondary to Technologies)
- Learn tab: Career Paths enabled

## Out of scope

- Career Path progress % (derive from Technology progress — use existing F18)
- Certifications
- New progress tables

## Backend work

| Item | Detail |
|------|--------|
| Tables | `learn_career_paths`, `learn_career_path_technologies` |
| APIs | List, detail, manage CRUD, publish, archive |
| Publish validation | ≥ 2 technologies (BR-C02); all required technologies PUBLISHED |
| Start career path | Creates career path enrollment + technology enrollment |

## Frontend work

| Item | Detail |
|------|--------|
| `CareerPathListPage` | Cards with technology count, duration |
| `CareerPathDetailPage` | Ordered technology list with progress if enrolled |
| `CreateCareerPathDialog` / editor | Technology picker with order, required/elective |
| Learn home | Featured Career Paths below Technology search |

## Shared models

| Type | Fields |
|------|--------|
| `CareerPathResponse` | id, title, description, estimatedDuration, featured, status, technologies[] |
| `CareerPathTechnologyResponse` | technologyId, name, order, required |

## Validation rules

| Field | Rule |
|-------|------|
| title | Max 100 |
| description | Max 2000 |
| technologies | Min 2 to publish |

## Business rules

| ID | Rule |
|----|------|
| BR-C01–03, BR-C10–12 | Career path rules |
| BR-C10 | Shared technology progress across paths |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend | Publish validation, start enrolls first technology, shared progress |
| Frontend | Career path detail links to technologies |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Employee browses Career Paths |
| 2 | Start Career Path enrolls and lands on first Technology Roadmap |
| 3 | Technology progress reflected on Career Path detail |
| 4 | Publish blocked with 1 Technology |
| 5 | Featured paths appear on Learn home |
| 6 | Technologies tab remains primary entry (hero/search) |

## Risks

| Risk | Mitigation |
|------|------------|
| Career Paths overshadow Technologies | IA: Technology-first home; Career Paths secondary tab |

## Dependencies

- **F16** (Technologies)
- **F18** (Enrollment — for start career path flow)

## Acceptance criteria

- [ ] Career Paths complement (not replace) Technology browsing
- [ ] Start Career Path guides to first Technology (next-step principle)

## Estimated complexity

**M (Medium)**

---

# F21 — Industry Certifications

## Objective

Employees browse Industry Certifications, see readiness based on Roadmap progress, and navigate to official providers. Admins maintain the certification catalog.

## Scope

Certification entity, Technology linkage, readiness calculation, employee CTAs.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| Browse/search Certifications; readiness CTAs | Create/edit Certifications |
| Visit official provider (external) | Link Technologies; publish |

## In scope

- Flyway `V17__learn_certifications.sql`
- Certification list (search, provider, level filters)
- Certification detail with readiness badge: NOT_STARTED → IN_PROGRESS → READY
- Readiness = all Stages complete in linked Technology (BR-CT04)
- **Continue Roadmap** / **Visit official provider** CTAs
- Admin: Certification CRUD, link Technologies, publish
- Learn tab: Certifications enabled
- Certifications distinct from Initiatives (no initiative UI here)

## Out of scope

- Initiative link (F22)
- RECORDED state automation from submissions (optional display only if submission exists)
- Exam scheduling

## Backend work

| Item | Detail |
|------|--------|
| Tables | `learn_certifications`, `learn_certification_technologies` |
| `GET .../certifications/{id}/readiness` | Per authenticated employee |
| Readiness service | Derives from F18 stage progress |

## Frontend work

| Item | Detail |
|------|--------|
| `CertificationListPage` | Provider, level filters |
| `CertificationDetailPage` | Readiness chip, linked Roadmap link, external exam CTA |
| `CreateCertificationDialog` | officialExamUrl required |

## Shared models

| Type | Fields |
|------|--------|
| `CertificationResponse` | id, name, provider, level, description, officialExamUrl, status, linkedTechnologies[] |
| `CertificationReadinessResponse` | state, linkedTechnologyId, progressPercent |

## Validation rules

| Field | Rule |
|-------|------|
| name | Max 150 |
| officialExamUrl | Required to publish; HTTPS |
| linkedTechnologies | Min 1 |

## Business rules

| ID | Rule |
|----|------|
| BR-CT01–08 | Certification rules |
| BR-IN03 | Learn works without initiatives |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend | Readiness state machine, official URL required on publish |
| Frontend | CTA changes by readiness state |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Certification list searchable |
| 2 | NOT_STARTED shows Start Roadmap |
| 3 | IN_PROGRESS shows Continue Roadmap |
| 4 | READY shows Visit official provider (new tab) |
| 5 | Admin creates and publishes certification |
| 6 | No Initiative UI on certification detail (F22 adds optional banner) |

## Risks

| Risk | Mitigation |
|------|------------|
| Readiness false positives | Self-reported stage completion — acceptable per design |

## Dependencies

- **F18** (Progress for readiness)

## Acceptance criteria

- [ ] Readiness derives from Stage completion
- [ ] Clear next-step CTA for each readiness state

## Estimated complexity

**M (Medium)**

---


# F22 — Optional Initiative Association

> **Former name:** ~~Initiative Integration~~ — Learn does **not** depend on Initiatives. Industry Certifications remain independent. Company Initiatives may **optionally** reference a Certification.

## Objective

Allow admins to optionally associate a Company Initiative with an Industry Certification. When linked, show Roadmap progress on Initiative detail and an active-initiative banner on Certification detail. **Learning never depends on an Initiative.**

## Scope

Nullable FK on initiatives, read-only cross-module surfaces, Submit Certificate contextual entry.

## Admin–employee parity

| Employee | Admin |
|----------|-------|
| View Certification progress on Initiative detail (when linked) | Set/clear **Linked Certification** on Initiative edit |
| Optional initiative banner on Certification detail | Same optional field on edit |

## In scope

- Flyway `V18__initiative_certification_link.sql`
- `learning_initiatives.linked_certification_id` nullable FK
- Edit Initiative dialog: optional Certification dropdown
- Initiative detail: employee Roadmap progress card when linked
- Certification detail: active initiative banner when applicable
- Submit Certificate: pre-fill `initiativeId` from initiative; certification context from Learn
- Unlinked initiatives: unchanged (regression)

## Out of scope

- Initiative lifecycle changes (F14 rules unchanged)
- Learn content changes
- Mandatory initiative participation

## Backend work

| Item | Detail |
|------|--------|
| Migration | Nullable FK with ON DELETE SET NULL or RESTRICT (document choice) |
| Initiative DTO | `linkedCertification` summary |
| Initiative detail | Include employee readiness when linked |

## Frontend work

| Item | Detail |
|------|--------|
| `EditInitiativeDialog` | Certification searchable select |
| `InitiativeCertificationProgressCard` | On initiative detail |
| `ActiveInitiativeBanner` | On certification detail |

## Business rules

| ID | Rule |
|----|------|
| BR-IN01–06 | Initiative **association** rules (optional) |
| BR-P05–06 | Independence |

## Test strategy

| Layer | Tests |
|-------|-------|
| Backend | Link/unlink, unlinked regression, F14 lifecycle unchanged |
| Frontend | Banner visibility only when initiative ACTIVE + visible |
| E2E | Initiative → Learn → back; zero initiatives regression |

## Manual QA checklist

| # | Scenario |
|---|----------|
| 1 | Link certification to initiative — progress shows on detail |
| 2 | Unlinked initiative unchanged from v0.7.1 |
| 3 | Expired initiative — banner removed from certification |
| 4 | Learn works with zero initiatives |
| 5 | F14 lifecycle regression |
| 6 | UI copy uses "association" / "optional" — not "integration" or "required" |

## Risks

| Risk | Mitigation |
|------|------------|
| Tight coupling | FK optional only; Learn never requires initiative |

## Dependencies

- **F21** (Certification catalog)
- v0.7.1 Initiative module

## Acceptance criteria

- [ ] Optional link works; unlinked path unchanged
- [ ] BR-IN03 enforced

## Estimated complexity

**S (Small)**

---

# F23 — Dashboard, Unified Search & Release

## Objective

Complete v0.8.0 release: Dashboard widgets, unified Learn search, Settings shell, navigation polish, documentation, full regression QA.

## Scope

Cross-cutting polish, unified search, release artifacts — no new core domain entities.

## In scope

- **Unified Learn search** on Learn home: searches Technologies, Career Paths, Certifications in one query (`GET /api/v1/learn/search?q=`)
- Dashboard **Continue Learning** widget (employee)
- Dashboard **Learn Content Health** widget (admin) — counts only
- Settings page shell (`/settings`) with Study Materials link
- Learn home polish: hero, featured Technologies, featured Career Paths, Certifications entry
- Notifications remain header-only (sidebar cleanup if not done in F16)
- `docs/releases/release-v0.8.0.md`
- Update `docs/project-roadmap.md`, `.cursor/project-context.md`
- Full manual QA pass across F16–F22
- Launch content verification checklist (seed/production catalog)

## Out of scope

- Global app-wide search (v0.9+)
- AI features
- Email notifications
- Learn leaderboards
- Full Projects module build-out
- Employee certificate download

## Backend work

| Item | Detail |
|------|--------|
| `GET /api/v1/learn/search` | Unified search across published technologies, career paths, certifications |
| Dashboard APIs | Optional aggregate endpoints or reuse existing list APIs with size=1 |

## Frontend work

| Item | Detail |
|------|--------|
| `LearnSearchBar` | Unified results dropdown or results page |
| `DashboardContinueLearningWidget` | Links to next Stage |
| `DashboardLearnHealthWidget` | Admin counts |
| `SettingsPage` | Shell with links |
| Learn tab bar | All tabs enabled: Home, Technologies, Career Paths, Certifications, My Journey, Manage |

## Business rules

| ID | Rule |
|----|------|
| BR-UX01–02 | Final nav labels |
| UX principle | Dashboard Continue Learning — never wonder what's next |

## Test strategy

| Layer | Tests |
|-------|-------|
| Full regression | All backend + frontend suites |
| Search | Unified search returns mixed entity types |
| Cross-browser | Chrome, Edge, Firefox per engineering standards |
| Module regression | Initiatives, submissions, users, leaderboards, projects routes |

## Manual QA checklist (release)

| # | Scenario |
|---|----------|
| 1 | Unified search finds Technology, Career Path, Certification |
| 2 | Dashboard Continue Learning resumes correct Stage |
| 3 | Full employee journey: Technology → Roadmap → Progress → Certification READY |
| 4 | Full admin journey: create tech → roadmap → publish → career path → certification |
| 5 | Technology ↔ Project cross-nav (F16) |
| 6 | Optional Initiative association (F22) |
| 7 | Zero initiatives — full Learn works |
| 8 | Zero project links — full Learn works |
| 9 | v0.7.1 initiatives regression |
| 10 | v0.6.x certificate workflow regression |
| 11 | Sidebar final state matches IA doc |
| 12 | No "Project" label on Practice Resources |

## Risks

| Risk | Mitigation |
|------|------------|
| Release scope creep | Strict freeze on v0.8.0; defer extras to v0.8.1 |
| Launch content gap | Content checklist in PR; block release without min catalog |

## Dependencies

- **F16–F22** complete

## Acceptance criteria

- [ ] v0.8.0 release notes published
- [ ] All phase acceptance criteria met
- [ ] Manual QA sign-off
- [ ] Tag `v0.8.0` ready

## Estimated complexity

**M (Medium)** — integration + polish, no new domain complexity.

---

## Incremental Flyway plan

| Migration | Phase | Contents |
|-----------|-------|----------|
| `V12__learn_technologies.sql` | F16 | `learn_technologies`, `learn_technology_project_links` |
| `V13__learn_roadmaps_and_learning_resources.sql` | F17 | roadmaps, stages, learning_resources, junction |
| `V14__learn_progress.sql` | F18 | enrollments, stage_progress, optional resource_visits |
| `V15__learn_practice_resources.sql` | F19 | practice_resources, junction, optional progress |
| `V16__learn_career_paths.sql` | F20 | career_paths, career_path_technologies |
| `V17__learn_certifications.sql` | F21 | certifications, certification_technologies |
| `V18__initiative_certification_link.sql` | F22 | `learning_initiatives.linked_certification_id` |

No migration in F23.

---

## Launch content checklist (pre-release)

| Content | Minimum | Owner |
|---------|---------|-------|
| Technologies | 10 published | Technical leads |
| Roadmaps | 10 (≥ 3 stages each) | Technical leads |
| Learning Resources | 100+ links | Technical leads |
| Practice Resources | 30+ links | Technical leads |
| Career Paths | 3 published | L&D |
| Certifications | 10 published | L&D |
| Technology ↔ Project links | 10+ | Engineering leads |

---

## Future guidance — rule-based recommendations (not in v0.8.0)

Future "what to learn next" recommendations should initially be **rule-based**, not AI-generated.

```text
Completed Java → recommend Spring Boot → recommend Docker → recommend AWS
```

| Aspect | Guidance |
|--------|----------|
| Implementation | Curated adjacency rules in catalog metadata or recommendation table |
| AI | Deferred — `06-future-enhancements.md` |
| v0.8.0 | **Out of scope** — static **Next up** within enrolled Roadmap only |

---

## Explicitly out of scope (all phases)

| Item | Target |
|------|--------|
| AI Mentor, AI Summaries, AI Roadmap Generation, AI Chat | Future |
| Rule-based next-Technology recommendations | v0.8.1+ |
| Standalone cross-navigation phase | **Eliminated** — merged into F16 |
| Learn owning Projects | **Permanently excluded** |
| LMS features | **Permanently excluded** |
| Global app-wide search | v0.9+ |
| Learn leaderboards | v0.9+ |
| Automated link health checker | v0.8.1 |

---

## Approval gate

| Gate | Status |
|------|--------|
| Product design v1.1 | **Approved — frozen** |
| Implementation plan | **FINAL — frozen** |
| F16 coding | **May begin after formal sign-off** |

| Gate | Required before |
|------|-----------------|
| F16 start | Final plan sign-off |
| Each subsequent phase | Prior phase merged |

---

## Document approval

| Role | Status |
|------|--------|
| Product Owner | Approved (design v1.1) |
| Implementation plan | **FINAL — frozen** |
| Engineering Lead | Pending sign-off |
| UX Lead | Pending sign-off |
