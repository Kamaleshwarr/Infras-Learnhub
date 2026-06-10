# Command: Review Pull Request

Use this checklist for PR reviews.

## Review Order

1. Understand business goal.
2. Read changed files.
3. Check architecture consistency.
4. Check security.
5. Check database changes.
6. Check tests.
7. Run validation commands when possible.

## Backend Review Checklist

- Controllers use DTOs.
- Services contain business logic.
- Repositories are minimal and safe.
- Specifications used for dynamic filters.
- Swagger annotations present.
- Method security present.
- Validation present.
- Flyway migration present if schema changed.
- Tests cover happy path and failure path.

## Frontend Review Checklist

- Uses typed API modules.
- Uses shared layout/components.
- Loading/error/empty states present.
- Auth and role logic correct.
- Material UI used consistently.
- Tests cover critical behavior.

## Database Review Checklist

- Migration order correct.
- Constraints and indexes included.
- No duplicate tables/entities.
- Existing data compatibility considered.
- Query performance acceptable.

## Security Review Checklist

- Authentication required where expected.
- ADMIN-only endpoints protected.
- Ownership/membership checks enforced.
- Negative authorization tests included.
- Sensitive values not logged.

## Output Format

If issues exist:

```text
Findings
1. [Severity] File/line - description

Questions
- ...

Summary
- ...
```

If no issues:

```text
No blocking issues found.
Tests/validation reviewed:
- ...
Residual risks:
- ...
```
