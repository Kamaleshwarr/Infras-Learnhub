export const EXPIRING_SOON_DAYS = 14
export const EXPIRING_CRITICAL_DAYS = 3

export function daysUntilExpiry(expiryDateUtc: string, now = Date.now()) {
  const expiry = Date.parse(expiryDateUtc)
  if (!Number.isFinite(expiry)) {
    return null
  }

  const msPerDay = 24 * 60 * 60 * 1000
  return Math.ceil((expiry - now) / msPerDay)
}

export function isExpiringSoon(expiryDateUtc: string, withinDays = EXPIRING_SOON_DAYS, now = Date.now()) {
  const expiry = Date.parse(expiryDateUtc)
  if (!Number.isFinite(expiry) || expiry < now) {
    return false
  }

  const threshold = now + withinDays * 24 * 60 * 60 * 1000
  return expiry <= threshold
}

export function isExpiringCritical(expiryDateUtc: string, now = Date.now()) {
  return isExpiringSoon(expiryDateUtc, EXPIRING_CRITICAL_DAYS, now)
}

export function formatInitiativeDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}

export function formatInitiativeDateRange(startDateUtc: string, expiryDateUtc: string) {
  return `${formatInitiativeDate(startDateUtc)} – ${formatInitiativeDate(expiryDateUtc)} (UTC)`
}

export function truncateText(text: string, maxLength: number) {
  if (text.length <= maxLength) {
    return text
  }

  return `${text.slice(0, maxLength).trimEnd()}…`
}

export const INITIATIVE_LIST_TRUNCATION = {
  tableTitle: 60,
  tableReward: 60,
  cardTitle: 80,
  cardReward: 80,
} as const
