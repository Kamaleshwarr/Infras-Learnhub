import { describe, expect, it } from 'vitest'
import { TEXT_DISPLAY_LIMITS, truncateText } from './textDisplay'

describe('textDisplay', () => {
  it('truncates long text with an ellipsis', () => {
    const text = 'a'.repeat(80)
    expect(truncateText(text, 60)).toHaveLength(61)
    expect(truncateText(text, 60).endsWith('…')).toBe(true)
    expect(truncateText('short text', 60)).toBe('short text')
  })

  it('exposes shared display limits for list and table views', () => {
    expect(TEXT_DISPLAY_LIMITS.tableTitle).toBe(60)
    expect(TEXT_DISPLAY_LIMITS.notificationMessage).toBe(120)
  })
})
