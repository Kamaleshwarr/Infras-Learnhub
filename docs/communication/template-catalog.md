# Communication Template Catalog

**Status:** Implemented (C2)  
**Companion:** [architecture-review.md](./architecture-review.md)

---

## Rendering architecture

The email template engine lives in `com.company.learninghub.communication.template` and renders Thymeleaf templates without sending email. `EmailChannelHandler` uses `EmailTemplateRenderer.buildEmailMessage()` when processing outbox entries.

```text
CommunicationEvent + User
        │
        ▼
EmailTemplateVariables.build()
        │
        ▼
EmailTemplateModel (subject, variables, template name)
        │
        ├─► communicationHtmlTemplateEngine → HTML body
        └─► communicationTextTemplateEngine → plain-text body
        │
        ▼
RenderedEmail / EmailMessage
```

### Key classes

| Class | Responsibility |
|-------|----------------|
| `CommunicationEmailTemplate` | Maps `CommunicationEventType` → template name + default subject |
| `EmailTemplateModel` | Structured render model (recipient, actor, URLs, extras) |
| `EmailTemplateVariables` | Builds model from `User` + `CommunicationEvent` |
| `EmailTemplateRenderer` | `render()`, `renderHtml()`, `renderText()`, `renderSubject()` |
| `CommunicationEmailTemplateConfiguration` | Dedicated HTML and TEXT Thymeleaf engines |

### Preview support

`EmailTemplateRenderer` exposes render methods without side effects. No REST preview API is included in C2; future admin preview can call these methods directly.

---

## Template hierarchy

```text
backend/src/main/resources/templates/communication/
├── layout/
│   ├── email-shell.html          # HTML master layout (header, content slot, support, footer)
│   └── email-shell.txt           # Plain-text master layout
├── fragments/
│   ├── email-styles.html         # Brand CSS (inline-safe)
│   ├── header.html               # Logo + tagline
│   ├── footer.html               # Automated message + copyright
│   ├── support-section.html      # Support contact block
│   ├── cta-button.html           # Primary action button
│   ├── email-body.html           # Shared HTML body content
│   └── email-body.txt            # Shared plain-text body content
└── email/
    ├── certificate-submitted.{html,txt}
    ├── certificate-approved.{html,txt}
    ├── certificate-rejected.{html,txt}
    ├── account-created.{html,txt}
    ├── account-activated.{html,txt}
    ├── account-deactivated.{html,txt}
    ├── password-reset.{html,txt}
    ├── password-reset-by-admin.{html,txt}
    ├── project-member-added.{html,txt}
    ├── project-repository-added.{html,txt}
    ├── project-environment-added.{html,txt}
    └── generic-notification.{html,txt}
```

### HTML composition

Each catalog template delegates to the shared shell and body fragments:

```html
<html th:replace="~{layout/email-shell :: shell(~{::main})}">
  <th:block th:fragment="main" th:replace="~{fragments/email-body :: body}"/>
</html>
```

### Plain-text composition

TEXT mode requires whole-template inclusion (no fragment selectors). Each `email/*.txt` replaces the text shell, which inserts `fragments/email-body` as a full template.

---

## Variable model

`EmailTemplateVariables` centralizes variable mapping. Templates receive both the full `model` object and individual context variables.

### Core variables

| Variable | Source |
|----------|--------|
| `recipientName` | `User.fullName` |
| `recipientEmail` | `User.email` |
| `actorName` | Event `variables.actorName` |
| `message` | Event `variables.message` / `emailBody` or template default |
| `subject` | Event `variables.emailSubject` or template default |
| `actionUrl` | Event `variables.actionUrl` / `resetUrl` or `{frontendBaseUrl}{actionPath}` |
| `actionLabel` | Event `variables.actionLabel` or template default |
| `applicationUrl` | `app.communication.frontend-base-url` |
| `supportEmail` | `app.communication.support-email` |
| `currentYear` | Current UTC year |
| `projectName` | Event `variables.projectName` |
| `certificationName` | Event `variables.certificationName` or `initiativeTitle` |
| `technologyName` | Event `variables.technologyName` |
| `resetUrl` | Event `variables.resetUrl` |
| `expirationMinutes` | Event `variables.expirationMinutes` |
| `priority` | `CommunicationEvent.priority()` |
| `extraVariables` | Remaining event variables (e.g. `rejectionReason`) |

### URL construction

- Base URL from `app.communication.frontend-base-url`
- Relative paths from `CommunicationEntityRef.actionPath`
- Absolute URLs in event variables are passed through unchanged

---

## Branding rules

Aligned with `frontend/src/theme/appTheme.ts`:

| Token | Value |
|-------|-------|
| Primary color | `#1f5eff` |
| Secondary | `#6d28d9` |
| Background | `#f6f8fb` |
| Paper | `#ffffff` |
| Font | Inter, Roboto, Arial, sans-serif |
| Border radius | 12px |

### Header

- Wordmark: **Engineering Learning Hub**
- Tagline: *Internal enablement*
- Gradient header using primary → secondary

### Footer (required on all templates)

- "This is an automated message from Engineering Learning Hub."
- Copyright with dynamic `currentYear`

### CTA button

- Primary `#1f5eff` button when `actionUrl` is present
- Plain-text fallback: `{actionLabel}: {actionUrl}`

---

## Naming conventions

| Pattern | Example |
|---------|---------|
| Template file | `{domain}-{action}.html` / `.txt` |
| Enum constant | `CERTIFICATE_APPROVED` → `certificate-approved` |
| Fragment | `email-{purpose}` in `fragments/` |
| Layout | `email-shell` in `layout/` |
| Event override keys | `emailSubject`, `emailBody`, `actionUrl`, `actionLabel` |

---

## Template inventory (C2)

| Template key | Event type(s) | Default subject |
|--------------|---------------|-----------------|
| `certificate-submitted` | `CERTIFICATE_SUBMITTED` | New certificate submission |
| `certificate-approved` | `CERTIFICATE_APPROVED` | Certificate approved |
| `certificate-rejected` | `CERTIFICATE_REJECTED` | Certificate requires attention |
| `account-created` | `ACCOUNT_CREATED` | Welcome to Engineering Learning Hub |
| `account-activated` | `ACCOUNT_ACTIVATED` | Your account has been activated |
| `account-deactivated` | `ACCOUNT_DEACTIVATED` | Your account has been deactivated |
| `password-reset` | `PASSWORD_RESET_REQUESTED` | Reset your Learning Hub password |
| `password-reset-by-admin` | `PASSWORD_RESET_BY_ADMIN` | Your password was reset |
| `project-member-added` | `PROJECT_MEMBER_ADDED` | You were added to a project |
| `project-repository-added` | `PROJECT_REPOSITORY_ADDED` | Repository added to project |
| `project-environment-added` | `PROJECT_ENVIRONMENT_ADDED` | Environment added to project |
| `generic-notification` | Learning reminders / completions | Notification from Engineering Learning Hub |

---

## Thymeleaf configuration

- `spring.thymeleaf.enabled: false` — Spring Boot auto-config disabled to avoid MVC impact
- `communicationHtmlTemplateEngine` — HTML templates under `templates/communication/`
- `communicationTextTemplateEngine` — TEXT templates with `.txt` suffix
- Engines are isolated beans; no view controller registration

---

## Legacy password reset (unchanged)

Production password reset continues to use `auth.EmailService` with `templates/email/forgot-password.{html,txt}` and `{{placeholder}}` replacement. The C2 `password-reset` templates are infrastructure-only and are not wired to `PasswordResetService`.

---

## Testing

| Test class | Coverage |
|------------|----------|
| `EmailTemplateRendererTest` | All catalog templates, branding, CTA, optional variables, preview methods |
| `EmailTemplateVariablesTest` | Model building, URL construction, variable mapping |
| `EmailChannelHandlerTest` | Handler uses renderer (mocked) |
| `PasswordResetServiceTest` | Regression — legacy flow unchanged |

---

## Future work (not in C2)

- Admin preview REST API (C5)
- Localization via message bundles
- Attachments and inline images
- Migrate `auth.EmailService` to communication templates (separate task)
