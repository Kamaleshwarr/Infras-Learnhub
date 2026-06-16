# Engineering Learning Hub v0.4

**Theme:** User Management UI — Phase 4 (Bulk Import)  
**Merged:** 2026-06-12  
**PRs:** #22 (Phase 4), #23 (blank row fix), #24 (header-only template)

---

## Completed Features

### User Management UI — Phase 4 (PR #22)

- **Bulk Import UI** — `BulkImportDialog` on `/users` with select → preview → confirm → results flow
- **Import Preview** — filename and estimated data-row count before upload; preview logic aligned with backend parser
- **Download Template** — toolbar action fetches `GET /api/v1/users/import/template` (`user-import-template.csv`)
- **Create-only import behavior** — import creates new users only; no update-via-import; default password `Temp@12345` with `mustChangePassword = true`
- **Upload lockdown** — Import, Close, and file picker disabled while preview or import is in progress
- **Import result summary** — total / imported / failed counts with per-row error list; list refresh on success via parent callback

### Import parser & template fixes (PRs #23, #24)

- **Blank row parser improvements** — trailing comma-only CSV lines and empty Excel rows skipped via `isBlankImportRow()`; cell normalization trims NBSP and zero-width spaces
- **Preview/import alignment** — `userImportPreview.ts` mirrors backend skip rules (header, blank rows, `#` comment lines)
- **Header-only template** — CSV template is a single header row (`Employee ID,Full Name,Email,Role`); no comment rows that Excel could render as data
- **Import role guidance** — dialog helper text: `Valid role values: ADMIN, EMPLOYEE`; invalid values (e.g. `Manager`, `Administrator`) rejected at import with row-level errors

---

## Backend Changes (v0.4)

| Change | PR |
|--------|-----|
| `normalizeImportCell()` — trim NBSP / zero-width space | #23 |
| `isBlankImportRow()` — skip all-blank rows in CSV and Excel parsers | #23 |
| `isImportCommentLine()` / `isImportCommentRow()` — skip `#` comment lines | #23 |
| Header-only `TEMPLATE` constant (no embedded comment row) | #24 |

**Location:** `UserManagementService` — `parseCsvRows()`, `parseWorkbookRows()`, `generateTemplate()`

---

## APIs Used by Phase 4 UI

| Action | Method | Endpoint |
|--------|--------|----------|
| Import users | `POST` | `/api/v1/users/import` (multipart `file`) |
| Download template | `GET` | `/api/v1/users/import/template` |

**Accepted formats:** CSV, XLS, XLSX  
**Columns:** `Employee ID`, `Full Name`, `Email`, `Role` (`ADMIN` \| `EMPLOYEE`)

**Response:** `UserImportResponse { totalRows, imported, failed, errors[] }`

---

## Frontend Additions

| Area | Files |
|------|-------|
| Dialog | `BulkImportDialog.tsx` |
| Toolbar | `UserListToolbar.tsx` — Download Template, Import Users |
| API | `usersApi.importUsers`, `usersApi.downloadImportTemplate` |
| Utils | `downloadBlob.ts`, `userImportPreview.ts` |
| Messages | `userManagementMessages.ts` — import helper copy |

---

## Test Summary

- Frontend: **98 tests** (23 files), production build green
- Backend: `UserManagementServiceTest` — blank rows, invalid roles, Excel trailing row, header-only template
- Manual validation: **Passed** (Phase 4 final validation after PRs #23–#24)

See `docs/testing-and-defect-history.md` for defect log and regression checklist.

---

## Backlog (post v0.4)

| ID | Item |
|----|------|
| UM-002 | User Details Drawer |
| UM-003 | Unified cross-field search |
| UM-004 | View User Details |
| UM-006 | Downloadable import error report |
| — | Notifications |
| — | Global Search |

Roadmap: `docs/project-roadmap.md`
