import { describe, expect, it } from 'vitest'
import { hasEditFormValidationErrors, isEditFormDirty } from './editUserForm'

const baseline = {
  fullName: 'Admin User',
  email: 'admin@example.com',
  role: 'ADMIN' as const,
}

describe('editUserForm', () => {
  it('is not dirty when values match the baseline', () => {
    expect(
      isEditFormDirty(
        { fullName: 'Admin User', email: 'admin@example.com', role: 'ADMIN' },
        baseline,
        true,
      ),
    ).toBe(false)
  })

  it('is dirty when full name changes', () => {
    expect(
      isEditFormDirty(
        { fullName: 'Updated Admin', email: 'admin@example.com', role: 'ADMIN' },
        baseline,
        true,
      ),
    ).toBe(true)
  })

  it('is dirty when normalized email changes', () => {
    expect(
      isEditFormDirty(
        { fullName: 'Admin User', email: '  Updated@Example.COM ', role: 'ADMIN' },
        baseline,
        true,
      ),
    ).toBe(true)
  })

  it('is not dirty when email changes only by casing and whitespace', () => {
    expect(
      isEditFormDirty(
        { fullName: 'Admin User', email: '  Admin@Example.COM ', role: 'ADMIN' },
        { ...baseline, email: 'admin@example.com' },
        true,
      ),
    ).toBe(false)
  })

  it('ignores role changes when role is not editable', () => {
    expect(
      isEditFormDirty(
        { fullName: 'Admin User', email: 'admin@example.com', role: 'EMPLOYEE' },
        baseline,
        false,
      ),
    ).toBe(false)
  })

  it('detects validation errors for empty required fields', () => {
    expect(
      hasEditFormValidationErrors(
        { fullName: '', email: 'admin@example.com', role: 'ADMIN' },
        {},
      ),
    ).toBe(true)
    expect(
      hasEditFormValidationErrors(
        { fullName: 'Admin User', email: '   ', role: 'ADMIN' },
        {},
      ),
    ).toBe(true)
    expect(
      hasEditFormValidationErrors(
        { fullName: 'Admin User', email: 'admin@example.com', role: 'ADMIN' },
        { email: 'must be a well-formed email address' },
      ),
    ).toBe(true)
  })
})
