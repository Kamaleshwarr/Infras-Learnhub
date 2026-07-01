import { describe, expect, it } from 'vitest'
import type { Initiative } from '../../types/initiatives'
import {
  buildCreateInitiativeRequest,
  buildUpdateInitiativeRequest,
  createEmptyInitiativeForm,
  getInitiativeFormFieldErrors,
  initiativeToFormValues,
  isInitiativeFormDirty,
  isInitiativeFormValid,
} from './initiativeFormState'

const initiative: Initiative = {
  createdAtUtc: '2026-01-01T00:00:00Z',
  createdBy: {
    email: 'admin@example.com',
    employeeId: 'ADMIN001',
    fullName: 'Admin User',
    id: 'admin-1',
  },
  description: 'Program details',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  rewardDescription: '$500 credit',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE',
  title: 'AWS Certification',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('initiativeFormState', () => {
  it('creates empty form defaults with UTC dates', () => {
    const form = createEmptyInitiativeForm(Date.parse('2026-06-19T12:00:00.000Z'))

    expect(form.startDate).toBe('2026-06-19')
    expect(form.expiryDate).toBe('2026-09-17')
    expect(form.title).toBe('')
  })

  it('maps initiative response values into form state', () => {
    expect(initiativeToFormValues(initiative)).toEqual({
      title: 'AWS Certification',
      description: 'Program details',
      rewardDescription: '$500 credit',
      startDate: '2026-01-01',
      expiryDate: '2026-12-31',
    })
  })

  it('validates required fields and date range for create mode', () => {
    const now = Date.parse('2026-06-19T12:00:00.000Z')
    const errors = getInitiativeFormFieldErrors(
      {
        title: '',
        description: '',
        rewardDescription: '',
        startDate: '2026-06-18',
        expiryDate: '2026-06-18',
      },
      { mode: 'create', now },
    )

    expect(errors.title).toBeTruthy()
    expect(errors.description).toBeTruthy()
    expect(errors.startDate).toContain('today')
    expect(isInitiativeFormValid(
      {
        title: 'Azure',
        description: 'Program',
        rewardDescription: '',
        startDate: '2026-06-19',
        expiryDate: '2026-06-18',
      },
      { mode: 'create', now },
    )).toBe(false)
  })

  it('allows expiry on the same day as start for create mode', () => {
    const now = Date.parse('2026-06-19T12:00:00.000Z')
    const errors = getInitiativeFormFieldErrors(
      {
        title: 'Azure',
        description: 'Program',
        rewardDescription: '',
        startDate: '2026-06-19',
        expiryDate: '2026-06-19',
      },
      { mode: 'create', now },
    )

    expect(errors.expiryDate).toBeUndefined()
  })

  it('allows unchanged past start dates in edit mode', () => {
    const now = Date.parse('2026-07-20T12:00:00.000Z')
    const baseline = {
      title: 'Azure',
      description: 'Program',
      rewardDescription: '',
      startDate: '2026-07-01',
      expiryDate: '2026-12-31',
    }
    const errors = getInitiativeFormFieldErrors(
      {
        ...baseline,
        description: 'Updated program details',
      },
      { mode: 'edit', now, baseline },
    )

    expect(errors.startDate).toBeUndefined()
  })

  it('rejects modified start dates before today in edit mode', () => {
    const now = Date.parse('2026-07-20T12:00:00.000Z')
    const baseline = {
      title: 'Azure',
      description: 'Program',
      rewardDescription: '',
      startDate: '2026-07-01',
      expiryDate: '2026-12-31',
    }
    const errors = getInitiativeFormFieldErrors(
      {
        ...baseline,
        startDate: '2026-07-10',
      },
      { mode: 'edit', now, baseline },
    )

    expect(errors.startDate).toContain('today')
  })

  it('allows modified start dates on or after today in edit mode', () => {
    const now = Date.parse('2026-07-20T12:00:00.000Z')
    const baseline = {
      title: 'Azure',
      description: 'Program',
      rewardDescription: '',
      startDate: '2026-07-01',
      expiryDate: '2026-12-31',
    }
    const todayErrors = getInitiativeFormFieldErrors(
      {
        ...baseline,
        startDate: '2026-07-20',
      },
      { mode: 'edit', now, baseline },
    )
    const futureErrors = getInitiativeFormFieldErrors(
      {
        ...baseline,
        startDate: '2026-07-21',
      },
      { mode: 'edit', now, baseline },
    )

    expect(todayErrors.startDate).toBeUndefined()
    expect(futureErrors.startDate).toBeUndefined()
  })

  it('validates required fields and date range', () => {
    const errors = getInitiativeFormFieldErrors({
      title: '',
      description: '',
      rewardDescription: '',
      startDate: '2026-06-20',
      expiryDate: '2026-06-19',
    })

    expect(errors.title).toBeTruthy()
    expect(errors.description).toBeTruthy()
    expect(errors.expiryDate).toBeTruthy()
    expect(isInitiativeFormValid({
      title: '',
      description: '',
      rewardDescription: '',
      startDate: '2026-06-20',
      expiryDate: '2026-06-19',
    })).toBe(false)
  })

  it('builds create and update payloads with null reward when empty', () => {
    const values = {
      title: 'Azure Certification',
      description: 'Azure program',
      rewardDescription: '   ',
      startDate: '2026-06-01',
      expiryDate: '2026-12-31',
    }

    expect(buildCreateInitiativeRequest(values)).toEqual({
      title: 'Azure Certification',
      description: 'Azure program',
      rewardDescription: null,
      startDateUtc: '2026-06-01T00:00:00.000Z',
      expiryDateUtc: '2026-12-31T00:00:00.000Z',
    })
    expect(buildUpdateInitiativeRequest(values)).toEqual(buildCreateInitiativeRequest(values))
  })

  it('detects dirty state against baseline', () => {
    const baseline = initiativeToFormValues(initiative)
    const unchanged = { ...baseline }
    const changed = { ...baseline, title: 'Updated title' }

    expect(isInitiativeFormDirty(unchanged, baseline)).toBe(false)
    expect(isInitiativeFormDirty(changed, baseline)).toBe(true)
  })

  it('rejects values that exceed field length limits', () => {
    const errors = getInitiativeFormFieldErrors({
      title: 't'.repeat(101),
      description: 'd'.repeat(2001),
      rewardDescription: 'r'.repeat(501),
      startDate: '2026-06-01',
      expiryDate: '2026-12-31',
    })

    expect(errors.title).toContain('100')
    expect(errors.description).toContain('2000')
    expect(errors.rewardDescription).toContain('500')
  })

  it('accepts values at the maximum field length limits', () => {
    const now = Date.parse('2026-06-19T12:00:00.000Z')
    const errors = getInitiativeFormFieldErrors(
      {
        title: 't'.repeat(100),
        description: 'd'.repeat(2000),
        rewardDescription: 'r'.repeat(500),
        startDate: '2026-06-19',
        expiryDate: '2026-12-31',
      },
      { now },
    )

    expect(errors).toEqual({})
  })
})
