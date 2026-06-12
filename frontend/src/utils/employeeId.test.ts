import { describe, expect, it } from 'vitest'
import { normalizeEmployeeId } from './employeeId'

describe('normalizeEmployeeId', () => {
  it('trims whitespace and uppercases employee IDs', () => {
    expect(normalizeEmployeeId('  emp001  ')).toBe('EMP001')
  })
})
