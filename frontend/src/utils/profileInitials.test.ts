import { describe, expect, it } from 'vitest'
import { getProfileInitials } from './profileInitials'

describe('getProfileInitials', () => {
  it('returns first and last initials for multi-word names', () => {
    expect(getProfileInitials('Jane Doe')).toBe('JD')
  })

  it('returns first two characters for single-word names', () => {
    expect(getProfileInitials('Madonna')).toBe('MA')
  })

  it('returns question mark for blank names', () => {
    expect(getProfileInitials('   ')).toBe('?')
  })
})
