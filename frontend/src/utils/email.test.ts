import { describe, expect, it } from 'vitest'
import { normalizeEmail } from './email'

describe('normalizeEmail', () => {
  it('trims whitespace and lowercases email addresses', () => {
    expect(normalizeEmail('  Jane.Doe@Example.COM  ')).toBe('jane.doe@example.com')
  })
})
