# Engineering Learning Hub v0.5

**Theme:** Profile Management (Phases 1–4)  
**Merged:** 2026-06-16  
**PR:** #27 (`cursor/profile-management-v0.5-acee`)

---

## Completed Features

### Phase 1 — Profile View (PR #27)

- **`GET /api/v1/profile`** — self-service profile for the authenticated user
- **`ProfilePage`** at `/profile` with read-only profile section
- **`ProfileAvatar`** — circular avatar with initials fallback when no image is set
- **Sidebar navigation** — **My Profile** link for all authenticated users
- **Flyway `V8__profile_avatar.sql`** — nullable avatar metadata columns on `users`

### Phase 2 — Edit Profile (PR #27)

- **`PUT /api/v1/profile`** — update `fullName` and `email` for the current user
- **Email change JWT refresh** — returns optional `accessToken` when email changes
- **Duplicate email guard** — case-insensitive uniqueness check
- **`ProfileEditForm`** — validation, dirty-form guard, snackbar feedback
- **`AuthProvider.refreshProfile()`** — session refresh after profile or avatar changes

### Phase 3 — Change Password Entry (PR #27)

- **Change Password** button on profile view navigates to `/change-password`
- **Hidden when `mustChangePassword === true`** — user is already on the forced-change flow
- **`MustChangePasswordRoute` fix** — voluntary navigation to `/change-password` no longer redirects to `/`

### Phase 4 — Avatar Upload / Replace / Delete (PR #27)

- **`POST /api/v1/profile/avatar`** — upload or replace avatar (multipart `file`)
- **`DELETE /api/v1/profile/avatar`** — idempotent `204` even when no avatar exists
- **`GET /api/v1/profile/avatar`** — serve avatar image bytes with cache headers
- **`AvatarStorageService`** — local filesystem storage under `avatars/{userId}/{uuid}.{ext}`
- **`ProfileAvatarUpload`** — Upload / Replace / Delete with confirm dialog
- **`UserSummaryResponse.avatarUrl`** — exposed on login and `GET /api/v1/auth/me`

---

## Backend Changes (v0.5)

| Change | Location |
|--------|----------|
| Profile module (`controller`, `service`, `dto`, `mapper`, `config`) | `com.company.learninghub.profile` |
| Avatar storage service | `com.company.learninghub.storage.AvatarStorageService` |
| Avatar metadata on `User` entity | `user.domain.User` |
| `issueAccessToken()` for email-change refresh | `AuthenticationService` |
| `resolveAvatarUrl()` for auth summary | `AuthenticationService` |
| Max avatar size config (`2 MB`) | `app.profile.avatar-max-size-bytes` in `application.yml` |

**Flyway:** `V8__profile_avatar.sql`

| Column | Type | Purpose |
|--------|------|---------|
| `avatar_storage_provider` | `VARCHAR(20)` | Storage backend identifier |
| `avatar_storage_key` | `VARCHAR(500)` | Object key / path |
| `avatar_content_type` | `VARCHAR(100)` | MIME type |
| `avatar_original_filename` | `VARCHAR(255)` | Original upload filename |
| `avatar_file_size_bytes` | `BIGINT` | Stored file size |
| `avatar_updated_at` | `TIMESTAMPTZ` | Last avatar mutation |

**Index:** `idx_users_avatar_updated_at` (partial, where `avatar_storage_key IS NOT NULL`)

---

## APIs Added (Profile Module)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/profile` | Bearer JWT | Current user profile |
| `PUT` | `/api/v1/profile` | Bearer JWT | Update `fullName` + `email`; optional `accessToken` on email change |
| `POST` | `/api/v1/profile/avatar` | Bearer JWT | Upload or replace avatar (multipart `file`) |
| `DELETE` | `/api/v1/profile/avatar` | Bearer JWT | Delete avatar (idempotent `204`) |
| `GET` | `/api/v1/profile/avatar` | Bearer JWT | Avatar image bytes |

**`ProfileResponse` fields:** `id`, `employeeId`, `fullName`, `email`, `role`, `active`, `mustChangePassword`, `avatarUrl`, `createdAtUtc`, `updatedAtUtc`

**Avatar validation:** JPG, JPEG, PNG, WebP; max 2 MB (`app.profile.avatar-max-size-bytes`)

---

## Frontend Additions

| Area | Files |
|------|-------|
| Page | `ProfilePage.tsx` — `/profile` |
| View | `ProfileViewSection.tsx`, `ProfileAvatar.tsx` |
| Edit | `ProfileEditForm.tsx`, `profileFormState.ts` |
| Avatar | `ProfileAvatarUpload.tsx` |
| API | `profileApi.ts` — `get`, `update`, `uploadAvatar`, `deleteAvatar`, `fetchAvatarBlob` |
| Types | `types/profile.ts`; `types/auth.ts` (+ `avatarUrl`) |
| Auth | `AuthProvider.tsx` — `refreshProfile()`, token update on email change |
| Navigation | `navigation.tsx` — My Profile; `AppRoutes.tsx` — `/profile` route |
| Route fix | `MustChangePasswordRoute.tsx` — allow voluntary `/change-password` access |
| Messages | `profileMessages.ts` |
| Utils | `profileInitials.ts` |

---

## Validation & Defects

| Phase | Manual validation | Notes |
|-------|-------------------|-------|
| Phase 1 — Profile View | **Passed** | — |
| Phase 2 — Edit Profile | **Passed** (after fix) | PM-D01: import collision |
| Phase 3 — Change Password Entry | **Passed** (after fix) | PM-D02: route redirect |
| Phase 4 — Avatar | **Passed** | — |

| ID | Phase | Symptom | Fix |
|----|-------|---------|-----|
| PM-D01 | 2 | App failed to load; `ProfileEditForm` import error | Renamed `profileEditForm.ts` → `profileFormState.ts` (case-insensitive FS collision) |
| PM-D02 | 3 | Change Password redirected to `/` | Removed redirect in `MustChangePasswordRoute` for users without `mustChangePassword` |

See `docs/testing-and-defect-history.md` for regression checklist and test coverage.

---

## Security Considerations

| Concern | Mitigation |
|---------|------------|
| Self-service only | All profile APIs use `@PreAuthorize("isAuthenticated()")`; operations scoped to `AuthenticatedUser` ID |
| Email uniqueness | Case-insensitive duplicate check; new JWT issued on email change |
| Avatar access | `GET /profile/avatar` requires authentication; users can only fetch their own avatar |
| File type validation | Content-type and extension allowlist (JPEG, PNG, WebP) |
| File size limit | Configurable max size (default 2 MB) |
| Storage isolation | Files stored under per-user path prefix `avatars/{userId}/` |
| Idempotent delete | `DELETE /avatar` returns `204` when no avatar exists (no information leak) |
| Must-change-password flow | Change Password entry hidden when flag is set; existing `MustChangePasswordFilter` unchanged |

---

## Test Summary

- Frontend: **132 tests** (31 files), production build green
- Backend: **184 tests** run, **10 skipped** (Docker integration); profile module: `ProfileServiceTest`, `ProfileControllerTest`, `ProfileMethodSecurityTest`
- Manual validation: **Passed** for all four phases (Phases 2 and 3 after defect fixes)

---

## Backlog (post v0.5)

| ID | Item |
|----|------|
| UM-002 | User Details Drawer |
| UM-003 | Unified cross-field search |
| UM-004 | View User Details |
| UM-006 | Downloadable import error report |
| — | Notifications |
| — | Global Search |
| — | AI Features |

Roadmap: `docs/project-roadmap.md`  
Workstream summary: `docs/releases/profile-management-final-summary.md`
