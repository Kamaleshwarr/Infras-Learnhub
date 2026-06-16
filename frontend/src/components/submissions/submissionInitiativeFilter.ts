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
