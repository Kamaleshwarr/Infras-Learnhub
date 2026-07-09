# Project Module — UX Information Architecture

**Date:** 2026-07-06

Design goal: **engineering project portal**, not a Windows File Explorer clone.

---

## 1. Top-level navigation

```text
Sidebar: Projects (/projects)
  → Project List
  → Project Detail (/projects/:projectId)
       ├── Overview (default tab)
       ├── Knowledge Base
       ├── Environments
       ├── Repositories
       ├── Team
       └── Settings (OWNER/ADMIN only)
```

Use **horizontal tabs** or **left sub-nav** on project detail — not deep route nesting for MVP.

---

## 2. Screen specifications

### 2.1 Projects List (`/projects`)

**Purpose:** Discover and enter projects.

| Element | Behavior |
|---------|----------|
| Search bar | Name, description (debounced, URL sync) |
| Filters | Access type, status, technology (P4), my projects |
| Sort | Name, recently updated |
| Project cards/rows | Name, status chip, description excerpt, tech stack chips (top 3), access badge |
| Empty state | "No projects yet" + admin create CTA |
| Loading/error | Standard page patterns |

**Avoid:** Raw table with only ID columns.

### 2.2 Project Overview (`/projects/:projectId` or `?tab=overview`)

**Purpose:** High-signal landing — answer "what is this?" in 30 seconds.

| Section | Content |
|---------|---------|
| Hero | Name, status chip, archived banner, description |
| Owners row | Project owner, technical owner, QA owner (avatars + names) |
| Quick Links | 4–8 pinned buttons opening external URLs |
| Technology Stack | Chips linking to `/learn/technologies/:id` |
| Environment summary | Table: env name → primary app URL (link out) |
| Repository summary | Primary repo + link to Repositories tab |
| Recent Updates | Last 5 changed resources with relative time |
| Knowledge Base CTA | "Browse knowledge base →" |

**Audience shortcuts:**

- New hire: description, owners, quick links, onboarding docs in KB
- Cross-team: tech stack, env summary, architecture links

### 2.3 Knowledge Base (`/projects/:projectId/knowledge`)

**Purpose:** Organized deeper documentation — folder browser.

| Element | Behavior |
|---------|----------|
| Breadcrumb | Project > Knowledge Base > [Folder] > [Subfolder] |
| Folder grid | Icon + name + item count; max 2 levels visible |
| Template hint | First-time: "Apply folder template" (Requirements, Tech Docs, QA, Deployment) |
| Item list | Title, type icon, type label, last updated, stale badge |
| Actions (CONTRIBUTOR+) | Add link, add folder, edit |
| Search within KB | Filter current folder or project-wide |

**Avoid:** Tree view with 6+ expand levels; drag-drop reorder in MVP.

### 2.4 Folder View

Same route as Knowledge Base with `?folderId=` — shows subfolders + items.

| Item row | Icon by type, title, description excerpt, external link icon |
| Click | Opens URL in new tab (links) or detail drawer |
| CONTRIBUTOR+ | Edit, move folder, delete (owner) |

### 2.5 Resource Detail / Edit

**MVP:** Slide-over drawer or modal — not full page.

| Field | Link resource |
|-------|---------------|
| Title | required |
| Type | dropdown |
| URL | required, https |
| Description | optional |
| Folder | picker |
| Owner | user picker |
| Last reviewed | date (P5) |

Save → snackbar → refresh list.

### 2.6 Environments (`/projects/:projectId/environments`)

| Element | Behavior |
|---------|----------|
| Env cards | One card per environment (QA, UAT, PROD…) |
| References grouped | App URL, API, Swagger, Monitoring, etc. |
| Production flag | Visual warning on PROD |
| Add env (CONTRIBUTOR+) | Name, optional code, is_production |
| Add reference | Type, title, URL |

**Primary use case:** "Where is the QA Swagger URL?" — answered in <10 seconds.

### 2.7 Repositories (`/projects/:projectId/repositories`)

| Element | Behavior |
|---------|----------|
| Repo list | Name, provider icon, primary badge, URL |
| Actions | Open in GitHub (external), copy URL |
| CONTRIBUTOR+ | Add, edit, delete (owner) |

### 2.8 Team & Contacts (`/projects/:projectId/team`)

| Element | Behavior |
|---------|----------|
| Member table | Name, email, employee ID, project role |
| Role chips | OWNER, CONTRIBUTOR, VIEWER |
| OWNER actions | Add member (user search), change role, remove |

Future: non-user contacts (vendor email) — optional extension.

### 2.9 Admin / Maintainer Settings (`/projects/:projectId/settings`)

**Visible to OWNER and ADMIN only.**

| Section | Actions |
|---------|---------|
| Metadata | Edit name, description, status, access type, owners |
| Danger zone | Archive project |
| Credential references (P5) | Manage vault pointers |
| Technology links | Link to admin Learn curation or inline read-only with "Manage in Learn" |

---

## 3. Visual design principles

| Principle | Implementation |
|-----------|----------------|
| Portal not explorer | Fixed sections + shallow folders |
| External-first | Link icon, "Opens externally" hint |
| Scannable overview | Cards and chips, not dense tables |
| Role-aware | Hide edit controls for VIEWER |
| Consistent with Learn | Reuse `RelatedTechnologiesCard` patterns |
| Consistent with Initiatives | Status chips, confirm dialogs for destructive actions |

---

## 4. Responsive behavior

| Breakpoint | Adaptation |
|------------|------------|
| Desktop | Side sub-nav + content |
| Tablet | Tabs + stacked sections |
| Mobile | Accordion sections on overview; simplified list |

---

## 5. Dashboard integration (extend)

| Widget | Enhancement |
|--------|-------------|
| Assigned Projects | Click row → `/projects/:id` |
| Recent Project Updates | Click row → project overview |
| Optional | Show project status chip |

---

## 6. Learn cross-navigation UX

| From | To | Component |
|------|-----|-----------|
| Technology detail | Projects using this tech | `RelatedOrganizationProjectsCard` (exists) |
| Project overview | Technology stack | Extend `RelatedTechnologiesCard` |
| Project overview | "Add technology" | Admin only → Learn manage |

---

## 7. Empty and error states

| State | Message tone |
|-------|--------------|
| No projects | Informative + admin path |
| No quick links | "Pin important links from Knowledge Base" |
| No environments | "Add environments to help the team find URLs" |
| 404 project | "Project not found or you don't have access" |
| Archived | Banner on overview for admin |

---

## 8. Anti-patterns to avoid

- Unlimited nested folder tree in sidebar
- Inline iframe for Confluence/GitHub
- Secret input fields
- Duplicating Learn roadmap on project page
- Making Knowledge Base the default tab (Overview first)
