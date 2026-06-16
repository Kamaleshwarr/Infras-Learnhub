# Engineering Learning Hub v0.6

**Theme:** In-App Notifications (certificate workflow)  
**Status:** In validation (draft PR #28)  
**Branch:** `cursor/v0.6-notifications-dd41`

---

## Scope

v0.6 delivers a persistent **in-app notification inbox** for **certificate workflow events only**.

### In-app notification types (actively produced)

| Type | Recipient | Trigger |
|------|-----------|---------|
| `CERTIFICATE_SUBMITTED` | All active `ADMIN` users | Employee submits a certificate |
| `CERTIFICATE_APPROVED` | Submitting employee | Admin approves submission |
| `CERTIFICATE_REJECTED` | Submitting employee | Admin rejects submission |

### Deferred from in-app (future email workstream)

Account lifecycle types remain in the schema and API for **historical rows** but are **not generated** in v0.6:

- `ACCOUNT_CREATED`
- `ACCOUNT_ACTIVATED`
- `ACCOUNT_DEACTIVATED`
- `PASSWORD_RESET_BY_ADMIN`

**Rationale:** These events are either unreachable in the inbox (deactivated users), redundant with enforced login/password flows, or better delivered via email at the time of the event.

---

## Backend

### Flyway

- `V9__create_notifications.sql` — `notifications` table, indexes, type check constraint

### Module

`com.company.learninghub.notification`

- `Notification` entity, `NotificationType`, `NotificationFactory`, `NotificationService`
- REST APIs under `/api/v1/notifications`
- Producers wired in `CertificateSubmissionService` only

### APIs

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/notifications` | Paginated inbox (`read`, `type` filters) |
| `GET` | `/api/v1/notifications/unread-count` | Unread badge count |
| `PATCH` | `/api/v1/notifications/{id}/read` | Mark one notification read |
| `PATCH` | `/api/v1/notifications/read-all` | Mark all read |

### Must-change-password access

`MustChangePasswordFilter` allowlists notification API paths so users with `mustChangePassword = true` can read the inbox (certificate notifications only in practice).

---

## Frontend

- Header notification bell with unread badge
- Dropdown preview (latest 10) with mark-all-read
- `/notifications` page with All/Unread tabs and pagination
- Sidebar **Notifications** navigation item
- `NotificationProvider` shared context — badge syncs immediately after mark-read on page or dropdown
- `MustChangePasswordRoute` allows `/notifications`

---

## Validation fixes during v0.6

| ID | Issue | Fix |
|----|-------|-----|
| NTF-D01 | Backend startup `BeanInstantiationException` on `NotificationService` | Added `@Autowired` on production constructor |
| NTF-D02 | Bell badge stale after mark-read on `/notifications` | Shared `NotificationProvider` + `refresh()` after read mutations |

---

## Explicitly out of scope (v0.6)

- Initiative, project, and study-material in-app notifications
- Scheduled expiry reminders
- Email notifications
- Notification preferences
- WebSockets / SSE / push
- Admin certificate review UI (approve/reject via Swagger)

---

## Suggested validation

1. Backend starts; Flyway V9 applied
2. Certificate submit → admin badge increments
3. Approve/reject → employee notification
4. User create / activate / deactivate / reset-password → **no** new in-app notification
5. Mark-read on page and dropdown updates badge immediately
6. Historical account-type rows (if any) still readable in inbox
