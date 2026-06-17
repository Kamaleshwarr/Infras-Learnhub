# Engineering Learning Hub v0.6

**Theme:** In-App Notification Infrastructure  
**Classification:** Foundation release — **not** notification feature complete  
**Merged:** 2026-06-16  
**PR:** #28 (`cursor/v0.6-notifications-dd41`)

Workstream summary: `docs/releases/notification-infrastructure-final-summary.md`  
Proposed follow-on: `docs/releases/release-v0.6.1.md` (released with PR #29)

---

## Release intent

v0.6 delivers the **persistent in-app notification platform** (persistence, APIs, consumer UI, and backend producers). It does **not** deliver end-to-end notification validation through the application UI, because certificate submit and admin review workflows remain placeholder pages in the frontend.

**Feature-complete notifications** require at least one producer to be triggerable and verifiable end-to-end through the application. That milestone is targeted for **v0.6.1** (Certificate Workflow UI & Notification E2E Validation).

---

## Delivered

### Notification persistence

- Flyway `V9__create_notifications.sql` — `notifications` table, indexes, type check constraint
- `Notification` entity, `NotificationType`, `NotificationEntityType`

### Notification APIs

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/notifications` | Paginated inbox (`read`, `type` filters) |
| `GET` | `/api/v1/notifications/unread-count` | Unread badge count |
| `PATCH` | `/api/v1/notifications/{id}/read` | Mark one notification read |
| `PATCH` | `/api/v1/notifications/read-all` | Mark all read |

- `com.company.learninghub.notification` module — `NotificationService`, `NotificationFactory`, `NotificationMapper`, `NotificationController`
- `MustChangePasswordFilter` allowlists `/api/v1/notifications/**` for inbox access during forced password change

### Bell and inbox UI

- Header notification bell with unread badge
- Dropdown preview (latest 10) with mark-all-read
- `/notifications` page with All/Unread tabs and pagination
- Sidebar **Notifications** navigation item
- `MustChangePasswordRoute` allows `/notifications`

### Badge synchronization

- `NotificationProvider` shared context
- `refresh()` after mark-read and mark-all-read on page and dropdown (NTF-D02)

### Certificate notification producers (backend)

Producers wired in `CertificateSubmissionService` only:

| Type | Recipient | Trigger |
|------|-----------|---------|
| `CERTIFICATE_SUBMITTED` | All active `ADMIN` users | Employee submits a certificate |
| `CERTIFICATE_APPROVED` | Submitting employee | Admin approves submission |
| `CERTIFICATE_REJECTED` | Submitting employee | Admin rejects submission |

Covered by unit and integration tests (`NotificationServiceTest`, `NotificationFactoryTest`, `CertificateSubmissionServiceTest`).

### Deferred from in-app (future email workstream)

Account lifecycle types remain in schema/enum for historical rows but are **not generated** in v0.6:

- `ACCOUNT_CREATED`
- `ACCOUNT_ACTIVATED`
- `ACCOUNT_DEACTIVATED`
- `PASSWORD_RESET_BY_ADMIN`

---

## Known limitation

| Limitation | Impact |
|------------|--------|
| Certificate producer workflows are **backend-only** today | Submit, approve, and reject trigger notifications via API/Swagger only |
| `SubmitCertificatePage` and `MySubmissionsPage` are placeholders | Employees cannot submit certificates through the UI |
| No admin certificate review UI | Admins cannot approve/reject through the UI |
| **End-to-end notification validation is blocked** | Cannot verify generation → delivery → inbox through the application until certificate workflow UI ships (v0.6.1) |

Swagger/API validation of producers is useful for backend confidence but does **not** satisfy the application E2E bar.

---

## Validation fixes during v0.6

| ID | Issue | Fix |
|----|-------|-----|
| NTF-D01 | Backend startup `BeanInstantiationException` on `NotificationService` | Added `@Autowired` on production constructor |
| NTF-D02 | Bell badge stale after mark-read on `/notifications` | Shared `NotificationProvider` + `refresh()` after read mutations |

---

## Validated at merge (infrastructure)

1. Backend starts; Flyway V9 applied
2. Notification APIs — list, unread-count, mark-read, mark-all-read
3. Bell, dropdown, `/notifications` page render and interact correctly
4. Mark-read on page and dropdown updates badge immediately
5. `mustChangePassword` user can access `/notifications` and notification APIs
6. User create / activate / deactivate / reset-password → **no** new in-app notification (Option B scope)

## Deferred to v0.6.1 (E2E producer validation)

1. Certificate submit → admin badge increments (`CERTIFICATE_SUBMITTED`)
2. Approve / reject → employee notification (`CERTIFICATE_APPROVED` / `CERTIFICATE_REJECTED`)
3. Full submit → review → notify → inbox path through the application UI

---

## Explicitly out of scope (v0.6)

- Initiative, project, and study-material in-app notifications
- Scheduled expiry reminders
- Email notifications
- Notification preferences
- WebSockets / SSE / push
- Certificate workflow UI (submit, my submissions, admin review)
