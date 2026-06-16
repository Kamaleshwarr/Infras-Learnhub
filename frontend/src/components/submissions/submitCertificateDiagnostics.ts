import type { PageResponse } from '../../types/api'
import type { InitiativeSummary } from '../../api/initiativesApi'
import type { CertificateSubmission } from '../../types/submissions'
import {
  extractSubmittedInitiativeIds,
  filterAvailableInitiatives,
  normalizeInitiativeId,
} from './submissionInitiativeFilter'

export const SUBMIT_CERTIFICATE_DIAGNOSTICS_FLAG = true

export interface InitiativeExclusionDiagnostic {
  initiativeId: string
  title: string
  reason: 'already_submitted'
}

export interface SubmitCertificateDiagnostics {
  capturedAtUtc: string
  initiativesRequest: {
    method: 'GET'
    path: '/api/v1/initiatives'
    params: Record<string, string | number | undefined>
  }
  submissionsRequest: {
    method: 'GET'
    path: '/api/v1/me/submissions'
    params: Record<string, string | number | undefined>
  }
  rawInitiativesResponse: PageResponse<InitiativeSummary> | { error: string }
  rawSubmissionsResponse: CertificateSubmission[] | { error: string }
  initiativesCount: number
  submissionsCount: number
  submittedInitiativeIds: string[]
  availableInitiativesCount: number
  availableInitiatives: Array<Pick<InitiativeSummary, 'id' | 'title' | 'status' | 'startDateUtc' | 'expiryDateUtc'>>
  excludedInitiatives: InitiativeExclusionDiagnostic[]
  notes: string[]
}

export function buildSubmitCertificateDiagnostics(input: {
  initiativeParams: Record<string, string | number | undefined>
  submissionParams: Record<string, string | number | undefined>
  rawInitiativesResponse: PageResponse<InitiativeSummary> | { error: string }
  rawSubmissionsResponse: CertificateSubmission[] | { error: string }
  initiatives: InitiativeSummary[]
  submissions: CertificateSubmission[]
}): SubmitCertificateDiagnostics {
  const submittedInitiativeIds = extractSubmittedInitiativeIds(input.submissions)
  const availableInitiatives = filterAvailableInitiatives(input.initiatives, input.submissions)
  const excludedInitiatives = input.initiatives
    .filter((initiative) => submittedInitiativeIds.has(normalizeInitiativeId(initiative.id)))
    .map((initiative) => ({
      initiativeId: initiative.id,
      title: initiative.title,
      reason: 'already_submitted' as const,
    }))

  const notes: string[] = [
    'Employee GET /initiatives is server-filtered to ACTIVE initiatives within the UTC start/expiry window.',
    'Admin Swagger GET /initiatives?status=ACTIVE does not apply the employee date window.',
    'No additional status or date filtering is applied in the frontend.',
  ]

  if ('error' in input.rawInitiativesResponse) {
    notes.push(`Initiatives request failed: ${input.rawInitiativesResponse.error}`)
  } else if (input.initiatives.length === 0) {
    notes.push('Initiatives response content is empty for the authenticated employee.')
  }

  if ('error' in input.rawSubmissionsResponse) {
    notes.push(`Submissions request failed: ${input.rawSubmissionsResponse.error}`)
  }

  if (input.initiatives.length > 0 && availableInitiatives.length === 0) {
    notes.push('Every loaded initiative was excluded because a submission already exists for it.')
  }

  return {
    capturedAtUtc: new Date().toISOString(),
    initiativesRequest: {
      method: 'GET',
      path: '/api/v1/initiatives',
      params: input.initiativeParams,
    },
    submissionsRequest: {
      method: 'GET',
      path: '/api/v1/me/submissions',
      params: input.submissionParams,
    },
    rawInitiativesResponse: input.rawInitiativesResponse,
    rawSubmissionsResponse: input.rawSubmissionsResponse,
    initiativesCount: input.initiatives.length,
    submissionsCount: input.submissions.length,
    submittedInitiativeIds: [...submittedInitiativeIds],
    availableInitiativesCount: availableInitiatives.length,
    availableInitiatives: availableInitiatives.map((initiative) => ({
      id: initiative.id,
      title: initiative.title,
      status: initiative.status,
      startDateUtc: initiative.startDateUtc,
      expiryDateUtc: initiative.expiryDateUtc,
    })),
    excludedInitiatives,
    notes,
  }
}

export function logSubmitCertificateDiagnostics(diagnostics: SubmitCertificateDiagnostics) {
  const label = '[SubmitCertificateDiagnostics]'
  console.group(label)
  console.log('1. GET /api/v1/initiatives response', diagnostics.rawInitiativesResponse)
  console.log('2. GET /api/v1/me/submissions response', diagnostics.rawSubmissionsResponse)
  console.log('3. availableInitiatives.length', diagnostics.availableInitiativesCount)
  console.log('4. submittedInitiativeIds', diagnostics.submittedInitiativeIds)
  console.log('5. final initiative list after filtering', diagnostics.availableInitiatives)
  console.log('6. excluded initiatives', diagnostics.excludedInitiatives)
  console.log('notes', diagnostics.notes)
  console.log('full diagnostics', diagnostics)
  console.groupEnd()
}
