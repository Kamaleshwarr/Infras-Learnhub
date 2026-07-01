import { describe, expect, it } from 'vitest'
import type { CreateInitiativeRequest, Initiative, InitiativeSummary, UpdateInitiativeRequest } from './initiatives'

describe('initiative types compatibility', () => {
  it('treats InitiativeSummary as an alias for Initiative', () => {
    const initiative: Initiative = {
      description: 'Program details',
      expiryDateUtc: '2026-12-31T00:00:00Z',
      id: 'initiative-1',
      startDateUtc: '2026-01-01T00:00:00Z',
      status: 'ACTIVE',
      title: 'AWS Certification',
    }

    const summary: InitiativeSummary = initiative

    expect(summary.title).toBe('AWS Certification')
    expect(summary.id).toBe(initiative.id)
  })

  it('allows shared create and update request shapes', () => {
    const request: CreateInitiativeRequest = {
      description: 'Program details',
      expiryDateUtc: '2026-12-31T00:00:00.000Z',
      rewardDescription: null,
      startDateUtc: '2026-01-01T00:00:00.000Z',
      title: 'AWS Certification',
    }

    const updateRequest: UpdateInitiativeRequest = request

    expect(updateRequest.title).toBe('AWS Certification')
  })
})
