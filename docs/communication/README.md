# Communication Framework

**Status:** C1–C5 shipped — C6 diagnostics not started  
**Version 1 priority:** #1 — Communication Framework (Email + existing In-App Notifications)  
**Last updated:** 2026-07-28

## C1 delivered (approved refinements)

| Item | C1 scope |
|------|----------|
| `communication_outbox` (V20) | Yes — async email queue |
| `communication_delivery_log` | **Deferred** — no audit table until operational need |
| Password reset migration | **Shipped in C5** — `PASSWORD_RESET_REQUESTED` via Communication Framework |
| `CommunicationPriority` | HIGH / NORMAL / LOW — outbox poll ordering only in V1 |
| Module integrations | **Shipped in C3** — certifications use Communication Framework |
| Email providers | **C4** — `LogEmailProvider` (default) + `ResendEmailProvider` (production) |

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
| [email-providers.md](./email-providers.md) | LOG / Resend provider architecture, configuration, setup guide |

## Relationship to existing infrastructure

| Existing artifact | Role in framework |
|-------------------|-------------------|
| `com.company.learninghub.notification` | In-app channel; inbox APIs remain |
| `auth.PasswordResetCommunicationPublisher` | Publishes `PASSWORD_RESET_REQUESTED` for forgot-password |
| `notifications` table (V9) | In-app delivery persistence — unchanged contract for clients |

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
| C5 | Password Reset Migration | Forgot-password via `PASSWORD_RESET_REQUESTED` + outbox |
| C6 | Preferences (minimal) | Global email enable + category flags if approved |
| C7 | Diagnostics | Admin health, preview/test send |

Do **not** start implementation until [architecture-review.md](./architecture-review.md) is manually approved.

## Related documentation

- In-app notification release: `docs/releases/notification-infrastructure-final-summary.md`
- Password/email (v0.2): `.cursor/architecture.md` — Password Management / Email Flow
- Project future hooks: `docs/project/12-future-compatibility.md`
- Mandatory workflow: `docs/development-workflow.md`
