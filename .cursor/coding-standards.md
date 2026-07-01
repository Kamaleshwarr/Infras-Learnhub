# Learning Hub Coding Standards

## General Standards

- Use enterprise-grade, maintainable code.
- Prefer clarity over cleverness.
- Follow existing package and naming conventions.
- Do not duplicate existing entities, tables, services, or repositories.
- Keep changes scoped to the requested module.
- Do not introduce unrelated refactors.

## Backend Standards

### Dependency Injection

- Constructor injection only.
- No field injection.
- No `@Autowired` fields.

### DTO Pattern

- DTOs are required for all public APIs.
- Do not expose JPA entities directly.
- Request DTOs must contain validation annotations.
- Response DTOs must be stable and API-friendly.

### Service Layer

- Every non-trivial module requires a service.
- Business logic belongs in the service layer.
- Security decisions beyond global role checks belong in the service layer.
- Services should be transactional.

### Repository Layer

- Use Spring Data JPA repositories.
- Use `JpaSpecificationExecutor` for search/filter APIs.
- Use custom SQL repositories only for reporting/window-function cases.
- Avoid nullable JPQL parameters that cause PostgreSQL type inference issues.

### Validation

- Use Jakarta Validation for request DTOs.
- Validate business invariants in services.
- Validate file size and MIME type before storage.
- Validate references belong to the expected parent/resource.

### Swagger / OpenAPI

- Swagger documentation is required for all controllers.
- Use `@Tag` at controller level.
- Use `@Operation` for each endpoint.
- Use `@SecurityRequirement(name = "bearerAuth")` for authenticated APIs.

### Testing

- Unit tests are required.
- Controller tests are required for API contract behavior.
- Method-security tests are required for role-restricted modules.
- Integration tests are required when behavior depends on PostgreSQL-specific SQL or parameter binding.
- File import/export logic must have parser tests.

### Database

- Use Flyway for schema changes.
- Do not modify merged migrations.
- Add indexes for filter/search columns.
- Add foreign keys and constraints.
- Use `TIMESTAMPTZ` for timestamps.
- Use `UUID` primary keys.

### Search and Filtering

- Use Spring Specifications for dynamic search/filter criteria.
- Normalize blank strings to `null`.
- Do not write JPQL like:

```sql
:search IS NULL OR LOWER(field) LIKE CONCAT('%', :search, '%')
```

This can cause PostgreSQL parameter type inference issues.

### Entity Reuse

- Reuse existing `User`, `Role`, `RoleName`, and `UserRole`.
- Reuse existing repositories where appropriate.
- Do not create duplicate user or role tables.
- Do not create duplicate identity entities.

## Frontend Standards

### React / TypeScript

- Use functional components.
- Use explicit types for API responses and props.
- Keep components small and composable.
- Use Context API only for app-wide state such as authentication.

### Material UI

- Use Material UI components consistently.
- Use responsive grids.
- Implement loading, error, and empty states.
- Keep visual patterns consistent across pages.

### API Integration

- Use `src/api/httpClient.ts` for Axios configuration.
- Feature-specific APIs belong in `src/api/*Api.ts`.
- Do not call Axios directly from many components unless a feature needs it.

### Routing

- Use protected routes for authenticated areas.
- Use role routes for role-specific areas.
- Keep route definitions centralized.

### Authentication

- JWT is stored in session storage.
- Axios attaches bearer token automatically.
- 401 responses clear auth state.
- Login page must validate email/password and show backend-aware errors.

### Text Display

Use the shared text display utilities in `src/components/common/` for all user-entered or API-returned text.

- **List/table views:** `TruncatedTextWithTooltip` with limits from `TEXT_DISPLAY_LIMITS` in `textDisplay.ts`. Tables use `fixedTableSx`, constrained column widths, and `minWidth: 0` on text cells.
- **Detail pages, cards, dialogs, and page headers:** `WrappingText` or `longTextWrapSx` so full content wraps without horizontal overflow.
- **Read-only form values:** `readOnlyTextFieldSlotProps` for profile-style display fields.
- **Do not** add page-specific truncation helpers; extend the shared utilities instead.

## Pull Request Standards

- Include summary of changes.
- Include tests run.
- Include migration notes if schema changed.
- Include security notes if role behavior changed.
- Include known limitations or follow-up work.

## Do Not Do

- Do not duplicate tables.
- Do not duplicate entities.
- Do not bypass service layer.
- Do not return entities from controllers.
- Do not add schema changes without Flyway.
- Do not add untested authorization paths.
- Do not hardcode secrets.
- Do not weaken existing security rules.
