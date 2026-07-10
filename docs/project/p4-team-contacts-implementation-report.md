# P4 — Project Team & Contacts — Implementation Report

**Date:** 2026-07-09  
**Branch:** `cursor/project-module-p4-team-contacts-63ba`  
**Status:** Implementation complete — awaiting manual QA

---

## 1. Impact analysis

| Area | Finding | Reuse decision |
|------|---------|----------------|
| `project_members` | Existing membership table with `project_role` | **Extended** with team fields |
| `ProjectRole` enum | OWNER / CONTRIBUTOR / VIEWER | **Unchanged** — access control only |
| `User` entity | fullName, email, employeeId | **Reused** — no duplicate personal data |
| Member APIs | `/projects/{id}/members` CRUD | **Extended** request/response DTOs |
| Project Overview Team card | Placeholder "Coming Soon" | **Activated** with summary + link |
| `ManageProjectMembersDialog` | Admin-only user search, access role only | **Replaced** by Team page + `TeamMemberDialog` |
| User directory | Admin `GET /users` only | **New** `member-candidates` for project owners |
| External contacts | No prior model | **New** `project_external_contacts` table |

## 2. Architecture decision

**Extend `project_members`** — no duplicate `project_team_members` table.

Added columns:
- `functional_role` (required, default `OTHER` for backward compatibility)
- `responsibility` (optional text)
- `is_primary_contact` (boolean)
- `display_order` (int)

**External contacts:** separate `project_external_contacts` entity for non-platform users (client PO, vendor, infra mailbox, etc.).

## 3. Access role vs functional role

| Type | Enum | Purpose |
|------|------|---------|
| Access role | `ProjectRole` | Authorization — OWNER / CONTRIBUTOR / VIEWER |
| Functional role | `ProjectFunctionalRole` | Job responsibility on the project |

These are stored and updated independently. Changing one does not reset the other.

## 4. API endpoint matrix

| Method | Path | Access |
|--------|------|--------|
| GET | `/api/v1/projects/{id}/members` | Project reader |
| POST | `/api/v1/projects/{id}/members` | OWNER / ADMIN |
| DELETE | `/api/v1/projects/{id}/members/{userId}` | OWNER / ADMIN |
| GET | `/api/v1/projects/{id}/member-candidates?search=` | OWNER / ADMIN |
| GET | `/api/v1/projects/{id}/contacts` | Project reader |
| POST | `/api/v1/projects/{id}/contacts` | OWNER / ADMIN |
| PUT | `/api/v1/projects/{id}/contacts/{contactId}` | OWNER / ADMIN |
| DELETE | `/api/v1/projects/{id}/contacts/{contactId}` | OWNER / ADMIN |

`ProjectResponse` extended with `primaryContactCount`.

## 5. Business rules

- One membership row per user per project (upsert on POST)
- Functional role required on new/updated members
- Multiple primary contacts allowed
- Cannot remove last OWNER
- MEMBERS_ONLY projects hidden from non-members (404)
- Archived project behavior unchanged from P1
- Team page respects same read authorization as other project routes

## 6. Frontend UX

- Route: `/projects/:projectId/team`
- Overview: compact Team & Contacts summary with link
- Team page: summary chips, primary contacts, grouped project team, external contacts
- Management: OWNER/ADMIN only via `TeamMemberDialog` and `ExternalContactDialog`
- CONTRIBUTOR / VIEWER: read-only directory

## 7. Database

**Flyway V19** — `V19__project_team_and_contacts.sql`

## 8. Tests

- Backend: `ProjectTeamServiceTest`, updated `ProjectKnowledgeServiceTest`
- Frontend: `ProjectTeamPage.test.tsx`, updated `ProjectDetailPage.test.tsx`
- E2E: `scripts/p4-team-e2e-smoke.sh`

## 9. Explicit scope boundaries

- **SKIPPED:** Cross-Project Search & Discovery
- **NOT STARTED:** P5 governance, Leaderboards, Email, AI Assistant, Initiative ↔ Project, Learn v2 expansion

## 10. Screenshots

Capture during manual QA under `docs/screenshots/p4-team-contacts/` at 1280px, 834px, and 390px viewports.
