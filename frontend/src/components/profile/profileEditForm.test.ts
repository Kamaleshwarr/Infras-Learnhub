import { describe, expect, it } from 'vitest'
import { hasProfileFormValidationErrors, isProfileFormDirty } from './profileEditForm'

describe('profileEditForm', () => {
  const baseline = {
    fullName: 'Jane Doe',
    email: 'jane.doe@company.com',
  }

  it('detects dirty state when full name changes', () => {
    expect(isProfileFormDirty({ fullName: 'Jane Smith', email: baseline.email }, baseline)).toBe(true)
  })

  it('detects dirty state when normalized email changes', () => {
    expect(
      isProfileFormDirty({ fullName: baseline.fullName, email: 'JANE.SMITH@company.com' }, baseline),
    ).toBe(true)
  })

  it('returns false when form matches baseline', () => {
    expect(isProfileFormDirty(baseline, baseline)).toBe(false)
  })

  it('flags validation errors for empty required fields', () => {
    expect(hasProfileFormValidationErrors({ fullName: '', email: '' }, {})).toBe(true)
  })
})
