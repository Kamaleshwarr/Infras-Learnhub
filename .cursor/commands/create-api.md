# Command: Create API Endpoint

Use this checklist when adding or changing an API endpoint.

## Inputs Required

- HTTP method.
- Path.
- Request DTO.
- Response DTO.
- Security rules.
- Validation rules.
- Pagination/sorting needs.

## Endpoint Checklist

1. Add or update request DTO.
2. Add or update response DTO.
3. Add service method.
4. Add controller method.
5. Add Swagger `@Operation`.
6. Add validation annotations.
7. Add controller tests.
8. Add service tests.
9. Add method-security tests when authorization changes.

## Path Standards

- Base path is `/api/v1`.
- Use resource-oriented nouns.
- Use subresources for nested concepts.

Examples:

```text
GET /api/v1/users
POST /api/v1/users/import
GET /api/v1/projects/{projectId}/items
```

## Response Standards

- Use DTOs.
- Use `PageResponse<T>` for paged responses.
- Use `201 Created` with `Location` for creates.
- Use `204 No Content` when returning no response body.

## Security Standards

- ADMIN-only:

```java
@PreAuthorize("hasRole('ADMIN')")
```

- ADMIN or EMPLOYEE:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
```

## Testing Checklist

- Success response.
- Validation failure.
- Not found.
- Unauthorized/forbidden.
- Pagination and sorting if applicable.
- Role-specific behavior.
