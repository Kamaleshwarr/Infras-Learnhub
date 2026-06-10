# Learning Hub Architecture

## Backend Architecture

The backend is a Java 21 / Spring Boot 3 layered application.

Primary layers:

```text
Controller -> DTO/Mapper -> Service -> Repository -> Domain Entity -> Database
```

### Controller Layer

- Exposes REST APIs under `/api/v1`.
- Applies request validation with Jakarta Validation.
- Uses `@Tag`, `@Operation`, and `@SecurityRequirement` for Swagger/OpenAPI.
- Uses `PageResponse<T>` for paginated API responses.
- Uses `@AuthenticationPrincipal AuthenticatedUser` when current user context is needed.

### DTO and Mapper Layer

- Controllers should not expose entities directly.
- Request DTOs define validation.
- Response DTOs define stable public API contracts.
- Mappers convert entities to DTOs.

### Service Layer

- Required for all business logic.
- Uses constructor injection.
- Applies `@Transactional`.
- Applies method security with `@PreAuthorize`.
- Owns authorization rules beyond global authentication.
- Builds dynamic Specifications for filtering.

### Repository Layer

- Uses Spring Data JPA.
- Uses `JpaRepository`.
- Uses `JpaSpecificationExecutor` for dynamic search/filtering.
- Uses custom query repositories only for SQL-heavy/reporting use cases such as leaderboards.
- Uses `@EntityGraph` when avoiding lazy-loading problems in response mapping.

## Frontend Architecture

The frontend is React + TypeScript + Material UI.

Primary structure:

```text
api -> auth -> routes -> layout -> pages -> components -> types
```

### API Layer

- Axios instance lives in `src/api/httpClient.ts`.
- Feature APIs live in `src/api/*Api.ts`.
- JWT bearer token is attached by Axios interceptor.

### Auth Layer

- Context API owns current user state.
- `AuthProvider` restores session using `/auth/me`.
- `ProtectedRoute` blocks unauthenticated access.
- `RoleRoute` blocks role-specific routes.

### Layout Layer

- `AppLayout` provides responsive shell.
- `Sidebar` renders role-aware navigation.
- `Header` shows user and logout.

### Page Layer

- Pages should orchestrate API calls and compose reusable components.
- Pages must implement loading, error, and empty states.
- Material UI should be used consistently.

## Database Architecture

Database is PostgreSQL managed by Flyway.

Core schemas/tables:

- `users`
- `roles`
- `user_roles`
- `learning_initiatives`
- `certificate_documents`
- `certificate_submissions`
- `study_material_folders`
- `study_materials`
- `study_material_download_events`
- `projects`
- `project_members`
- `project_knowledge_folders`
- `project_knowledge_items`
- `project_knowledge_access_events`

## Entity Relationships

```text
User -> UserRole -> Role
User -> LearningInitiative.createdBy
User -> CertificateSubmission.employee
User -> CertificateSubmission.reviewedBy
User -> CertificateDocument.uploadedBy
User -> StudyMaterial.uploadedBy
User -> Project.createdBy
User -> ProjectMember.user
User -> ProjectKnowledgeItem.uploadedBy
```

```text
LearningInitiative -> CertificateSubmission -> CertificateDocument
```

```text
StudyMaterialFolder -> child folders
StudyMaterialFolder -> StudyMaterial -> StudyMaterialDownloadEvent
```

```text
Project -> ProjectMember
Project -> ProjectKnowledgeFolder -> child folders
Project -> ProjectKnowledgeItem -> ProjectKnowledgeAccessEvent
```

## Authentication Flow

1. User calls `POST /api/v1/auth/login`.
2. Backend authenticates credentials using Spring Security and BCrypt.
3. Backend returns JWT and user summary.
4. Frontend stores JWT in session storage.
5. Axios attaches `Authorization: Bearer <token>`.
6. Frontend restores session with `GET /api/v1/auth/me`.
7. Expired or invalid JWT returns 401 and frontend clears auth state.

## Authorization Model

### Global Roles

- `ADMIN`
- `EMPLOYEE`

### Project Roles

- `OWNER`
- `CONTRIBUTOR`
- `VIEWER`

### Common Rules

- Admin-only APIs use `@PreAuthorize("hasRole('ADMIN')")`.
- Mixed APIs use service-level role/project membership checks.
- Employees can access only their own certificate submissions.
- Members-only projects require project membership unless system admin.

## API Design Standards

- Base path: `/api/v1`
- Use nouns for resources.
- Use standard HTTP status codes.
- Use `PageResponse<T>` for paged endpoints.
- Use request/response DTOs.
- Use validation annotations on request DTOs.
- Use Swagger annotations on controllers.
- Keep error responses centralized through global exception handling.

## Module Structure

```text
com.company.learninghub.<module>/
├── controller/
├── domain/
├── dto/
├── mapper/
├── repository/
└── service/
```

## DTO Pattern

- Create request DTOs for writes.
- Update request DTOs for mutations.
- Response DTOs for API responses.
- Avoid returning JPA entities.
- DTOs should reflect API contract, not database internals.

## Service Layer Pattern

- Constructor injection only.
- Transaction boundaries live here.
- Authorization and business rules live here.
- Normalize search/filter inputs here.
- Use meaningful exceptions with consistent messages.

## Repository Pattern

- Prefer Spring Data repositories.
- Use Specifications for dynamic filters.
- Avoid nullable JPQL predicates of the form `(:param IS NULL OR field LIKE :param)` when parameter type inference may break in PostgreSQL.
- Use native SQL only when needed for reporting/window functions.

## Flyway Migration Strategy

- Use sequential versioned migrations:

```text
V1__create_identity_schema.sql
V2__seed_default_users.sql
...
```

- Add migrations only for schema changes.
- Never modify already-applied migrations after merge.
- Use constraints and indexes for integrity/performance.
- Use `TIMESTAMPTZ` for UTC timestamps.

## Testing Strategy

Required test types:

- Service tests for business rules.
- Controller tests for API contracts.
- Method-security tests for role restrictions.
- Integration tests when database behavior is risk-prone.
- Import/parser tests for file import flows.

## Swagger / OpenAPI Strategy

- Every controller needs `@Tag`.
- Every endpoint needs `@Operation`.
- Authenticated APIs should use `@SecurityRequirement(name = "bearerAuth")`.
- Swagger sections should match business module names:
  - Authentication
  - Learning Initiatives
  - Certificate Submissions
  - Leaderboards
  - Study Materials
  - Project Knowledge
  - Users
