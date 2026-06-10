# Command: Create Frontend Page

Use this checklist when creating a new frontend page.

## Inputs Required

- Page name.
- Route path.
- Required API endpoints.
- Required roles.
- Main UI states.
- Form fields if applicable.

## Process

1. Add typed API function in `frontend/src/api`.
2. Add route in `frontend/src/routes/AppRoutes.tsx`.
3. Add nav item if page should be in sidebar.
4. Add page component under `frontend/src/pages`.
5. Use shared components from `frontend/src/components`.
6. Add loading state.
7. Add error state.
8. Add empty state.
9. Add validation for forms.
10. Add tests.

## Page Structure

```text
frontend/src/pages/<feature>/<FeaturePage>.tsx
```

## API Structure

```text
frontend/src/api/<feature>Api.ts
```

## UI Standards

- Use Material UI.
- Use `PageHeader`.
- Use responsive grids.
- Use clear call-to-action buttons.
- Avoid hardcoded data except placeholders during structure-only phases.

## Auth Standards

- Use `ProtectedRoute` for authenticated pages.
- Use `RoleRoute` for role-specific pages.
- Use `useAuth` to access current user and role.

## Testing Checklist

- Page renders.
- Loading state.
- Error state.
- Empty state.
- API success state.
- Role-based visibility.
- Form validation if applicable.

## Validation Commands

```bash
cd frontend
npm run test
npm run build
```
