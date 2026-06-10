# API Module Template

Use this template when creating a new backend module.

## Module Name

```text
<Module Name>
```

## Business Goal

```text
Describe what this module enables for users/admins.
```

## Package Structure

```text
backend/src/main/java/com/company/learninghub/<module>/
├── controller/
├── domain/
├── dto/
├── mapper/
├── repository/
└── service/
```

## Entity

Checklist:

- Reuse existing entities when possible.
- Add entity only if new table/domain concept is required.
- Use UUID primary key.
- Use `Instant` for timestamps.
- Use relationships with explicit foreign keys.

Example entity considerations:

```text
id
name/title
description
status/type enum
createdBy/uploadedBy
createdAt
updatedAt
```

## DTOs

Required DTO types:

```text
Create<Thing>Request
Update<Thing>Request
<Thing>Response
```

DTO rules:

- Validation annotations on request DTOs.
- No JPA entities in API responses.
- Include UTC suffix for timestamp response fields.

## Service

Service methods:

```text
list
get
create
update
delete/archive
```

Service rules:

- Constructor injection.
- `@Transactional`.
- Method security.
- Business validation.
- Ownership/membership checks.

## Repository

Repository rules:

- Extend `JpaRepository`.
- Extend `JpaSpecificationExecutor` for filtering/search.
- Use `@EntityGraph` for response mapping needs.
- Avoid nullable JPQL search predicates.

## Controller

Controller rules:

- Base path under `/api/v1`.
- Use `@Tag`.
- Use `@Operation`.
- Use `@SecurityRequirement`.
- Return DTOs.
- Use `PageResponse<T>` for paged endpoints.

## Swagger

Required:

```java
@Tag(name = "<Module>", description = "<Description>")
@SecurityRequirement(name = "bearerAuth")
```

## Tests

Required tests:

- Service test.
- Controller test.
- Method-security test.
- Integration test when database-specific behavior is involved.

## Validation Commands

```bash
mvn -f backend/pom.xml test
```

## Done Checklist

- [ ] No duplicate entities.
- [ ] No duplicate tables.
- [ ] DTOs added.
- [ ] Service added.
- [ ] Repository added.
- [ ] Controller added.
- [ ] Swagger documented.
- [ ] Validation added.
- [ ] Tests added.
- [ ] Build/test passes.
