# Communication Template Catalog

**Status:** Design specification — not implemented  
**Companion:** [architecture-review.md](./architecture-review.md)

---

## Design goals

1. **Single master layout** for all HTML emails (branding consistency)
2. **Paired HTML + plain text** for every template (accessibility + deliverability)
3. **No duplication** — content blocks composed into layout
4. **Future localization** — externalized strings; keys not inline prose in Java
5. **Dynamic variables** — typed, validated before render

---

## Template engine recommendation

| Approach | V1 recommendation |
|----------|-------------------|
| Current `{{var}}` replace | Used only for forgot-password today |
| **Thymeleaf** (classpath templates) | **Adopt in C2** — layout fragments, conditionals, i18n-ready |
| Stored DB templates | Defer — catalog in repo is sufficient for V1 |

Migrate `templates/email/forgot-password.*` into Thymeleaf structure without changing user-visible copy.

---

## Directory structure (proposed)

```text
backend/src/main/resources/templates/communication/
├── layout/
│   ├── master.html              # HTML shell
│   └── master.txt               # Plain text shell
├── fragments/
│   ├── header.html
│   ├── footer.html
│   ├── cta-button.html
│   └── support-section.html
├── email/
│   ├── password-reset.html
│   ├── password-reset.txt
│   ├── password-reset-by-admin.html
│   ├── password-reset-by-admin.txt
│   ├── welcome-user.html
│   ├── welcome-user.txt
│   ├── account-activated.html
│   ├── account-activated.txt
│   ├── account-deactivated.html
│   ├── account-deactivated.txt
│   ├── certificate-submitted-admin.html
│   ├── certificate-submitted-admin.txt
│   ├── certificate-approved.html
│   ├── certificate-approved.txt
│   ├── certificate-rejected.html
│   ├── certificate-rejected.txt
│   ├── roadmap-completed.html      # optional V1
│   ├── roadmap-completed.txt
│   ├── project-member-added.html
│   └── project-member-added.txt
└── in-app/                         # optional JSON copy maps
    └── messages.properties
```

---

## Master layout (HTML)

### Structure

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title th:text="${subject}">Engineering Learning Hub</title>
  <style>/* inline-safe styles — see brand tokens */</style>
</head>
<body>
  <table role="presentation" class="container">
    <tr><td th:replace="~{fragments/header :: header}"></td></tr>
    <tr><td class="content" th:utext="${content}">...</td></tr>
    <tr><td th:if="${ctaUrl}" th:replace="~{fragments/cta-button :: cta}"></td></tr>
    <tr><td th:replace="~{fragments/support-section :: support}"></td></tr>
    <tr><td th:replace="~{fragments/footer :: footer}"></td></tr>
  </table>
</body>
</html>
```

### Brand tokens (align with `appTheme.ts`)

| Token | Value |
|-------|-------|
| Primary color | `#1f5eff` |
| Secondary | `#6d28d9` |
| Background | `#f6f8fb` |
| Paper | `#ffffff` |
| Font | Inter, Roboto, Arial, sans-serif |
| Border radius | 12px |

### Header fragment

- Engineering Learning Hub wordmark / logo (hosted static asset or inline SVG)
- Optional subtitle: "Internal enablement"

### Footer fragment

- Company name
- "This is an automated message from Engineering Learning Hub."
- Link to platform base URL (`app.frontend.base-url` config)

### CTA button fragment

- Primary button style
- Variables: `ctaLabel`, `ctaUrl`
- Fallback plain link in text template

### Support section fragment

- Variables: `supportEmail` (default from config)
- Copy: "Need help? Contact your administrator or …"

---

## Plain text master

```text
Engineering Learning Hub
========================

${content}

${ctaLabel}: ${ctaUrl}

---
${supportLine}
This is an automated message. Please do not reply to this email.
```

---

## Template inventory (Version 1)

| Template key | Subject line | Primary CTA |
|--------------|--------------|-------------|
| `password-reset` | Reset your Learning Hub password | Reset password |
| `password-reset-by-admin` | Your password was reset | Sign in |
| `welcome-user` | Welcome to Engineering Learning Hub | Sign in |
| `account-activated` | Your account has been activated | Open Learning Hub |
| `account-deactivated` | Your account has been deactivated | — |
| `certificate-submitted-admin` | New certificate submission | Review submission |
| `certificate-approved` | Certificate approved | View certifications |
| `certificate-rejected` | Certificate requires attention | View submissions |
| `roadmap-completed` | You completed a learning roadmap | View progress |
| `project-member-added` | You were added to a project | View project |

---

## Variable catalog

### Global variables (all templates)

| Variable | Source |
|----------|--------|
| `fullName` | `User.fullName` |
| `recipientEmail` | `User.email` |
| `platformName` | Config constant |
| `platformUrl` | `app.frontend.base-url` |
| `supportEmail` | `app.communication.support-email` |
| `year` | Current UTC year |

### Per-template variables

| Template | Additional variables |
|----------|---------------------|
| `password-reset` | `resetUrl`, `expirationMinutes` |
| `password-reset-by-admin` | `loginUrl` |
| `welcome-user` | `loginUrl`, `mustChangePassword` |
| `certificate-submitted-admin` | `employeeName`, `initiativeTitle`, `reviewUrl`, `submittedAt` |
| `certificate-approved` | `initiativeTitle`, `approvedAt` |
| `certificate-rejected` | `initiativeTitle`, `rejectionReason` |
| `roadmap-completed` | `technologyName`, `roadmapUrl` |
| `project-member-added` | `projectName`, `projectRole`, `projectUrl` |

---

## URL construction

- Base URL from `app.frontend.base-url` (new config; today only reset URL exists)
- Paths from `CommunicationEntityRef.actionPath`
- **Never** embed JWT or reset token except in `password-reset` template

```text
{baseUrl}{actionPath}
```

Example: `http://localhost:5173/submissions/review`

---

## In-app copy alignment

`NotificationFactory` strings should be sourced from the same `messages.properties` keys as email summaries where possible:

```properties
notification.certificate-approved.title=Certificate approved
notification.certificate-approved.message=Your certificate submission for "{initiativeTitle}" was approved.
email.certificate-approved.subject=Certificate approved
```

Prevents drift between bell inbox and email subject/body.

---

## Attachments

**Version 1:** No attachments.

**Future:** Certificate PDF as attachment on approval — defer (employee download deferred from v0.6.2).

---

## Localization (future)

- Thymeleaf `#{key}` message bundles
- `templates/communication/messages_en.properties`
- User `locale` column deferred to V2

---

## Admin preview (C5)

`POST /admin/communication/preview` accepts:

```json
{
  "templateKey": "certificate-approved",
  "format": "html",
  "variables": { "fullName": "Sample User", "initiativeTitle": "AWS Cert" }
}
```

Returns rendered HTML or text without sending.

---

## Testing requirements

| Test | Assert |
|------|--------|
| Each template renders | No unsubstituted `${` or `{{` |
| HTML + text pairs exist | Catalog completeness |
| Master layout | Header/footer on all HTML |
| CTA URLs | Valid https?:// in smtp mode |
| Long initiative titles | No layout break (smoke) |
| Missing optional var | `rejectionReason` empty → graceful copy |
