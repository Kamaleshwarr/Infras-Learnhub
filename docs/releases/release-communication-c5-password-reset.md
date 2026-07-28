# Release Report: Communication C5 — Password Reset Migration

**Release:** Communication Framework C5  
**Date:** 2026-07-28  
**Branch:** `cursor/communication-c5-password-reset-91e0`

---

## Summary

Migrated self-service password reset email delivery from the legacy `auth.EmailService` pipeline to the Communication Framework. All application emails now flow through a single pipeline: `CommunicationService` → outbox → `EmailChannelHandler` → `EmailProvider` (LOG / Resend).

---

## What was delivered

### Password reset communication publisher

- `PasswordResetCommunicationPublisher` publishes `PASSWORD_RESET_REQUESTED` (EMAIL only)
- `PasswordResetService` no longer references email providers, templates, or SMTP

### Legacy removal

- Deleted `auth.EmailService`
- Deleted `auth.config.MailProperties`
- Deleted `templates/email/forgot-password.{html,txt}`
- Removed `app.mail` configuration block from `application.yml`

### Template reuse

- Password reset uses C2 `password-reset` Thymeleaf templates via `EmailTemplateRenderer`
- Single email rendering pipeline for all modules

---

## Architecture (after C5)

```
PasswordResetService
      ↓
PasswordResetCommunicationPublisher.publishPasswordResetRequested()
      ↓
CommunicationService.publish()
      ↓
CommunicationDispatcher
      ↓
EmailChannelHandler → communication_outbox
      ↓
CommunicationOutboxProcessor
      ↓
EmailTemplateRenderer (password-reset template)
      ↓
EmailProvider (LOG / Resend / SMTP)
```

---

## What was NOT changed

| Area | Status |
|------|--------|
| Token generation, hashing, expiry | Unchanged |
| Forgot/reset API contracts | Unchanged |
| Enumeration-safe 202 response | Unchanged |
| Certificate module (C3) | Unchanged |
| User Management / Projects / Learn / Leaderboards | Unchanged |
| Frontend | Unchanged |
| C6 diagnostics | Not started |

---

## Tests

| Test | Coverage |
|------|----------|
| `PasswordResetCommunicationPublisherTest` | Event payload, channels, idempotency |
| `PasswordResetServiceTest` | Publisher invoked; security behaviour |
| `PasswordResetCommunicationIntegrationTest` | Forgot-password → outbox → SENT → reset completes |
| `PasswordManagementIntegrationTest` | Regression |
| `EmailTemplateRendererTest` | `password-reset` template rendering |

---

## Files changed

- `auth/communication/PasswordResetCommunicationPublisher.java` (new)
- `auth/service/PasswordResetService.java`
- `LearningHubApplication.java`
- `application.yml`
- Deleted: `EmailService.java`, `MailProperties.java`, legacy templates
- Tests (3 new/updated)
- `docs/communication/*` (updated)
- `docs/releases/release-communication-c5-password-reset.md`

---

## Configuration

Password reset email uses Communication Framework settings:

```yaml
app.communication.email.provider: log  # or resend
app.password-reset.frontend-reset-url: http://localhost:5173/reset-password
```

Legacy `app.mail.mode` is removed. Use `APP_COMMUNICATION_EMAIL_PROVIDER` instead.
