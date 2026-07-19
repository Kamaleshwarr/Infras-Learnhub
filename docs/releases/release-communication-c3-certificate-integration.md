# Release Report: Communication C3 — Certificate Module Integration

**Release:** Communication Framework C3  
**Date:** 2026-07-19  
**Branch:** `cursor/communication-c3-certificate-integration-91e0`

---

## Summary

Migrated the Certificate Submission workflow to the Communication Framework. `CertificateSubmissionService` now publishes `CERTIFICATE_SUBMITTED`, `CERTIFICATE_APPROVED`, and `CERTIFICATE_REJECTED` events through `CommunicationService`, delivering in-app notifications synchronously and queueing emails via the outbox for C2 template rendering.

---

## What was delivered

### Integration layer

- `CertificateCommunicationPublisher` — builds `CommunicationEvent` payloads and calls `CommunicationService.publish()`
- `CertificateSubmissionService` — replaced direct `NotificationService` calls with publisher methods

### Event flow

```text
CertificateSubmissionService (submit / approve / reject)
        ↓
CertificateCommunicationPublisher
        ↓
CommunicationService.publish()
        ↓
CommunicationDispatcher
        ├─► InAppChannelHandler → notifications table (unchanged UI)
        └─► EmailChannelHandler → communication_outbox → C2 templates
```

### Events implemented

| Event | Recipient | Channels | Priority |
|-------|-----------|----------|----------|
| `CERTIFICATE_SUBMITTED` | Each active admin | IN_APP + EMAIL | HIGH |
| `CERTIFICATE_APPROVED` | Submitting employee | IN_APP + EMAIL | NORMAL |
| `CERTIFICATE_REJECTED` | Submitting employee | IN_APP + EMAIL | NORMAL |

In-app copy matches the previous `NotificationFactory` messages. Email uses `certificate-submitted`, `certificate-approved`, and `certificate-rejected` templates.

---

## What was NOT changed

| Area | Status |
|------|--------|
| Frontend notification UI | Unchanged |
| Password reset (`auth.EmailService`) | Unchanged |
| User Management notifications | Unchanged |
| Project module | Unchanged |
| Learn module | Unchanged |
| C4 preferences / diagnostics | Not started |

`NotificationService.notifyCertificate*` methods remain for backward compatibility but are no longer called by the certificate workflow.

---

## Transaction behavior

Communication publishes run in the same business transaction as submit/approve/reject:

- In-app notifications persist with the business write (same as before)
- Email is queued to `communication_outbox` (SMTP send remains async via poller)
- Business operations do not depend on SMTP delivery success

---

## Tests added/updated

| Test | Coverage |
|------|----------|
| `CertificateCommunicationPublisherTest` | Event payloads, admin fan-out, rejection reason |
| `CertificateSubmissionServiceTest` | Publisher invocation on submit/approve/reject |
| `CertificateSubmissionMethodSecurityTest` | Updated wiring |
| `CertificateCommunicationIntegrationTest` | End-to-end with Testcontainers (when Docker available) |

---

## Files changed

- `submission/communication/CertificateCommunicationPublisher.java` (new)
- `submission/service/CertificateSubmissionService.java`
- `notification/service/NotificationService.java` (javadoc only)
- Test classes listed above
- `docs/communication/communication-events.md`
- `docs/releases/release-communication-c3-certificate-integration.md`

---

## Merge notes

Requires C1 (communication infrastructure) and C2 (email template engine) on `main`.
