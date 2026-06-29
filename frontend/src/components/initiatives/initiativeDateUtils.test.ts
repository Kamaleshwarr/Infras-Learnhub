import { describe, expect, it } from 'vitest'
import {
  addUtcDays,
  compareUtcDateInputs,
  instantToUtcDateInput,
  isUtcDateBefore,
  isUtcDateOnOrAfter,
  isUtcDateInput,
  todayUtcDateInput,
  utcDateInputToInstant,
} from './initiativeDateUtils'

describe('initiativeDateUtils', () => {
  it('validates UTC date input format', () => {
    expect(isUtcDateInput('2026-06-19')).toBe(true)
    expect(isUtcDateInput('2026-6-19')).toBe(false)
    expect(isUtcDateInput('')).toBe(false)
  })

  it('converts UTC date input to instant at start of day', () => {
    expect(utcDateInputToInstant('2026-06-19')).toBe('2026-06-19T00:00:00.000Z')
  })

  it('converts instant to UTC date input', () => {
    expect(instantToUtcDateInput('2026-06-19T15:30:00.000Z')).toBe('2026-06-19')
    expect(instantToUtcDateInput('2026-06-19T00:00:00.000Z')).toBe('2026-06-19')
  })

  it('returns today in UTC for a fixed timestamp', () => {
    expect(todayUtcDateInput(Date.parse('2026-06-19T12:00:00.000Z'))).toBe('2026-06-19')
  })

  it('adds days in UTC without local timezone drift', () => {
    expect(addUtcDays('2026-06-19', 90)).toBe('2026-09-17')
  })

  it('compares UTC date inputs', () => {
    expect(compareUtcDateInputs('2026-06-19', '2026-06-20')).toBeLessThan(0)
    expect(isUtcDateBefore('2026-06-18', '2026-06-19')).toBe(true)
    expect(isUtcDateOnOrAfter('2026-06-19', '2026-06-19')).toBe(true)
    expect(isUtcDateOnOrAfter('2026-06-18', '2026-06-19')).toBe(false)
  })
})
