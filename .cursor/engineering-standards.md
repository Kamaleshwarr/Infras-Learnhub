# Engineering Learning Hub — Engineering Standards

**Status:** Mandatory for all implementation work unless explicitly overridden by the product owner or release plan.

**Companion documents:**

- `.cursor/project-context.md` — project identity, stack, release state
- `.cursor/coding-standards.md` — language and module-level coding rules
- `.cursor/architecture.md` — system architecture reference

Read this document **before starting any feature phase**.

---

## 1. Engineering principles

1. **Correctness before speed** — ship working, testable increments; do not skip validation or regression checks to move faster.
2. **Minimal scope** — implement only what the approved phase requires; avoid unrelated refactors or speculative features.
3. **Consistency over novelty** — match existing patterns in the codebase before introducing new abstractions.
4. **Explicit over implicit** — document assumptions; do not invent business rules when requirements are ambiguous.
5. **Fail safely** — prefer clear errors, guarded transitions, and backward-compatible behaviour over silent breakage.
6. **Test what matters** — automated tests must cover real behaviour, not trivial assertions.
7. **Leave the codebase better** — each phase should improve clarity without increasing duplication.

---

## 2. Implementation standards

### 2.1 Reuse before create

Before adding a new component, helper, API wrapper, or dialog:

1. Search for an existing equivalent in the codebase.
2. Extend or generalize the existing implementation when the change is small.
3. Create a new artifact only when reuse would harm clarity or coupling.

**Preferred reuse references:**

| Area | Reference patterns |
|------|-------------------|
| Admin list pages | `UserListPage`, `InitiativeListPage` |
| Create/Edit dialogs | `CreateUserDialog`, `EditUserDialog`, `CreateInitiativeDialog` |
| Confirmation flows | `ConfirmActionDialog` |
| Snackbars / notifications | `UserManagementSnackbar`, `InitiativeManagementSnackbar` |
| API clients | `usersApi`, `initiativesApi` |
| Form state / dirty guards | `editUserForm.ts`, `initiativeFormState.ts` |
| Error handling | `resolveApiError`, `getValidationErrors` |
| Tables / pagination | `UserTable`, `SortableTableHead`, `TablePaginationBar` |

### 2.2 Architecture and UI consistency

- Follow established **page orchestration**: `PageHeader` → toolbar → filters → content → pagination → dialogs → snackbar.
- Use **Material UI** components and existing theme conventions.
- Keep **role-aware UI** in-page (`useAuth().isAdmin`) unless routing requirements dictate otherwise.
- Use **URL query sync** for list filters, sort, and pagination where list pages already do so.
- Match **message copy** patterns in module-specific `*Messages.ts` files.
- Use **feature numbering** (F0, F1, F2, …) in PR titles, implementation reports, and release tracking — not ad-hoc phase letters unless mapped to feature IDs.

### 2.3 Avoid duplicate implementations

Do **not**:

- Create parallel form validation logic when shared helpers exist.
- Duplicate API error parsing.
- Introduce a second snackbar pattern when the module snackbar can be reused.
- Copy-paste dialog structure without extracting shared fields (e.g. `InitiativeFormFields`).
- Add backend endpoints when existing APIs already satisfy the requirement.

When generalizing a shared component, preserve backward compatibility for existing callers.

### 2.4 Frontend / backend separation

| Layer | Responsibility |
|-------|----------------|
| **Backend** | Business rules, persistence, authorization, validation of invariants, API contracts |
| **Frontend** | Presentation, client-side validation for fast feedback, routing, role-aware UX |

Rules:

- Do not move business rules to the frontend when the backend is the source of truth.
- Do not change API contracts without explicit approval and coordinated backend/frontend updates.
- Frontend client validation must mirror backend constraints but **does not replace** server validation.
- Use typed API modules (`frontend/src/api/`) — no ad-hoc `fetch` in components.

### 2.5 Backward compatibility

Unless a release explicitly approves breaking changes:

- Preserve existing routes and deep links.
- Preserve existing API response shapes and query parameter behaviour.
- Keep employee and admin read paths working when adding admin write features.
- Avoid renaming user-facing labels without product approval.
- When backend behaviour is stricter than before, surface clear UI messaging rather than failing silently.

### 2.6 Backend implementation (summary)

For full detail see `.cursor/coding-standards.md`. Minimum expectations:

- DTOs for all public APIs; no entity exposure.
- Service-layer business rules and `@PreAuthorize` where required.
- Flyway for schema changes only; never edit merged migrations.
- Swagger/OpenAPI on all new endpoints.
- Unit, controller, and security tests for new modules.

### 2.7 Frontend implementation (summary)

- TypeScript strict typing; no `any` without justification.
- Colocate tests with components (`*.test.tsx`) or modules (`*.test.ts`).
- Use Vitest + Testing Library; prefer user-visible assertions.
- Run `npm test` and `npm run build` before marking a phase complete.

---

## 3. Mandatory self-QA

Self-QA is required **before** declaring any feature phase complete. Do not rely on manual QA to discover issues that self-QA should have caught.

### 3.1 Functional testing

Verify each applicable category:

| Category | Examples |
|----------|----------|
| **Positive scenarios** | Happy path end-to-end |
| **Negative scenarios** | API failures, permission denied, not found |
| **Boundary testing** | Min/max lengths, page boundaries, date edges |
| **Edge cases** | Empty lists, single item, last page, concurrent state |
| **Invalid inputs** | Malformed dates, wrong file types, bad IDs |
| **Empty values** | Blank required fields, optional fields omitted |
| **Duplicate data** | Unique constraint violations, duplicate submissions |
| **Long text** | Max-length fields, truncation/display |
| **Date validation** | UTC boundaries, start/expiry ordering, visibility windows |
| **Permission/role validation** | Admin-only actions hidden/blocked for employees |
| **State transitions** | Status changes, publish/draft/expired, activate/deactivate |

### 3.2 UX review

| Area | Check |
|------|-------|
| Validation messages | Clear, field-level where appropriate, consistent tone |
| Button behaviour | Disabled when invalid/submitting; correct labels |
| Loading states | Spinners/skeletons; no double-submit |
| Empty states | Meaningful copy and recovery actions |
| Error handling | Recoverable errors show retry; forms stay open on failure |
| Success messages | Snackbar or inline confirmation after mutations |
| Navigation | Back links, redirects after delete, URL state preserved |
| Responsive behaviour | Desktop table + mobile card patterns where applicable |
| Accessibility basics | Labels, dialog titles, keyboard-focusable controls, `aria` on dialogs |

### 3.3 Regression review

- Re-run automated test suites.
- Verify adjacent features still work (e.g. employee browse flows when adding admin management).
- Confirm no unintended changes to API contracts, routes, or role guards.

### 3.4 Product review (four perspectives)

Review every feature from:

1. **Senior Software Engineer** — Is the design maintainable, testable, and consistent with architecture?
2. **Senior QA Engineer** — What breaks under failure, misuse, or edge conditions?
3. **Product Owner** — Does this meet the approved scope and business intent?
4. **UX Reviewer** — Is the workflow intuitive, discoverable, and consistent with the rest of the app?

**Challenge the implementation** — do not only implement the literal requirement. Identify usability issues, workflow gaps, and improvement opportunities **before** manual QA.

### 3.5 Browser compatibility

Where feasible, validate on **Chrome**, **Edge**, and **Firefox**.

If full cross-browser testing is not performed, document:

- Which browsers were tested
- Known limitations or untested areas
- Any browser-specific dependencies (e.g. date input behaviour)

---

## 4. Phase completion criteria

A feature phase (e.g. F12) is **not complete** until all items below are satisfied:

| # | Criterion | Required |
|---|-----------|----------|
| 1 | Implementation complete per approved scope | Yes |
| 2 | Unit/integration tests added or updated | Yes |
| 3 | Existing tests passing | Yes |
| 4 | Production build passing (`npm run build` / `mvn test`) | Yes |
| 5 | Self-QA completed (Section 3) | Yes |
| 6 | Risks/issues documented | Yes |
| 7 | Manual QA checklist prepared | Yes |
| 8 | Documentation updated (when applicable) | When scope includes docs |
| 9 | PR created with clear feature ID | Yes |
| 10 | Merge readiness reviewed | Yes |

**Standard implementation report deliverables:**

1. Files changed
2. Tests added/updated
3. Test results
4. Build results
5. Risks/issues discovered
6. Acceptance checklist
7. Manual QA checklist

---

## 5. Working rules

### 5.1 Requirements and assumptions

- **Never assume business behaviour** when requirements are ambiguous.
- **Document assumptions** in the PR description or implementation report.
- **Ask for clarification** only when assumptions would materially affect implementation, data model, or user workflow.
- Prefer aligning with existing backend behaviour over inventing new rules.

### 5.2 Improvements during implementation

| Finding | Action |
|---------|--------|
| **Low-risk UX improvement** (label clarity, missing empty state, obvious validation gap) | Implement in the current phase |
| **Larger product improvement** (new workflow, API change, scope expansion) | Document with impact analysis and recommendation; do not implement without approval |

### 5.3 Git and PR discipline

- Use branch prefix `cursor/` and suffix `-7a10` per cloud agent conventions.
- One feature phase per PR when practical.
- Commit with clear messages referencing feature ID (e.g. `feat(v0.7.1): F12 create initiative dialog`).
- Push before creating/updating PR.
- Do not mark a phase complete with failing tests or build.

### 5.4 Feature numbering

Use consistent feature IDs across the project:

- **F0–F10** — v0.7.0 Initiatives Experience (shipped)
- **F11** — Initiative Management Foundation
- **F12** — Create Initiative
- **F13** — Edit Initiative
- **F14** — Lifecycle Management
- **F15** — Delete Initiative

New features receive the next available F-number in the release plan. Avoid unnumbered "Phase A/B/C" in PRs, release notes, and reports.

### 5.5 What not to do

- Do not skip tests for "simple" changes.
- Do not merge backend and frontend contract changes without coordination.
- Do not add Flyway migrations for frontend-only releases.
- Do not remove or weaken existing authorization checks.
- Do not introduce duplicate components when extension is sufficient.

---

## 6. Quick reference checklist

Copy into PR description or implementation report as needed:

```markdown
## Self-QA
- [ ] Positive / negative / boundary scenarios
- [ ] Role and permission checks
- [ ] Validation, empty, and error states
- [ ] Success messaging and navigation
- [ ] Regression of adjacent features
- [ ] Product review (engineer, QA, PO, UX)
- [ ] Browser check or limitations documented

## Completion
- [ ] Tests added/updated
- [ ] All tests pass
- [ ] Build passes
- [ ] Risks documented
- [ ] Manual QA checklist prepared
- [ ] Docs updated (if applicable)
- [ ] PR ready for review
```

---

## 7. Document maintenance

- Update this document when engineering policy changes.
- Significant process changes should be noted in release documentation.
- `.cursor/coding-standards.md` remains the authoritative source for code-style and module-structure detail; this document governs **how we work**, not every syntax rule.
