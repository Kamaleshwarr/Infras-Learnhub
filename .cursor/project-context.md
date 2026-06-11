# Learning Hub Project Context

## Project Identity

- **Project Name:** Learning Hub
- **Repository:** https://github.com/Kamaleshwarr/Infras-Learnhub.git
- **Purpose:** Enterprise internal learning platform for initiatives, certification submissions, study materials, project knowledge, leaderboards, and administration workflows.

## Technology Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- PostgreSQL
- Flyway
- Spring Data JPA
- Docker / Docker Compose
- Swagger / OpenAPI

### Frontend

- React
- TypeScript
- Material UI
- React Router
- Axios
- Context API
- Vitest / Testing Library for frontend tests

## Roles

- `ADMIN`
- `EMPLOYEE`

## Completed Backend Modules

1. Authentication
2. Learning Initiatives
3. Certificate Submissions
4. Leaderboards
5. Study Materials Repository
6. Project Knowledge Repository
7. User Management
8. Password Management

## Completed Frontend Modules

1. Frontend foundation
2. Authentication UI
3. Role-aware dashboard foundation
4. Password Management UI

## Password Management Features

- Change Password
- First Login Password Change Enforcement
- Forgot Password (Email Reset)
- Reset Password
- Email Templates
- JWT Hardening
- Password Reset Tokens

## User Management Features

- User CRUD
- Activate user
- Deactivate user
- Admin Password Reset
- Force Password Change After Reset
- CSV import
- XLS import
- XLSX import
- User import template download

## Pending Features

1. User Management UI
2. Profile Page
3. Profile Photo Upload
4. Notifications
5. Global Search
6. AI Features

## Current Backend Package Pattern

Backend modules use package-per-feature under:

```text
backend/src/main/java/com/company/learninghub/
```

Typical module structure:

```text
module/
├── controller/
├── domain/
├── dto/
├── mapper/
├── repository/
└── service/
```

## Current Frontend Structure

Frontend code lives under:

```text
frontend/src/
├── api/
├── auth/
├── components/
├── layout/
├── pages/
├── routes/
├── theme/
└── types/
```

## Important Implementation Notes

- Do not recreate identity tables or duplicate `User`, `Role`, `RoleName`, or `UserRole`.
- Use existing `UserRepository` and `RoleRepository` for identity-related work.
- Use Flyway only when schema changes are required.
- Use Spring Specifications for dynamic filtering/search where nullable JPQL parameters can cause PostgreSQL type inference problems.
- Preserve UTC timestamp handling with `Instant` and `TIMESTAMPTZ`.
- Keep endpoints under `/api/v1`.
- All new APIs require Swagger/OpenAPI documentation.
- All new modules require service and controller tests.
