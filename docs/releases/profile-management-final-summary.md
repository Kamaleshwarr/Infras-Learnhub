# Profile Management Workstream — Final Summary

**Workstream status:** Complete (backend + self-service UI)  
**Final release:** v0.5 (2026-06-16)  
**Scope:** Authenticated user profile view, edit, change-password entry, and avatar management

Per-release detail: `docs/releases/release-v0.5.md`

---

## Release overview

| Phase | Feature | Commits | Validation |
|-------|---------|---------|------------|
| **Phase 1** | Profile View | `983d82f` | Passed |
| **Phase 2** | Edit Profile | `83a5976`, `daf91cd` (fix) | Passed (after PM-D01) |
| **Phase 3** | Change Password Entry | `874caa7`, `a173d44` (fix) | Passed (after PM-D02) |
| **Phase 4** | Avatar Upload / Replace / Delete | `f2d418d` | Passed |

**Merged via PR #27** (`cursor/profile-management-v0.5-acee` → `main`)

---

## Phase 1 — Profile View

### Features delivered

**Backend**

- New `profile` module under `com.company.learninghub.profile`
- `GET /api/v1/profile` — returns `ProfileResponse` for authenticated user
- `ProfileService`, `ProfileMapper`, `ProfileController`
- Flyway `V8__profile_avatar.sql` — avatar metadata columns on `users` (nullable)
- Extended `User` entity with avatar fields

**Frontend**

- `ProfilePage` at `/profile`
- `ProfileViewSection` — read-only display of profile fields
- `ProfileAvatar` — initials fallback when no avatar
- `profileApi.get()`, `types/profile.ts`
- Sidebar **My Profile** navigation entry

**Tests:** `ProfileServiceTest`, `ProfileControllerTest`, `ProfileMethodSecurityTest`, `ProfileNavigation.test.tsx`, `ProfilePage.test.tsx`

### Defects

None logged for Phase 1.

---

## Phase 2 — Edit Profile

### Features delivered

**Backend**

- `PUT /api/v1/profile` with `UpdateProfileRequest`, `ProfileUpdateResponse`
- Email change issues new JWT via `AuthenticationService.issueAccessToken()`
- Duplicate email check with lowercase normalization

**Frontend**

- `ProfileEditForm` — full name and email editing
- `profileFormState.ts` — dirty-form guard (`isEditFormDirty`)
- `AuthProvider.refreshProfile()` and token storage update on email change
- Reuses `UserManagementSnackbar` for feedback

### Defects discovered

| ID | Symptom | Root cause |
|----|---------|------------|
| PM-D01 | Application failed to load; `ProfileEditForm` import error | Case-insensitive filesystem collision between `profileEditForm.ts` and `ProfileEditForm.tsx` — Vite resolved to the `.ts` utility file instead of the React component |

### Defects fixed

| ID | Fix |
|----|-----|
| PM-D01 | Renamed `profileEditForm.ts` → `profileFormState.ts` (and corresponding test file) |

---

## Phase 3 — Change Password Entry

### Features delivered

**Frontend only** — no backend API changes (reuses existing `POST /api/v1/auth/change-password`)

- **Change Password** button on profile view → `navigate('/change-password')`
- Button hidden when `profile.mustChangePassword === true`
- No changes to `ChangePasswordPage` itself

### Defects discovered

| ID | Symptom | Root cause |
|----|---------|------------|
| PM-D02 | Clicking Change Password redirected to `/` instead of `/change-password` | `MustChangePasswordRoute` redirected users *without* `mustChangePassword` away from `/change-password` |

### Defects fixed

| ID | Fix |
|----|-----|
| PM-D02 | Removed redirect for voluntary `/change-password` access; added `MustChangePasswordRoute.test.tsx` |

---

## Phase 4 — Avatar Upload / Replace / Delete

### Features delivered

**Backend**

- `POST /api/v1/profile/avatar` — upload/replace (multipart `file`)
- `DELETE /api/v1/profile/avatar` — idempotent `204` even when no avatar
- `GET /api/v1/profile/avatar` — serve image bytes with `Cache-Control` and `ETag`
- `AvatarStorageService` — local storage at `avatars/{userId}/{uuid}.{ext}`
- `ProfileProperties` — `app.profile.avatar-max-size-bytes` (default 2 MB)
- Validation: JPG, JPEG, PNG, WebP; max 2 MB
- `UserSummaryResponse.avatarUrl` on login and `/auth/me`

**Frontend**

- `ProfileAvatar` — authenticated blob fetch for image display
- `ProfileAvatarUpload` — Upload / Replace / Delete with confirm dialog
- `profileApi` — `uploadAvatar`, `deleteAvatar`, `fetchAvatarBlob`
- `refreshProfile()` after avatar mutations

### Defects

None logged for Phase 4 — passed validation on first round.

---

## APIs added (complete Profile module)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/profile` | Current user profile |
| `PUT` | `/api/v1/profile` | Update fullName + email |
| `POST` | `/api/v1/profile/avatar` | Upload or replace avatar |
| `DELETE` | `/api/v1/profile/avatar` | Delete avatar (idempotent) |
| `GET` | `/api/v1/profile/avatar` | Avatar image bytes |

**Auth summary extension:** `UserSummaryResponse.avatarUrl` (relative URL to `GET /profile/avatar`)

---

## Database changes

**Migration:** `V8__profile_avatar.sql`

```sql
ALTER TABLE users
    ADD COLUMN avatar_storage_provider VARCHAR(20),
    ADD COLUMN avatar_storage_key VARCHAR(500),
    ADD COLUMN avatar_content_type VARCHAR(100),
    ADD COLUMN avatar_original_filename VARCHAR(255),
    ADD COLUMN avatar_file_size_bytes BIGINT,
    ADD COLUMN avatar_updated_at TIMESTAMPTZ;

CREATE INDEX idx_users_avatar_updated_at ON users (avatar_updated_at)
    WHERE avatar_storage_key IS NOT NULL;
```

No new tables. Avatar metadata lives on the existing `users` row.

---

## Defect summary

| ID | Phase | Severity | Area | Status |
|----|-------|----------|------|--------|
| PM-D01 | 2 | Functional | Frontend import collision | Fixed |
| PM-D02 | 3 | Functional | Route guard redirect | Fixed |

**Total logged defects:** 2 — all resolved.  
**Validation:** All four phases passed manual validation (Phases 2 and 3 after fix rounds).

---

## Final Profile Management capabilities

### Who can use it

- **All authenticated users** (`ADMIN` and `EMPLOYEE`) — self-service only; no admin profile APIs

### Self-service UI (`/profile`)

| Capability | Description |
|------------|-------------|
| **View profile** | Employee ID, full name, email, role, status, must-change-password flag |
| **Edit profile** | Update full name and email; JWT refresh on email change |
| **Change password entry** | Navigate to `/change-password` (hidden when must-change is active) |
| **Avatar display** | Circular image with initials fallback |
| **Avatar upload** | JPG, JPEG, PNG, WebP up to 2 MB |
| **Avatar replace** | Overwrites existing avatar; old file removed from storage |
| **Avatar delete** | Removes avatar; reverts to initials fallback |

### Security

| Rule | Enforcement |
|------|-------------|
| Self-only access | Service methods use authenticated user ID from JWT principal |
| Email uniqueness | Case-insensitive duplicate rejection |
| Avatar file types | Content-type and extension allowlist |
| Avatar size | Configurable max bytes (default 2 MB) |
| Avatar fetch | Authenticated `GET /profile/avatar` only |
| Email change | New access token issued; client updates session storage |

---

## Test coverage summary

| Area | Tests |
|------|-------|
| `ProfileServiceTest` | Get/update profile, email duplicate, avatar upload/replace/delete, validation |
| `ProfileControllerTest` | API contracts, multipart upload, avatar GET/DELETE |
| `ProfileMethodSecurityTest` | `@PreAuthorize` on profile service methods |
| `ProfilePage.test.tsx` | Page render, view/edit modes |
| `ProfileEditForm` / `profileFormState` | Dirty guard, form validation |
| `ProfileAvatar.test.tsx` | Initials fallback, blob display |
| `ProfileAvatarUpload.test.tsx` | Upload/replace/delete flows |
| `ProfileNavigation.test.tsx` | Sidebar link, route access |
| `MustChangePasswordRoute.test.tsx` | Voluntary change-password navigation |
| `profileApi.test.ts` | API client contracts |
| `profileInitials.test.ts` | Initials derivation |

**Baselines after v0.5:** 132 frontend tests (31 files); 184 backend tests (10 Docker integration skipped).

---

## Validation & test artifacts

| Artifact | Location |
|----------|----------|
| Defect log & regression checklist | `docs/testing-and-defect-history.md` |
| Backend architecture & API reference | `docs/backend-architecture-and-roadmap.md` |
| Release notes | `docs/releases/release-v0.5.md` |
| Project roadmap | `docs/project-roadmap.md` |

---

## Workstream conclusion

The Profile Management workstream delivered a complete **self-service** profile experience across four phases:

1. **Phase 1** — read-only profile view and navigation  
2. **Phase 2** — edit full name and email with JWT refresh on email change  
3. **Phase 3** — change-password entry point from the profile page  
4. **Phase 4** — avatar upload, replace, and delete with local file storage

Two defects were discovered during UI validation (import collision, route guard redirect). Both were fixed before the workstream closed.

**Next planned workstream:** Awaiting approval — recommended candidates: Notifications or Global Search (see `docs/project-roadmap.md`).
