import { describe, expect, it } from 'vitest'
import {
  EXPIRING_SOON_DAYS,
  daysUntilExpiry,
  formatInitiativeDateRange,
  isExpiringCritical,
  isExpiringSoon,
  truncateText,
} from './initiativeDisplay'

const NOW = Date.parse('2026-06-01T12:00:00Z')

describe('initiativeDisplay', () => {
  it('calculates days until expiry', () => {
    const inTenDays = new Date(NOW + 10 * 24 * 60 * 60 * 1000).toISOString()
    expect(daysUntilExpiry(inTenDays, NOW)).toBe(10)
  })

  it('detects initiatives expiring within the default window', () => {
    const inTenDays = new Date(NOW + 10 * 24 * 60 * 60 * 1000).toISOString()
    const inTwentyDays = new Date(NOW + 20 * 24 * 60 * 60 * 1000).toISOString()

    expect(isExpiringSoon(inTenDays, EXPIRING_SOON_DAYS, NOW)).toBe(true)
    expect(isExpiringSoon(inTwentyDays, EXPIRING_SOON_DAYS, NOW)).toBe(false)
  })

  it('returns false for past expiry dates', () => {
    expect(isExpiringSoon('2026-05-01T00:00:00Z', EXPIRING_SOON_DAYS, NOW)).toBe(false)
  })

  it('detects critical expiry within three days', () => {
    const inTwoDays = new Date(NOW + 2 * 24 * 60 * 60 * 1000).toISOString()
    const inFiveDays = new Date(NOW + 5 * 24 * 60 * 60 * 1000).toISOString()

    expect(isExpiringCritical(inTwoDays, NOW)).toBe(true)
    expect(isExpiringCritical(inFiveDays, NOW)).toBe(false)
  })

  it('formats initiative date ranges with UTC label', () => {
    expect(formatInitiativeDateRange('2026-06-01T00:00:00Z', '2026-06-30T00:00:00Z')).toContain('(UTC)')
    expect(formatInitiativeDateRange('2026-06-01T00:00:00Z', '2026-06-30T00:00:00Z')).toContain('–')
  })

  it('truncates long text with an ellipsis', () => {
    const text = 'a'.repeat(80)
    expect(truncateText(text, 60)).toHaveLength(61)
    expect(truncateText(text, 60).endsWith('…')).toBe(true)
    expect(truncateText('short text', 60)).toBe('short text')
  })
})
