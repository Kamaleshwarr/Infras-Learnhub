import { describe, expect, it } from 'vitest'
import { formatEffortRange, getRemainingEffortSummary, parseEffortString, summarizeStageEfforts } from './roadmapEffort'

describe('roadmapEffort', () => {
  it('parses single and ranged effort strings', () => {
    expect(parseEffortString('1 week')).toEqual({ minDays: 7, maxDays: 7 })
    expect(parseEffortString('2 weeks')).toEqual({ minDays: 14, maxDays: 14 })
    expect(parseEffortString('1-2 weeks')).toEqual({ minDays: 7, maxDays: 14 })
    expect(parseEffortString('3-5 days')).toEqual({ minDays: 3, maxDays: 5 })
  })

  it('summarizes total roadmap effort from stages', () => {
    expect(summarizeStageEfforts(['1 week', '2 weeks'])).toBe('≈ 3 weeks')
    expect(summarizeStageEfforts(['1 week', '1-2 weeks'])).toBe('≈ 2–3 weeks')
  })

  it('formats long roadmaps in months', () => {
    expect(formatEffortRange({ minDays: 56, maxDays: 70 })).toBe('≈ 2–3 months')
  })

  it('summarizes remaining stages and effort', () => {
    const summary = getRemainingEffortSummary(
      [
        { id: 'a', estimatedEffort: '1 week' },
        { id: 'b', estimatedEffort: '2 weeks' },
      ],
      ['a'],
    )

    expect(summary.stagesLeft).toBe(1)
    expect(summary.effortLabel).toBe('≈ 2 weeks')
  })
})
