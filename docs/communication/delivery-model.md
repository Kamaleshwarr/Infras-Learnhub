# Communication Delivery Model

**Status:** Design specification — not implemented  
**Companion:** [architecture-review.md](./architecture-review.md)

---

## Options compared

### Option A — Immediate synchronous send

Domain service calls email provider directly in the same HTTP transaction.

| Aspect | Assessment |
|--------|------------|
| Performance | Blocks request thread on SMTP latency |
| Reliability | User sees failure if SMTP slow/down |
| Retry | Difficult without losing request context |
| Audit | Must write log before send — rollback issues |
| Scalability | Poor under burst |

**Verdict:** Reject for operational email. Accept only for **must-deliver-in-request** if ever required (not needed in V1).

---

### Option B — Queued worker only

All communication (including in-app) goes through async queue.

| Aspect | Assessment |
|--------|------------|
| Performance | Fast API responses |
| Reliability | Strong for email |
| Retry | Natural |
| Audit | Clean |
| In-app UX | Delay before bell shows notification — **bad UX** |

**Verdict:** Reject for in-app. Accept email-only portion.

---

### Option C — Hybrid (recommended)

| Channel | Delivery | Transaction boundary |
|---------|----------|---------------------|
| **IN_APP** | Synchronous after domain write | Same `@Transactional` as business operation |
| **EMAIL** | Async via outbox poller | After commit |

```text
HTTP Request
  → Domain Service (@Transactional)
      → Business persistence
      → CommunicationService.publish(event)
          → InAppChannelHandler → notifications INSERT (sync)
          → CommunicationOutbox INSERT status=PENDING (sync)
  → Transaction commit
  → OutboxPoller (@Scheduled)
      → EmailChannelHandler → SMTP → delivery_log SENT/FAILED
      → Retry with backoff
```

**Verdict:** **Recommended for Version 1.**

---

## Outbox table (proposed)

`communication_outbox`

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `payload_json` | JSONB | Serialized CommunicationEvent |
| `channel` | VARCHAR | EMAIL |
| `status` | VARCHAR | PENDING, PROCESSING, SENT, FAILED, DEAD |
| `available_at` | TIMESTAMPTZ | For backoff |
| `retry_count` | INT | Default 0 |
| `last_error` | TEXT | |
| `created_at` | TIMESTAMPTZ | |
| `processed_at` | TIMESTAMPTZ | |

Index: `(status, available_at) WHERE status IN ('PENDING', 'FAILED')`

---

## Poller design

```java
@Scheduled(fixedDelayString = "${app.communication.outbox.poll-interval:10s}")
public void processOutbox() {
    // SELECT ... FOR UPDATE SKIP LOCKED (batch size 20)
    // mark PROCESSING → send → SENT or schedule retry
}
```

| Setting | Default |
|---------|---------|
| Poll interval | 10 seconds |
| Batch size | 20 |
| Max retries | 3 |
| Backoff | 1m, 5m, 15m |
| Dead letter | status=DEAD after max retries; alert in logs |

**First scheduled job in codebase** — document in implementation report.

---

## Password reset migration

Today: `PasswordResetService` sends email **inside** transaction before commit.

**Target:**

1. Save reset token (unchanged)
2. Publish `PASSWORD_RESET_REQUESTED` → outbox
3. Commit
4. Poller sends email

User still receives identical 202 on forgot-password. Token exists even if email delayed.

---

## Idempotency

### Publish time

`CommunicationService` checks `communication_delivery_log.idempotency_key` before enqueue/send.

### Process time

Outbox processor re-checks idempotency before SMTP send (crash between send and mark).

### Key format

See [communication-events.md](./communication-events.md).

---

## Failure handling

| Failure | Behavior |
|---------|----------|
| SMTP auth error | FAILED, no retry (config issue) — DEAD after 1 |
| SMTP timeout | Retry with backoff |
| Invalid template | DEAD immediately; log error |
| Inactive recipient | SKIPPED in delivery log |
| Null email | SKIPPED |
| User `email_notifications_enabled=false` | SKIPPED (C4) |

In-app channel failures **roll back** business transaction (same as today).

Email failures **do not** roll back business transaction.

---

## Audit integration

Every delivery attempt writes to `communication_delivery_log`:

```text
PENDING → SENT | FAILED | SKIPPED
```

Correlation ID ties multi-channel fan-out:

```text
correlation_id = event.eventId
```

Admin can query recent failures in C5 diagnostics.

---

## Performance estimates

| Metric | V1 target |
|--------|-----------|
| Daily emails | < 500 |
| Peak rate | < 20/min |
| Outbox depth (steady) | < 50 |
| Outbox depth (alert) | > 500 |
| SMTP timeout | 30s |
| Poller batch | 20 |

### Admin fan-out (certificate submitted)

10 admins = 10 outbox rows per submission. Acceptable at low volume. Optimize later with BCC **not recommended** (privacy).

---

## Duplicate prevention

| Scenario | Prevention |
|----------|------------|
| Double-click submit | Business idempotency on submission |
| Retry same event | `idempotency_key` UNIQUE |
| Poller crash after send | Provider message id logged; manual reconcile |
| Concurrent pollers | `SKIP LOCKED` row locking |

---

## Configuration (proposed)

```yaml
app:
  frontend:
    base-url: ${APP_FRONTEND_BASE_URL:http://localhost:5173}
  communication:
    enabled: true
    support-email: ${APP_SUPPORT_EMAIL:support@learninghub.local}
    email:
      provider: ${APP_MAIL_MODE:log}   # log | smtp — migrate naming
      from: ${APP_MAIL_FROM:noreply@learninghub.local}
    outbox:
      poll-interval: 10s
      batch-size: 20
      max-retries: 3
      backoff: 1m,5m,15m
```

---

## Monitoring (C5)

| Signal | Source |
|--------|--------|
| Outbox depth | `COUNT(*) WHERE status=PENDING` |
| Dead letters | `status=DEAD` last 24h |
| Email failure rate | delivery_log FAILED / total |
| Provider health | `EmailProvider.isHealthy()` — SMTP connect test |

Expose via `/api/v1/admin/communication/health` (admin only).

---

## Version 2 extensions

| Feature | Delivery impact |
|---------|-----------------|
| Digest emails | New scheduler aggregates outbox |
| Slack/Teams | New channel handler; same outbox pattern |
| Push | External provider queue |
| Priority queues | Separate outbox partition by `CommunicationPriority` |

---

## Decision summary

| Question | Answer |
|----------|--------|
| Sync or async email? | **Async outbox** |
| Sync in-app? | **Yes** |
| First scheduler? | Outbox poller |
| Retry? | **Yes**, 3× exponential |
| Block HTTP on SMTP? | **No** |
