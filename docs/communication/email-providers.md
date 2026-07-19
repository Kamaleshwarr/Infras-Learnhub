# Email Providers

**Status:** Implemented (C4)  
**Companion:** [architecture-review.md](./architecture-review.md), [delivery-model.md](./delivery-model.md)

---

## Architecture

Business modules never reference email providers directly. Delivery flows through the Communication Framework:

```text
CommunicationService
        ↓
CommunicationDispatcher
        ↓
EmailChannelHandler
        ↓
EmailProvider (interface)
        ├─ LogEmailProvider      (development)
        ├─ ResendEmailProvider   (production)
        └─ SmtpEmailProvider     (legacy optional)
```

Provider selection is configuration-driven via `EmailProviderConfiguration`.

---

## Supported providers

| Provider | Config value | Use case |
|----------|--------------|----------|
| Log | `log` (default) | Local development — logs email content, no external account required |
| Resend | `resend` | Production transactional email via [Resend](https://resend.com) REST API |
| SMTP | `smtp` | Legacy JavaMail SMTP integration (optional) |

---

## Configuration

```yaml
app:
  communication:
    email:
      provider: log   # log | resend | smtp
      from: noreply@learninghub.local
      resend:
        api-key: ${APP_COMMUNICATION_EMAIL_RESEND_API_KEY:}
        base-url: ${APP_COMMUNICATION_EMAIL_RESEND_BASE_URL:https://api.resend.com}
        connect-timeout: ${APP_COMMUNICATION_EMAIL_RESEND_CONNECT_TIMEOUT:PT10S}
        read-timeout: ${APP_COMMUNICATION_EMAIL_RESEND_READ_TIMEOUT:PT30S}
```

### Environment variables

| Variable | Purpose |
|----------|---------|
| `APP_COMMUNICATION_EMAIL_PROVIDER` | `log`, `resend`, or `smtp` |
| `APP_COMMUNICATION_EMAIL_RESEND_API_KEY` | Resend API key (`re_...`) — never commit to source control |
| `APP_COMMUNICATION_EMAIL_RESEND_BASE_URL` | Resend API base URL (default `https://api.resend.com`) |
| `APP_COMMUNICATION_EMAIL_RESEND_CONNECT_TIMEOUT` | HTTP connect timeout (ISO-8601 duration) |
| `APP_COMMUNICATION_EMAIL_RESEND_READ_TIMEOUT` | HTTP read timeout (ISO-8601 duration) |
| `APP_MAIL_FROM` | Default From address for communication emails |

---

## Local development (LOG)

Default configuration requires no Resend account:

```bash
APP_COMMUNICATION_EMAIL_PROVIDER=log mvn -f backend spring-boot:run
```

Emails are written to application logs by `LogEmailProvider`.

---

## Production setup (RESEND)

### 1. Create a free Resend account

1. Sign up at [https://resend.com](https://resend.com)
2. Create an API key in the dashboard (starts with `re_`)
3. Verify a sender domain or use the Resend sandbox sender for testing

### 2. Verify sender

- Add your sending domain in Resend → Domains
- Complete DNS verification (SPF, DKIM)
- Set `APP_MAIL_FROM` to an address on the verified domain (e.g. `noreply@yourdomain.com`)

### 3. Switch provider

```bash
export APP_COMMUNICATION_EMAIL_PROVIDER=resend
export APP_COMMUNICATION_EMAIL_RESEND_API_KEY=re_your_api_key
export APP_MAIL_FROM=noreply@yourdomain.com
```

Restart the backend. No code changes required.

---

## Error handling

`ResendEmailProvider` never throws from `send()`. All outcomes return `EmailDeliveryResult`:

| Outcome | Behavior |
|---------|----------|
| HTTP 2xx + message id | `success=true` |
| HTTP 4xx/5xx | `success=false`, error message from Resend response |
| Timeout | `success=false`, retry-friendly message |
| Missing API key | `success=false`, configuration error |

The existing outbox processor retries failed deliveries using configured backoff. Business transactions are unaffected.

---

## Health check

`EmailProvider.isHealthy()`:

| Provider | Healthy when |
|----------|--------------|
| Log | Always |
| Resend | API key is configured |
| SMTP | `JavaMailSender` bean is available |

---

## Testing

| Test class | Coverage |
|------------|----------|
| `ResendEmailProviderTest` | Success, API error, timeout, missing API key |
| `EmailProviderConfigurationTest` | LOG / RESEND / SMTP selection |
| `CommunicationPropertiesResendBindingTest` | Configuration binding |
| `LogEmailProviderTest` | Log mode unchanged |
| `EmailChannelHandlerTest` | Outbox retry on provider failure |

---

## Out of scope (C4)

- Email analytics
- Bounce handling
- Webhook processing
- Unsubscribe management
- Admin diagnostics (C5)
