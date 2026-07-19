# Release Report: Communication C4 — Resend Email Provider

**Release:** Communication Framework C4  
**Date:** 2026-07-19  
**Branch:** `cursor/communication-c4-resend-provider-91e0`

---

## Summary

Added production-ready `ResendEmailProvider` as a configuration-selectable implementation of the existing `EmailProvider` abstraction. `LogEmailProvider` remains the default for local development. No business modules were changed.

---

## What was delivered

### ResendEmailProvider

- Implements `EmailProvider` using Resend REST API (`POST /emails`)
- Supports from, to, subject, plain text, and HTML
- Bearer token authentication from environment configuration
- Connect and read timeouts
- Returns `EmailDeliveryResult` for all outcomes (no unexpected runtime exceptions)

### Configuration

| Property | Default |
|----------|---------|
| `app.communication.email.provider` | `log` |
| `app.communication.email.resend.api-key` | (empty — from env) |
| `app.communication.email.resend.base-url` | `https://api.resend.com` |

### Provider selection

`EmailProviderConfiguration` routes:

- `log` → `LogEmailProvider`
- `resend` → `ResendEmailProvider`
- `smtp` → `SmtpEmailProvider` (legacy, unchanged)

---

## What was NOT changed

| Area | Status |
|------|--------|
| `EmailProvider` interface | Unchanged |
| `EmailChannelHandler` / outbox retry | Unchanged |
| Certificate module (C3) | Unchanged |
| Password reset | Unchanged |
| User Management / Projects / Learn | Unchanged |
| Frontend | Unchanged |
| C5 diagnostics | Not started |

---

## Tests

| Test | Coverage |
|------|----------|
| `ResendEmailProviderTest` | Success, API error, timeout, missing API key |
| `EmailProviderConfigurationTest` | Provider selection |
| `CommunicationPropertiesResendBindingTest` | YAML/env binding |
| `LogEmailProviderTest` | Regression |
| `EmailChannelHandlerTest` | Outbox retry regression |

---

## Files changed

- `communication/email/ResendEmailProvider.java` (new)
- `communication/config/CommunicationProperties.java`
- `communication/email/EmailProviderConfiguration.java`
- `application.yml`
- Test classes (3 new)
- `docs/communication/email-providers.md` (new)
- `docs/communication/README.md` (updated)
- `docs/releases/release-communication-c4-resend-provider.md`

---

## Switching to Resend in production

```bash
export APP_COMMUNICATION_EMAIL_PROVIDER=resend
export APP_COMMUNICATION_EMAIL_RESEND_API_KEY=re_...
export APP_MAIL_FROM=noreply@yourdomain.com
```

Verify sender domain in Resend dashboard before production use.
