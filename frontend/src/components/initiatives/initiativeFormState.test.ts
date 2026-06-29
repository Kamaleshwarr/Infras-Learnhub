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
  it('creates empty form defaults as draft with UTC dates', () => {
    const form = createEmptyInitiativeForm(Date.parse('2026-06-19T12:00:00.000Z'))

    expect(form.status).toBe('DRAFT')
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
      status: 'ACTIVE',
    })
  })

  it('validates required fields and date range', () => {
    const errors = getInitiativeFormFieldErrors({
      title: '',
      description: '',
      rewardDescription: '',
      startDate: '2026-06-20',
      expiryDate: '2026-06-19',
      status: 'DRAFT',
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
      status: 'DRAFT',
    })).toBe(false)
  })

  it('builds create and update payloads with null reward when empty', () => {
    const values = {
      title: 'Azure Certification',
      description: 'Azure program',
      rewardDescription: '   ',
      startDate: '2026-06-01',
      expiryDate: '2026-12-31',
      status: 'DRAFT' as const,
    }

    expect(buildCreateInitiativeRequest(values)).toEqual({
      title: 'Azure Certification',
      description: 'Azure program',
      rewardDescription: null,
      startDateUtc: '2026-06-01T00:00:00.000Z',
      expiryDateUtc: '2026-12-31T00:00:00.000Z',
      status: 'DRAFT',
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
})
