# Communication Framework — Architecture Review

**Date:** 2026-07-12  
**Type:** Architecture review only — **no implementation**  
**Approver:** Product owner / tech lead (manual sign-off required)

---

## Executive summary

Learning Hub already has **production-grade in-app notification infrastructure** (v0.6/v0.6.1) and a **minimal but real email channel** for password reset (v0.2). The recommended path is **Option C — Communication Framework**: introduce a thin orchestration layer that domain services call with **communication events**, fanning out to **channel adapters** (in-app, email) while preserving existing inbox APIs and UI.

Do **not** create a standalone Email module or overload `NotificationService` with email, template, and queue concerns.

---

## 1. Repository context reviewed

Read and cross-checked against production code:

- `.cursor/architecture.md`, `.cursor/project-context.md`, `.cursor/engineering-standards.md`, `.cursor/coding-standards.md`
- All remaining `.cursor/` agents, commands, templates
- `README.md`, `docs/project-roadmap.md`, `docs/development-workflow.md`, `docs/testing-guide.md`, `docs/contributing.md`
- `docs/releases/notification-infrastructure-final-summary.md`, `docs/releases/release-v0.6.md`, `docs/releases/release-v0.6.1.md`, `docs/releases/release-v0.2.md`
- `docs/project/12-future-compatibility.md`
- Production packages: `notification`, `auth` (EmailService), `submission`, `user`, `learn`, `projectknowledge`, `profile`

**Version 1 priorities (authoritative):**

1. Communication Framework (Email + In-App)
2. AI Assistant
3. Final UI/UX Polish

**Version 2 deferred:** Career Paths, Learn v2, Certification Catalog, 500+ technologies, advanced gamification/analytics.

---

## 2. Existing notification infrastructure (audit)

### Backend — `com.company.learninghub.notification`

| Component | File | Responsibility |
|-----------|------|----------------|
| Entity | `domain/Notification.java` | Inbox row: user, type, title, message, entity ref, actionPath, readAt, createdAt |
| Types | `domain/NotificationType.java` | 7 enum values (certificate + account lifecycle) |
| Entity types | `domain/NotificationEntityType.java` | `CERTIFICATE_SUBMISSION`, `USER` |
| Repository | `repository/NotificationRepository.java` | JPA + Specifications; `markAllReadForUser` bulk update |
| Factory | `service/NotificationFactory.java` | Builds `Notification` entities with copy + deep links |
| Service | `service/NotificationService.java` | Inbox read APIs + producer methods |
| Controller | `controller/NotificationController.java` | `GET /notifications`, unread-count, mark-read, mark-all-read |
| DTOs | `dto/NotificationResponse.java`, `UnreadCountResponse.java` | API contracts |
| Mapper | `mapper/NotificationMapper.java` | Entity → response |

**Producer methods on `NotificationService`:**

| Method | Called by production code? |
|--------|---------------------------|
| `notifyCertificateSubmitted` | **Yes** — `CertificateSubmissionService` after create |
| `notifyCertificateApproved` | **Yes** — after admin approve |
| `notifyCertificateRejected` | **Yes** — after admin reject |
| `notifyPasswordResetByAdmin` | **No** — method exists, no caller |
| `notifyAccountActivated` | **No** |
| `notifyAccountDeactivated` | **No** |
| `notifyAccountCreated` | **No** |

Account lifecycle in-app notifications were **intentionally not wired** in v0.6 (deferred to email workstream). Factory + enum + DB check constraint already support them.

### Database — `V9__create_notifications.sql`

```sql
notifications (
  id, user_id, type, title, message,
  entity_type, entity_id, action_path,
  read_at, created_at
)
```

Indexes: `(user_id, created_at DESC)`, partial unread `(user_id, read_at) WHERE read_at IS NULL`.

**No** queue, audit, preference, or email delivery tables exist.

### Frontend

| Component | Path | Notes |
|-----------|------|-------|
| Provider | `notifications/NotificationProvider.tsx` | Unread count; 60s poll + window focus refresh |
| Hook | `notifications/useNotifications.ts` | Context consumer |
| Bell | `components/notifications/NotificationBell.tsx` | Badge in header |
| Menu | `components/notifications/NotificationMenu.tsx` | Dropdown; mark read; navigate via `actionPath` |
| Page | `pages/notifications/NotificationsPage.tsx` | Full inbox; tabs; pagination |
| API | `api/notificationsApi.ts` | list, unreadCount, markRead, markAllRead |
| Types | `types/notifications.ts` | Mirrors backend enums |

**Not present:** notification preferences, email settings, profile communication section.

### Security

`MustChangePasswordFilter` allows `GET` and `PATCH` on `/api/v1/notifications/**` while password change is required.

### Async / events / schedulers

| Capability | Present? |
|------------|----------|
| `@Scheduled` jobs | **No** |
| `@Async` | **No** |
| `ApplicationEventPublisher` | **No** |
| Message queue (Redis, SQS, etc.) | **No** |

---

## 3. Existing email infrastructure (audit)

### `auth.EmailService`

- Location: `backend/src/main/java/com/company/learninghub/auth/service/EmailService.java`
- Dependency: `spring-boot-starter-mail` → `JavaMailSender` (optional bean when SMTP not configured)
- Config: `MailProperties` (`app.mail.mode`, `app.mail.from`)
- Modes:
  - `log` (default): logs reset URL; no SMTP required
  - `smtp`: sends via `JavaMailSender`; requires `spring.mail.*`
- Templates: classpath `templates/email/forgot-password.html` + `.txt`
- Rendering: simple `{{placeholder}}` string replacement (not Thymeleaf/Freemarker)
- Single method: `sendPasswordResetEmail(recipient, fullName, resetUrl, expiration)`

### Caller

`PasswordResetService.issueResetToken()` → `EmailService.sendPasswordResetEmail()` on forgot-password for active users.

**Synchronous, in-request:** email failure throws `IllegalStateException` and fails the forgot-password flow (after token is saved). Account enumeration is still prevented at API level (always 202).

### Configuration (`application.yml`)

```yaml
spring.mail.host/port/username/password  # env: SPRING_MAIL_*
app.mail.mode: log|smtp                   # env: APP_MAIL_MODE
app.mail.from                            # env: APP_MAIL_FROM
app.password-reset.frontend-reset-url
```

`docker-compose.yml` does **not** set mail env vars (local dev uses log mode).

### Admin password reset

`UserManagementService` → `PasswordService` for admin reset. **No email** sent today; only in-app helper exists unused.

---

## 4. Architecture options comparison

### Option A — Standalone Email Service

New `email` module parallel to `notification`. Domain services call `EmailService` and `NotificationService` separately.

| Pros | Cons |
|------|------|
| Clear email ownership | Duplicate event/template/audit logic |
| Fast for email-only features | Business modules learn two APIs |
| | Hard to add Slack/Teams later |

**Verdict:** Rejected — violates DRY and future channel goals.

### Option B — Extend NotificationService

Add email sending, templates, queue, and audit to existing `NotificationService`.

| Pros | Cons |
|------|------|
| Single entry point | God service; mixes inbox read API with outbound delivery |
| Minimal new packages | Email provider logic in wrong bounded context |
| | Hard to test; breaks single responsibility |

**Verdict:** Rejected — inbox service should not own SMTP.

### Option C — Communication Framework (recommended)

Introduce `com.company.learninghub.communication` orchestration:

```text
Domain Service (after successful commit)
    → CommunicationService.publish(CommunicationEvent)
        → CommunicationDispatcher
            → InAppChannelAdapter  → NotificationService / Factory (existing)
            → EmailChannelAdapter  → TemplateRenderer + EmailProvider
        → CommunicationDeliveryLog (audit)
        → CommunicationOutbox (optional queue for email retries)
```

| Pros | Cons |
|------|------|
| Extends existing code | New package + migration |
| Single event API for domains | Refactor password reset into framework |
| Channel plug-ins | Initial design effort |
| Preserves inbox APIs | |
| Matches `docs/project/12-future-compatibility.md` | |

**Verdict:** **Recommended.**

---

## 5. Recommended package structure (proposed)

```text
com.company.learninghub.communication/
├── domain/
│   ├── CommunicationEventType.java      # stable event enum
│   ├── CommunicationChannel.java        # IN_APP, EMAIL, (future)
│   ├── CommunicationEvent.java          # immutable record
│   └── CommunicationPriority.java       # IMMEDIATE, NORMAL
├── service/
│   ├── CommunicationService.java        # publish API for domain modules
│   ├── CommunicationDispatcher.java     # route to channels
│   └── CommunicationIdempotencyService.java
├── channel/
│   ├── CommunicationChannelHandler.java # interface
│   ├── InAppChannelHandler.java         # delegates to notification module
│   └── EmailChannelHandler.java
├── email/
│   ├── EmailProvider.java               # interface
│   ├── SmtpEmailProvider.java
│   ├── LogEmailProvider.java
│   └── EmailMessage.java
├── template/
│   ├── TemplateRenderer.java
│   ├── TemplateCatalog.java
│   └── TemplateVariableResolver.java
├── persistence/
│   ├── CommunicationDeliveryLog.java
│   ├── CommunicationOutbox.java         # if queue adopted
│   └── repositories...
└── config/
    └── CommunicationProperties.java
```

**Keep** `notification` package for inbox domain. **Deprecate** direct domain → `NotificationService.notify*` calls over time; route through `CommunicationService`.

**Migrate** `auth.EmailService` template rendering into `communication.template`; keep thin wrapper or delegate for backward compatibility during C1.

---

## 6. Channel architecture

| Channel | V1 | Handler | Persistence |
|---------|----|---------|-------------|
| In-app | Yes | `InAppChannelHandler` | `notifications` table (existing) |
| Email | Yes | `EmailChannelHandler` | `communication_delivery_log` + optional outbox |
| Slack | No | Future `SlackChannelHandler` | Future |
| Teams | No | Future | Future |
| SMS / Push / WhatsApp | No | Future | Future |

**Multi-channel:** One event may target multiple channels (e.g. certificate approved → in-app + email).

**Fallback:** No cross-channel fallback in V1 (if email fails, in-app still delivered). Optional admin alert on repeated email failure in C5.

**Inactive users:** Skip all channels for `active=false` except where legally required (none in V1).

---

## 7. Provider abstraction (email)

```java
public interface EmailProvider {
    EmailDeliveryResult send(EmailMessage message);
    boolean isHealthy();
    String providerName();
}
```

| Implementation | V1 | Notes |
|----------------|----|-------|
| `LogEmailProvider` | Yes | Replaces inline log branch in current EmailService |
| `SmtpEmailProvider` | Yes | Wraps `JavaMailSender` |
| `SesEmailProvider` | Future | AWS SES |
| `SendGridEmailProvider` | Future | |
| `MailgunEmailProvider` | Future | |
| `AzureCommunicationEmailProvider` | Future | |

Provider selected via `app.communication.email.provider=smtp|log` (inherits from `app.mail.mode` during migration).

Business modules **never** reference `JavaMailSender`.

---

## 8. User preferences (recommendation)

### Current state

- No `user_communication_preferences` table
- No profile UI for communication settings
- `User.email` exists and is validated on profile update

### Version 1 recommendation — **defer preference UI; minimal backend rules**

| Rule | V1 behavior |
|------|-------------|
| Security / account emails | **Always send** (password reset, admin reset notice, activation) — no opt-out |
| Operational emails | Send to active users with valid email |
| In-app notifications | Always deliver (existing behavior) |
| Category toggles | **Defer to C4 or Version 2** unless product insists |
| Global `emailEnabled` | Optional single column on `users` in C4 — default `true` |

**Rationale:** Preferences add UI, migration, and testing surface. V1 goal is reliable delivery + audit. Add preferences when volume or complaint risk justifies it.

If C4 is approved, minimal schema:

```text
users.email_notifications_enabled BOOLEAN DEFAULT TRUE
-- category flags deferred to V2
```

---

## 9. Audit model (recommendation)

**New table (proposed V20):** `communication_delivery_log`

| Column | Purpose |
|--------|---------|
| `id` | UUID PK |
| `correlation_id` | Groups multi-channel delivery for one event |
| `idempotency_key` | UNIQUE — prevents duplicate sends |
| `event_type` | `CommunicationEventType` |
| `channel` | IN_APP, EMAIL |
| `recipient_user_id` | FK users (nullable for external email) |
| `recipient_email` | Snapshot for email |
| `template_key` | e.g. `certificate-approved` |
| `status` | PENDING, SENT, FAILED, SKIPPED |
| `failure_reason` | Text |
| `retry_count` | Int |
| `provider_name` | smtp, log, … |
| `provider_message_id` | Optional SMTP message id |
| `sent_at` | TIMESTAMPTZ |
| `created_at` | TIMESTAMPTZ |

**Minimal V1:** Log email attempts and final status. In-app can log SKIPPED/SENT for traceability without duplicating `notifications` rows.

**Retention:** 90 days online; archive policy TBD.

---

## 10. API recommendation

### Keep unchanged (clients depend on these)

- `GET/PATCH /api/v1/notifications/**` — inbox APIs

### New — admin diagnostics only (C5)

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/admin/communication/health` | Provider reachability, queue depth |
| `POST` | `/api/v1/admin/communication/preview` | Render template with sample variables |
| `POST` | `/api/v1/admin/communication/test-send` | Send test email to admin |

**Not in V1:** public APIs for preferences until C4 UI exists.

---

## 11. Performance considerations

**Expected volume (internal enterprise, rough):**

| Event | Est. frequency |
|-------|----------------|
| Certificate submitted | Low–medium (per submission × admin count) |
| Certificate approved/rejected | Low–medium |
| Account lifecycle | Low (admin-driven) |
| Learn progress | Medium if enabled — consider digest in V2 |
| Project changes | Low–medium |

**V1 targets:** < 500 emails/day; peak < 20/minute.

| Concern | Recommendation |
|---------|----------------|
| Idempotency | `idempotency_key = {eventType}:{entityId}:{recipientId}:{channel}` |
| Duplicate prevention | UNIQUE on `idempotency_key` in delivery log |
| Retry | Email: 3 attempts, exponential backoff (1m, 5m, 15m) via outbox poller |
| Timeout | SMTP connect 10s; send 30s |
| Queue | **Outbox table + `@Scheduled` poller** (5–10s interval) for email; in-app sync in transaction |
| User request latency | Domain transaction commits before email send; never block HTTP on SMTP |

---

## 12. Integration map (completed modules)

See [communication-events.md](./communication-events.md) for full event catalog.

| Module | V1 events | Channels | Notes |
|--------|-----------|----------|-------|
| Certifications | submitted, approved, rejected | In-app + email | Replace direct `NotificationService` calls |
| Authentication | password reset, admin reset | Email (+ in-app for admin reset) | Migrate existing forgot-password |
| User management | created, activated, deactivated | Email (+ in-app optional) | Wire unused notify* methods via framework |
| Learn | stage completed, roadmap completed, continue reminder | Email optional V1; in-app optional | Reminder needs scheduler (new) |
| Initiatives | assigned, due, completed | **Defer** — no assignment model today | V2 or when feature exists |
| Projects | member added, repo/env added | Email + optional in-app | P4 team events |
| Leaderboards | milestone | **Defer** — L2 gamification | V2 |
| Dashboard | none (consumer) | — | Links only |
| Profile | none (recipient) | — | Preferences in C4 |

---

## 13. Test strategy (design)

| Layer | Tests |
|-------|-------|
| Template rendering | Unit: all variables substituted; missing var fails safe |
| Communication events | Unit: dispatcher routes to correct channels |
| In-app channel | Integration: event → row in `notifications` |
| Email channel | Unit with `LogEmailProvider`; integration with GreenMail |
| Retry | Unit: outbox marks FAILED, retries, succeeds |
| Idempotency | Integration: duplicate publish → one email |
| Preferences | Unit: SKIPPED status when email disabled |
| Audit | Integration: delivery log rows per channel |
| Frontend | Existing notification tests unchanged; C4 adds settings page tests |
| E2E | Submit cert → admin inbox + admin email (log mode assert) |

---

## 14. Implementation phases (recommended)

| Phase | Scope | Flyway | Exit criteria |
|-------|-------|--------|---------------|
| **C1** | Communication package, event types, dispatcher, channel interfaces, delivery log, outbox poller, provider abstraction; migrate password reset email | V20 | Tests pass; forgot-password still works |
| **C2** | Master layout, template renderer upgrade, template catalog | — | All V1 templates render HTML+text |
| **C3** | Certifications + user lifecycle + selected learn/project events | — | E2E cert + account emails |
| **C4** | `email_notifications_enabled` + profile toggle (if approved) | V21 optional | Preference respected in dispatcher |
| **C5** | Admin health, preview, test send | — | Admin can verify SMTP in staging |

Each phase: full 11-step workflow + implementation report in `docs/releases/`.

---

## 15. Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| SMTP misconfiguration in production | High | Log mode default; health endpoint; test-send in C5 |
| Email sent but transaction rolls back | Medium | Outbox pattern; send after commit |
| Duplicate emails on retry | Medium | Idempotency keys |
| `NotificationService` bloat if not refactored | Medium | Route producers through framework only |
| Learn reminder needs scheduler | Medium | Scope to C3 or defer reminders to C3b |
| Doc/code drift on account notifications | Low | Wire or remove dead notify* methods in C3 |
| PII in email bodies | Medium | Template review; no secrets in URLs with tokens |
| Volume spike on admin fan-out (cert submitted) | Low | Batch email; BCC avoided; individual sends |

---

## 16. Open architectural decisions (need approval)

| ID | Decision | Options | Recommendation |
|----|----------|---------|----------------|
| OD-C01 | Outbox vs `@Async` for email | Outbox table + scheduler vs Spring `@Async` | **Outbox** — auditable, retryable |
| OD-C02 | Template engine | Keep `{{var}}` vs Thymeleaf | **Thymeleaf** for master layout; migrate forgot-password |
| OD-C03 | Package name | `communication` vs extend `notification` | **`communication`** — clear bounded context |
| OD-C04 | Account lifecycle in-app + email | Both vs email only | **Both** for parity with factory |
| OD-C05 | Learn progress emails in V1 | Yes vs defer | **Defer reminders**; optional milestone email only |
| OD-C06 | Admin cert submitted email | All admins vs configurable | **All active admins** (current in-app fan-out) |
| OD-C07 | Preference UI in V1 | C4 vs V2 | **Defer UI to C4**; default send all |
| OD-C08 | Continue-learning reminder | Requires scheduler — in V1? | **Defer to C3b** or Version 2 |
| OD-C09 | Initiative emails | No assignment model exists | **Defer** |
| OD-C10 | Refactor `auth.EmailService` | Delete vs delegate | **Delegate to framework** in C1, remove in C3 |

---

## 17. Documentation / code mismatches found

| Topic | Documentation | Production code |
|-------|---------------|-----------------|
| Account lifecycle in-app | Enums + factory exist; v0.6 docs say deferred to email | Methods on `NotificationService` **never called** by `UserManagementService` |
| Email scope | Some docs imply "no email infrastructure" | **Password reset email fully implemented** |
| `NotificationService` DI | Coding standards say no `@Autowired` fields | Uses `@Autowired` on constructor (pre-existing) |
| Flyway version in contributing.md | Says V15 | **V19** is current |
| Project P4 in phased plan | P4 = search | **P4 = Team & Contacts**; search skipped |

---

## 18. Files reviewed

### Backend
- `notification/**` (all 10 files)
- `auth/service/EmailService.java`, `PasswordResetService.java`, `MailProperties.java`
- `auth/security/MustChangePasswordFilter.java`, `SecurityConfig.java`
- `submission/service/CertificateSubmissionService.java`
- `user/service/UserManagementService.java`
- `resources/application.yml`, `templates/email/*`
- `resources/db/migration/V9__create_notifications.sql`
- `pom.xml` (spring-boot-starter-mail)

### Frontend
- `notifications/NotificationProvider.tsx`, `useNotifications.ts`
- `components/notifications/*`
- `pages/notifications/NotificationsPage.tsx`
- `api/notificationsApi.ts`, `types/notifications.ts`
- `layout/AppLayout.tsx`, `layout/navigation.tsx`
- `pages/profile/ProfilePage.tsx`

### Documentation
- All `.cursor/*` listed in Step 0
- `docs/releases/notification-infrastructure-final-summary.md`
- `docs/project/12-future-compatibility.md`
- `docs/project-roadmap.md`

---

## Approval gate

| Item | Status |
|------|--------|
| Architecture review | Complete |
| Production code changed | **No** |
| Flyway migration created | **No** |
| APIs implemented | **No** |
| Frontend changes | **No** |
| Email / Communication implementation | **No** |
| AI Assistant | **Not started** |
| Final UI Polish | **Not started** |
| Learn v2 | **Not started** |

**Implementation blocked until this review is manually approved.**
