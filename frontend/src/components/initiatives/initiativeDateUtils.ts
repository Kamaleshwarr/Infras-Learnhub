const UTC_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/

export function isUtcDateInput(value: string) {
  return UTC_DATE_PATTERN.test(value)
}

export function instantToUtcDateInput(instant: string) {
  const parsed = Date.parse(instant)
  if (!Number.isFinite(parsed)) {
    return ''
  }

  const date = new Date(parsed)
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  const day = String(date.getUTCDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function utcDateInputToInstant(dateInput: string) {
  if (!isUtcDateInput(dateInput)) {
    return ''
  }

  return `${dateInput}T00:00:00.000Z`
}

export function todayUtcDateInput(now = Date.now()) {
  return instantToUtcDateInput(new Date(now).toISOString())
}

export function addUtcDays(dateInput: string, days: number) {
  if (!isUtcDateInput(dateInput)) {
    return ''
  }

  const [year, month, day] = dateInput.split('-').map(Number)
  const utcDate = new Date(Date.UTC(year, month - 1, day))
  utcDate.setUTCDate(utcDate.getUTCDate() + days)
  return instantToUtcDateInput(utcDate.toISOString())
}

export function defaultExpiryUtcDateInput(now = Date.now()) {
  return addUtcDays(todayUtcDateInput(now), 90)
}
