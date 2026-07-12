# Communication Framework

**Status:** C1 infrastructure **shipped** — C2/C3 not started  
**Version 1 priority:** #1 — Communication Framework (Email + existing In-App Notifications)  
**Last updated:** 2026-07-12

## C1 delivered (approved refinements)

| Item | C1 scope |
|------|----------|
| `communication_outbox` (V20) | Yes — async email queue |
| `communication_delivery_log` | **Deferred** — no audit table until operational need |
| Password reset migration | **Deferred** — `auth.EmailService` unchanged |
| `CommunicationPriority` | HIGH / NORMAL / LOW — outbox poll ordering only in V1 |
| Module integrations | **Deferred to C3** — certifications still use `NotificationService` directly |

## Purpose

Unify **in-app notifications** and **email** under a single Communication Framework that domain modules invoke through events — without duplicating delivery pipelines.

Version 1 implements:

- Existing in-app notification inbox (extend, do not replace)
- Transactional and operational email delivery

Version 2+ may add Slack, Microsoft Teams, SMS, push, and WhatsApp without changing business modules.

## Documents

| Document | Contents |
|----------|----------|
| [architecture-review.md](./architecture-review.md) | Full audit, options comparison, recommendation, risks, open decisions |
| [communication-events.md](./communication-events.md) | Event catalog, ownership, payloads, channel mapping |
| [template-catalog.md](./template-catalog.md) | Master layout, template inventory, variable conventions |
| [delivery-model.md](./delivery-model.md) | Sync vs queue, retry, idempotency, audit, performance |

## Relationship to existing infrastructure

| Existing artifact | Role in framework |
|-------------------|-------------------|
| `com.company.learninghub.notification` | In-app channel; inbox APIs remain |
| `auth.EmailService` | Seed for email channel; refactor into provider + template layer |
| `notifications` table (V9) | In-app delivery persistence — unchanged contract for clients |
| `templates/email/forgot-password.*` | First email templates; migrate to shared layout |

## Version 1 scope boundary

**In scope (after approval):**

- Communication orchestration layer
- Email templates for completed modules (certifications, account lifecycle, selected learn/project events)
- Delivery audit log
- Admin diagnostics (minimal)

**Out of scope (Version 2):**

- User preference UI (optional minimal backend flags only in late C-phase)
- Digest emails
- Slack / Teams / SMS / push
- Career Paths, Learn v2, advanced gamification emails tied to deferred features

## Implementation phases (proposed)

| Phase | Name | Summary |
|-------|------|---------|
| C1 | Communication Infrastructure | Events, dispatcher, channels, audit, provider abstraction |
| C2 | Email Templates | Master layout, HTML + plain text, template catalog |
| C3 | Module Integrations | Wire completed modules to communication events |
| C4 | Preferences (minimal) | Global email enable + category flags if approved |
| C5 | Diagnostics | Admin health, preview/test send |

Do **not** start implementation until [architecture-review.md](./architecture-review.md) is manually approved.

## Related documentation

- In-app notification release: `docs/releases/notification-infrastructure-final-summary.md`
- Password/email (v0.2): `.cursor/architecture.md` — Password Management / Email Flow
- Project future hooks: `docs/project/12-future-compatibility.md`
- Mandatory workflow: `docs/development-workflow.md`
