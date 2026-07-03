export interface EffortRange {
  minDays: number
  maxDays: number
}

const DAYS_PER_WEEK = 7
const WEEKS_PER_MONTH = 4

function unitToDays(value: number, unit: string): number {
  if (unit.startsWith('month')) {
    return value * DAYS_PER_WEEK * WEEKS_PER_MONTH
  }
  if (unit.startsWith('week')) {
    return value * DAYS_PER_WEEK
  }
  return value
}

export function parseEffortString(value: string): EffortRange {
  const normalized = value.trim().toLowerCase()
  if (!normalized) {
    return { minDays: 0, maxDays: 0 }
  }

  const rangeMatch = normalized.match(/(\d+)\s*-\s*(\d+)\s*(day|days|week|weeks|month|months)/)
  if (rangeMatch) {
    const min = Number(rangeMatch[1])
    const max = Number(rangeMatch[2])
    const unit = rangeMatch[3]
    return {
      minDays: unitToDays(min, unit),
      maxDays: unitToDays(max, unit),
    }
  }

  const singleMatch = normalized.match(/(\d+)\s*(day|days|week|weeks|month|months)/)
  if (singleMatch) {
    const amount = Number(singleMatch[1])
    const days = unitToDays(amount, singleMatch[2])
    return { minDays: days, maxDays: days }
  }

  return { minDays: 0, maxDays: 0 }
}

export function combineEffortRanges(ranges: EffortRange[]): EffortRange {
  return ranges.reduce(
    (total, range) => ({
      minDays: total.minDays + range.minDays,
      maxDays: total.maxDays + range.maxDays,
    }),
    { minDays: 0, maxDays: 0 },
  )
}

function pluralize(count: number, singular: string, plural = `${singular}s`) {
  return count === 1 ? singular : plural
}

export function formatEffortRange({ minDays, maxDays }: EffortRange): string {
  if (minDays === 0 && maxDays === 0) {
    return '—'
  }

  const minWeeks = Math.max(1, Math.round(minDays / DAYS_PER_WEEK))
  const maxWeeks = Math.max(minWeeks, Math.round(maxDays / DAYS_PER_WEEK))

  if (maxWeeks >= 8) {
    const minMonths = Math.max(1, Math.round(minWeeks / WEEKS_PER_MONTH))
    const maxMonths = Math.max(minMonths, Math.round(maxWeeks / WEEKS_PER_MONTH))
    if (minMonths === maxMonths) {
      return `≈ ${minMonths} ${pluralize(minMonths, 'month')}`
    }
    return `≈ ${minMonths}–${maxMonths} months`
  }

  if (maxWeeks <= 1 && maxDays <= DAYS_PER_WEEK) {
    const minDaysRounded = Math.max(1, minDays)
    const maxDaysRounded = Math.max(minDaysRounded, maxDays)
    if (minDaysRounded === maxDaysRounded) {
      return `≈ ${minDaysRounded} ${pluralize(minDaysRounded, 'day')}`
    }
    return `≈ ${minDaysRounded}–${maxDaysRounded} days`
  }

  if (minWeeks === maxWeeks) {
    return `≈ ${minWeeks} ${pluralize(minWeeks, 'week')}`
  }

  return `≈ ${minWeeks}–${maxWeeks} weeks`
}

export function summarizeStageEfforts(stageEfforts: string[]): string {
  const combined = combineEffortRanges(stageEfforts.map(parseEffortString))
  return formatEffortRange(combined)
}

export function getRemainingEffortSummary(
  stages: { id: string; estimatedEffort: string }[],
  completedStageIds: string[],
): { stagesLeft: number; effortLabel: string } {
  const remainingStages = stages.filter((stage) => !completedStageIds.includes(stage.id))
  return {
    stagesLeft: remainingStages.length,
    effortLabel: summarizeStageEfforts(remainingStages.map((stage) => stage.estimatedEffort)),
  }
}
