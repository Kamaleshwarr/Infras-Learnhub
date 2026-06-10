# Flyway Migration Template

Use this template when adding database schema changes.

## File Naming

```text
backend/src/main/resources/db/migration/V<number>__short_description.sql
```

Examples:

```text
V7__create_notifications.sql
V8__create_global_search_index.sql
```

## Migration Structure

Recommended order:

```sql
CREATE TABLE ...

ALTER TABLE ... ADD CONSTRAINT ...

CREATE INDEX ...

INSERT INTO ... -- seed data only when required
```

## Table Standards

- Use `UUID` primary keys.
- Use `gen_random_uuid()` where database-generated IDs are needed.
- Use `TIMESTAMPTZ` for UTC timestamps.
- Include `created_at` and `updated_at` when entities are auditable.
- Add foreign keys.
- Add uniqueness constraints.
- Add check constraints for enum values.

## Index Standards

Add indexes for:

- Foreign keys.
- Frequently filtered fields.
- Search fields.
- Sorting fields when needed.

## Rollback Considerations

Flyway Community migrations are forward-only by default in this project.

Before writing migration:

- Verify it is safe to apply once.
- Avoid destructive changes unless explicitly required.
- Use additive schema changes when possible.
- Preserve existing data.

## Validation Checklist

- [ ] Migration number is next in sequence.
- [ ] Migration name is descriptive.
- [ ] SQL is PostgreSQL-compatible.
- [ ] Foreign keys reference existing tables.
- [ ] Enum check constraints match Java enums.
- [ ] Indexes support expected queries.
- [ ] No duplicate tables.
- [ ] No duplicate columns.
- [ ] Application entity mappings match schema.

## Example

```sql
CREATE TABLE example_entities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_example_entities_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_example_entities_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_example_entities_status ON example_entities (status);
CREATE INDEX idx_example_entities_created_by ON example_entities (created_by);
```

## Test Expectations

For schema-affecting changes:

- Run backend test suite.
- Add integration tests when query behavior is PostgreSQL-specific.
- Validate Flyway startup through Docker Compose when possible.
