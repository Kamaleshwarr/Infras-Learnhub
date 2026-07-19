# Release Report: Communication C2 — Email Template Engine

**Release:** Communication Framework C2  
**Date:** 2026-07-18  
**Branch:** `cursor/communication-c2-template-engine-91e0`  
**Depends on:** C1 Communication Framework (`cursor/communication-framework-c1-91e0`)

---

## Summary

Introduced a reusable Thymeleaf-based email template engine on top of the Communication Framework (C1). The engine provides branded HTML and plain-text templates, a structured template model, and preview-ready render methods. No business modules were wired; password reset remains on the legacy `auth.EmailService`.

---

## What was delivered

### Template engine (`communication.template`)

- `CommunicationEmailTemplate` — event-to-template mapping
- `EmailTemplateModel` — structured render model with future extensibility
- `EmailTemplateVariables` — centralized variable resolution
- `EmailTemplateRenderer` — HTML, text, and subject rendering + `buildEmailMessage()`
- `RenderedEmail` — render result record
- `CommunicationEmailTemplateConfiguration` — isolated HTML/TEXT Thymeleaf engines

### Master layout and fragments

- Responsive HTML shell with Engineering Learning Hub branding
- Shared fragments: header, footer, support section, CTA button, styles
- Plain-text shell and body templates

### Template catalog (12 templates × HTML + text)

- Certification: submitted, approved, rejected
- Account: created, activated, deactivated
- Password reset (new templates only — not wired)
- Project: member added, repository added, environment added
- Generic notification

### Integration

- `EmailChannelHandler` now uses `EmailTemplateRenderer` instead of `SimpleEmailContentBuilder`
- `SimpleEmailContentBuilder` removed
- `spring-boot-starter-thymeleaf` added; `spring.thymeleaf.enabled: false` to avoid MVC impact

---

## What was intentionally not changed

| Area | Status |
|------|--------|
| `PasswordResetService` / `auth.EmailService` | Unchanged |
| `NotificationService` | Unchanged |
| `CommunicationDispatcher` behavior | Unchanged |
| Certification / User / Project / Learn modules | Not wired |
| REST APIs | None added |
| Frontend | No changes |
| C3 (preferences) | Not started |
| Diagnostics / AI Assistant | Not started |

---

## Configuration

```yaml
spring:
  thymeleaf:
    enabled: false   # Communication engines are manual beans

app:
  communication:
    frontend-base-url: http://localhost:5173
    support-email: support@learninghub.local
```

---

## Tests

| Suite | Result |
|-------|--------|
| `EmailTemplateRendererTest` | 18 passed |
| `EmailTemplateVariablesTest` | 4 passed |
| `EmailChannelHandlerTest` | 4 passed |
| `PasswordResetServiceTest` | Passed (regression) |
| Full `*Communication*` suite | Passed |

---

## Manual QA recommendations

1. Trigger a communication outbox email in a dev environment (when C1+C2 are merged and a module is wired in a future release) and verify HTML renders in a mail client.
2. Confirm `POST /api/v1/auth/forgot-password` still sends the legacy forgot-password template.
3. Spot-check HTML in Gmail/Outlook for responsive layout and CTA button styling.
4. Verify `spring.thymeleaf.enabled: false` does not affect any existing MVC views.

---

## Files changed (high level)

- `backend/pom.xml` — Thymeleaf dependency
- `backend/src/main/resources/application.yml` — disable auto Thymeleaf
- `backend/src/main/java/.../communication/template/*` — new engine classes
- `backend/src/main/java/.../communication/channel/EmailChannelHandler.java` — renderer integration
- `backend/src/main/resources/templates/communication/**` — layouts, fragments, catalog
- `backend/src/test/java/.../communication/template/*` — new tests
- `docs/communication/template-catalog.md` — updated
- `docs/releases/release-communication-c2-template-engine.md` — this report

---

## Merge order

Merge C1 (`cursor/communication-framework-c1-91e0`) before C2, or merge C2 into C1 branch first for stacked review.
