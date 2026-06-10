# Frontend Page Template

Use this template when creating a new frontend page.

## Page Name

```text
<Feature Page>
```

## Route

```text
/<route>
```

## Required Role

```text
ADMIN | EMPLOYEE | Both
```

## Files

```text
frontend/src/pages/<feature>/<FeaturePage>.tsx
frontend/src/api/<feature>Api.ts
frontend/src/types/<feature>.ts       # optional
frontend/src/components/<feature>/    # optional
```

## API Integration

API method:

```ts
export const featureApi = {
  list: async () => {
    const response = await httpClient.get(...)
    return response.data
  },
}
```

Rules:

- Use `httpClient`.
- Type API responses.
- Do not duplicate backend URL logic.

## Page Component Checklist

- [ ] Uses `PageHeader`.
- [ ] Uses Material UI layout.
- [ ] Has loading state.
- [ ] Has error state.
- [ ] Has empty state.
- [ ] Uses typed API calls.
- [ ] Handles role restrictions.
- [ ] Has tests.

## Form Validation Checklist

- [ ] Required fields.
- [ ] Email validation when applicable.
- [ ] File validation messaging when applicable.
- [ ] Disable submit while loading.
- [ ] Show backend error messages.

## Route Registration

Add route in:

```text
frontend/src/routes/AppRoutes.tsx
```

If role-specific:

```tsx
<Route element={<RoleRoute roles={['ADMIN']} />}>
  <Route path="..." element={<FeaturePage />} />
</Route>
```

## Navigation

Add nav item in:

```text
frontend/src/layout/navigation.tsx
```

Only add if page belongs in sidebar.

## Tests

Recommended tests:

- Renders page title.
- Loading state.
- Error state.
- Empty state.
- Success state.
- Role-specific visibility.
- Form validation.

## Validation Commands

```bash
cd frontend
npm run test
npm run build
```
