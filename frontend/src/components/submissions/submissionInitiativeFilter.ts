import type { InitiativeSummary } from '../../api/initiativesApi'
import type { CertificateSubmission } from '../../types/submissions'

export function normalizeInitiativeId(id: string) {
  return id.trim().toLowerCase()
}

export function extractSubmittedInitiativeIds(submissions: CertificateSubmission[]) {
  const submittedIds = new Set<string>()

  for (const submission of submissions) {
    const initiativeId = submission.initiative?.id
    if (typeof initiativeId === 'string' && initiativeId.trim().length > 0) {
      submittedIds.add(normalizeInitiativeId(initiativeId))
    }
  }

  return submittedIds
}

export function filterAvailableInitiatives(
  initiatives: InitiativeSummary[],
  submissions: CertificateSubmission[],
) {
  const submittedInitiativeIds = extractSubmittedInitiativeIds(submissions)

  return initiatives.filter(
    (initiative) => !submittedInitiativeIds.has(normalizeInitiativeId(initiative.id)),
  )
}

export function parseInitiativeSummaries(content: InitiativeSummary[] | undefined | null) {
  if (!Array.isArray(content)) {
    return []
  }

  return content.filter(
    (initiative): initiative is InitiativeSummary =>
      Boolean(initiative?.id) && Boolean(initiative?.title),
  )
}

function compareByExpiryDateUtcAsc(left: InitiativeSummary, right: InitiativeSummary) {
  const leftExpiry = Date.parse(left.expiryDateUtc)
  const rightExpiry = Date.parse(right.expiryDateUtc)

  if (Number.isFinite(leftExpiry) && Number.isFinite(rightExpiry) && leftExpiry !== rightExpiry) {
    return leftExpiry - rightExpiry
  }

  return left.title.localeCompare(right.title)
}

export function sortInitiativesForSubmitDropdown(
  initiatives: InitiativeSummary[],
  submittedInitiativeIds: Set<string>,
) {
  const available: InitiativeSummary[] = []
  const submitted: InitiativeSummary[] = []

  for (const initiative of initiatives) {
    if (submittedInitiativeIds.has(normalizeInitiativeId(initiative.id))) {
      submitted.push(initiative)
    } else {
      available.push(initiative)
    }
  }

  available.sort(compareByExpiryDateUtcAsc)
  submitted.sort(compareByExpiryDateUtcAsc)

  return [...available, ...submitted]
}
