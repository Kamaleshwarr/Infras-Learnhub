# Command: Create Backend Module

Use this checklist when creating a new backend module.

## Inputs Required

- Module name.
- Business goal.
- Roles and permissions.
- Required APIs.
- Data model.
- Validation rules.
- Search/filter requirements.
- File/storage needs.

## Process

1. Read `.cursor/project-context.md`.
2. Read `.cursor/architecture.md`.
3. Read `.cursor/coding-standards.md`.
4. Inspect the closest existing module.
5. Decide whether schema changes are needed.
6. Add Flyway migration if needed.
7. Add domain entities or reuse existing entities.
8. Add DTOs.
9. Add mapper.
10. Add repository.
11. Add service.
12. Add controller.
13. Add Swagger documentation.
14. Add tests.
15. Run tests.

## Required Backend Package Structure

```text
backend/src/main/java/com/company/learninghub/<module>/
├── controller/
├── domain/
├── dto/
├── mapper/
├── repository/
└── service/
```

## Required Tests

```text
backend/src/test/java/com/company/learninghub/<module>/
├── controller/
├── service/
└── repository/    # when applicable
```

## Security Checklist

- Use `@PreAuthorize`.
- Add negative tests for unauthorized roles.
- Use `AuthenticatedUser` for current-user decisions.
- Hide inaccessible resources with 404 when appropriate.

## Search Checklist

- Use Specifications for dynamic filters.
- Avoid nullable JPQL search parameters.
- Add tests for null, empty, and text search.

## Done Criteria

- Code compiles.
- Tests pass.
- Swagger section appears correctly.
- No duplicate entities/tables.
- No unrelated changes.
