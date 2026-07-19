# Communication Events Catalog

**Status:** Implemented for certificate workflow (C3); other events remain planned  
**Companion:** [architecture-review.md](./architecture-review.md)

Events are immutable records published by domain services **after** successful business transactions. The Communication Framework maps each event to one or more channels.

---

## Certificate event flow (C3 — implemented)

```text
CertificateSubmissionService
  submit()   → CertificateCommunicationPublisher.publishSubmitted()
  approve()  → CertificateCommunicationPublisher.publishApproved()
  reject()   → CertificateCommunicationPublisher.publishRejected()
        ↓
CommunicationService.publish(CommunicationEvent)
        ↓
CommunicationDispatcher
  ├─ IN_APP  → InAppChannelHandler → notifications (existing UI)
  └─ EMAIL   → EmailChannelHandler → communication_outbox → EmailTemplateRenderer
```

### Template mapping (certificate)

| Event | Email template | In-app title |
|-------|----------------|--------------|
| `CERTIFICATE_SUBMITTED` | `certificate-submitted` | New certificate submission |
| `CERTIFICATE_APPROVED` | `certificate-approved` | Certificate approved |
| `CERTIFICATE_REJECTED` | `certificate-rejected` | Certificate rejected |

### Idempotency key (implemented)

```text
{eventType}:{submissionId}:{recipientUserId}
```

Email outbox appends `:EMAIL` in `EmailChannelHandler`.

---

## Event record shape

```java
public record CommunicationEvent(
    UUID eventId,                    // unique per publish attempt
    CommunicationEventType type,
    Instant occurredAt,
    UUID actorUserId,                // nullable (system)
    UUID recipientUserId,            // primary recipient
    CommunicationEntityRef entity,   // optional typed reference
    Map<String, String> variables,   // template + in-app copy
    Set<CommunicationChannel> channels,
    CommunicationPriority priority,
    String idempotencyKey            // dedupe key
) {}
```

```java
public record CommunicationEntityRef(
    CommunicationEntityType entityType,
    UUID entityId,
    String actionPath                // deep link, e.g. /submissions/review
) {}
```

---

## Idempotency key convention

```text
{eventType}:{entityId}:{recipientUserId}:{channel}
```

For admin fan-out (certificate submitted):

```text
CERTIFICATE_SUBMITTED:{submissionId}:{adminUserId}:EMAIL
```

Duplicate publish with same key → delivery log returns existing result; no second send.

---

## Event ownership matrix

| Event type | Owner module | Trigger point |
|------------|--------------|---------------|
| `PASSWORD_RESET_REQUESTED` | auth | `PasswordResetService.issueResetToken` |
| `PASSWORD_CHANGED` | auth | `PasswordService.changePassword` (optional V1) |
| `PASSWORD_RESET_BY_ADMIN` | user | `UserManagementService.resetPassword` |
| `ACCOUNT_CREATED` | user | `UserManagementService.createUser` / import row |
| `ACCOUNT_ACTIVATED` | user | `UserManagementService.activate` |
| `ACCOUNT_DEACTIVATED` | user | `UserManagementService.deactivate` |
| `CERTIFICATE_SUBMITTED` | submission | `CertificateSubmissionService.submit` → `CertificateCommunicationPublisher` |
| `CERTIFICATE_APPROVED` | submission | `CertificateSubmissionService.approve` → `CertificateCommunicationPublisher` |
| `CERTIFICATE_REJECTED` | submission | `CertificateSubmissionService.reject` → `CertificateCommunicationPublisher` |
| `LEARNING_STAGE_COMPLETED` | learn | `LearningProgressService.completeStage` |
| `LEARNING_ROADMAP_COMPLETED` | learn | `LearningProgressService` on final stage |
| `CONTINUE_LEARNING_REMINDER` | learn | Scheduled job (deferred C3b) |
| `PROJECT_MEMBER_ADDED` | projectknowledge | `ProjectTeamService` add member |
| `PROJECT_REPOSITORY_ADDED` | projectknowledge | `ProjectLinkedRepositoryService` create |
| `PROJECT_ENVIRONMENT_ADDED` | projectknowledge | `ProjectEnvironmentService` create |
| `LEADERBOARD_MILESTONE` | leaderboard | Deferred V2 |
| `INITIATIVE_*` | initiative | Deferred — no assignment/due model |
| `AI_RECOMMENDATION` | future | Version 2 |

---

## Version 1 — event specifications

### Authentication & account

#### `PASSWORD_RESET_REQUESTED`

| Field | Value |
|-------|-------|
| Channels | EMAIL only |
| Recipient | Requesting user |
| Template | `password-reset` (migrate from forgot-password) |
| Variables | `fullName`, `resetUrl`, `expirationMinutes` |
| Priority | IMMEDIATE |
| Notes | Replace direct `EmailService` call; security email — ignore preferences |

#### `PASSWORD_RESET_BY_ADMIN`

| Field | Value |
|-------|-------|
| Channels | EMAIL + IN_APP |
| Recipient | Target user |
| Template | `password-reset-by-admin` |
| Variables | `fullName`, `loginUrl`, `mustChangePassword` |
| Idempotency | Per reset action UUID |

#### `ACCOUNT_CREATED`

| Field | Value |
|-------|-------|
| Channels | EMAIL (+ IN_APP optional) |
| Recipient | New user |
| Template | `welcome-user` |
| Variables | `fullName`, `email`, `loginUrl`, `temporaryPasswordHint` (import only) |

#### `ACCOUNT_ACTIVATED` / `ACCOUNT_DEACTIVATED`

| Field | Value |
|-------|-------|
| Channels | EMAIL + IN_APP |
| Templates | `account-activated`, `account-deactivated` |
| Variables | `fullName`, `loginUrl`, `supportEmail` |

---

### Certifications

#### `CERTIFICATE_SUBMITTED`

| Field | Value |
|-------|-------|
| Channels | IN_APP + EMAIL |
| Recipients | **Each active ADMIN** (fan-out) |
| Entity | `CERTIFICATE_SUBMISSION`, submission id |
| actionPath | `/submissions/review` |
| Template | `certificate-submitted` |
| Variables | `title`, `message`, `actorName`, `initiativeTitle`, `certificationName` |
| Replaces | `NotificationService.notifyCertificateSubmitted` (C3) |

#### `CERTIFICATE_APPROVED`

| Field | Value |
|-------|-------|
| Channels | IN_APP + EMAIL |
| Recipient | Submitting employee |
| actionPath | `/submissions` |
| Template | `certificate-approved` |
| Variables | `fullName`, `initiativeTitle`, `approvedAt` |

#### `CERTIFICATE_REJECTED`

| Field | Value |
|-------|-------|
| Channels | IN_APP + EMAIL |
| Recipient | Submitting employee |
| Template | `certificate-rejected` |
| Variables | `fullName`, `initiativeTitle`, `rejectionReason`, `submissionsUrl` |

---

### Learn (Version 1 — optional subset)

#### `LEARNING_STAGE_COMPLETED`

| Field | Value |
|-------|-------|
| Channels | IN_APP optional; EMAIL **defer by default** (OD-C05) |
| Recipient | Enrolled employee |
| Entity | enrollment + stage |
| Variables | `technologyName`, `stageTitle`, `progressPercent`, `roadmapUrl` |

#### `LEARNING_ROADMAP_COMPLETED`

| Field | Value |
|-------|-------|
| Channels | IN_APP + EMAIL (recommended if learn emails in V1) |
| Template | `roadmap-completed` |

#### `CONTINUE_LEARNING_REMINDER`

| Field | Value |
|-------|-------|
| Channels | EMAIL |
| Trigger | Scheduler — inactive enrollment > N days |
| Status | **Defer to C3b** (requires new scheduled job) |

---

### Projects

#### `PROJECT_MEMBER_ADDED`

| Field | Value |
|-------|-------|
| Channels | EMAIL + IN_APP |
| Recipient | Added user |
| Template | `project-member-added` |
| Variables | `fullName`, `projectName`, `projectRole`, `functionalRole`, `projectUrl` |

#### `PROJECT_REPOSITORY_ADDED` / `PROJECT_ENVIRONMENT_ADDED`

| Field | Value |
|-------|-------|
| Channels | EMAIL optional; IN_APP optional |
| Recipients | Project OWNERs + CONTRIBUTORs (configurable) |
| Variables | `projectName`, `itemName`, `itemType`, `projectUrl` |
| Notes | Lower priority than cert/account — may defer to C3.2 |

---

## Version 2 — deferred events

| Event | Reason deferred |
|-------|-----------------|
| `INITIATIVE_ASSIGNED` | No assignment entity |
| `INITIATIVE_DUE` | No due-date notification model |
| `INITIATIVE_COMPLETED` | No completion workflow |
| `LEADERBOARD_MILESTONE` | L2 scoring not implemented |
| `AI_RECOMMENDATION` | AI Assistant not started |
| `DIGEST_*` | Requires preference + scheduler infrastructure |
| `PROJECT_RESOURCE_STALE` | P5 governance not shipped |

---

## Channel mapping summary (V1 target)

| Event | IN_APP | EMAIL |
|-------|--------|-------|
| PASSWORD_RESET_REQUESTED | — | ✓ |
| PASSWORD_RESET_BY_ADMIN | ✓ | ✓ |
| ACCOUNT_CREATED | ○ | ✓ |
| ACCOUNT_ACTIVATED | ✓ | ✓ |
| ACCOUNT_DEACTIVATED | ✓ | ✓ |
| CERTIFICATE_SUBMITTED | ✓ | ✓ |
| CERTIFICATE_APPROVED | ✓ | ✓ |
| CERTIFICATE_REJECTED | ✓ | ✓ |
| LEARNING_ROADMAP_COMPLETED | ○ | ○ |
| PROJECT_MEMBER_ADDED | ○ | ✓ |
| CONTINUE_LEARNING_REMINDER | — | defer |

○ = optional / phase-dependent

---

## Payload guidelines

**Include in events/templates:**

- Recipient `fullName`, `email`
- Human-readable entity names (initiative title, project name)
- Deep link paths (not full URLs with secrets)
- Actor display name when relevant

**Exclude:**

- Passwords, reset tokens, JWTs
- Certificate file content
- Credential URLs with embedded auth
- Full rejection of internal UUIDs in user-facing copy (use names)

---

## Enum evolution

Add `CommunicationEventType` in framework package. Extend `NotificationType` only when in-app copy differs — prefer mapping:

```text
CommunicationEventType.CERTIFICATE_APPROVED → NotificationType.CERTIFICATE_APPROVED
```

Keeps inbox filter compatibility with existing frontend types.
