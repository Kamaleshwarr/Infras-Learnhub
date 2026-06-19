import { describe, expect, it } from 'vitest'
import type { Initiative, InitiativeSummary } from './initiatives'

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
})
