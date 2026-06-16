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

export interface InitiativeParseExclusionDiagnostic {
  index: number
  reason: 'missing_id' | 'missing_title'
  record: unknown
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
  rawInitiativesContentCount: number
  parseExclusions: InitiativeParseExclusionDiagnostic[]
  initiativesCount: number
  submissionsCount: number
  submittedInitiativeIds: string[]
  availableInitiativesCount: number
  availableInitiatives: Array<Pick<InitiativeSummary, 'id' | 'title' | 'status' | 'startDateUtc' | 'expiryDateUtc'>>
  excludedInitiatives: InitiativeExclusionDiagnostic[]
  notes: string[]
}

export function parseInitiativesContent(content: InitiativeSummary[] | undefined | null) {
  const exclusions: InitiativeParseExclusionDiagnostic[] = []
  const initiatives: InitiativeSummary[] = []

  if (!Array.isArray(content)) {
    return {
      exclusions,
      initiatives,
      rawContentCount: 0,
    }
  }

  content.forEach((initiative, index) => {
    if (!initiative?.id) {
      exclusions.push({ index, reason: 'missing_id', record: initiative })
      return
    }

    if (!initiative?.title) {
      exclusions.push({ index, reason: 'missing_title', record: initiative })
      return
    }

    initiatives.push(initiative)
  })

  return {
    exclusions,
    initiatives,
    rawContentCount: content.length,
  }
}

export function buildSubmitCertificateDiagnostics(input: {
  initiativeParams: Record<string, string | number | undefined>
  submissionParams: Record<string, string | number | undefined>
  rawInitiativesResponse: PageResponse<InitiativeSummary> | { error: string }
  rawSubmissionsResponse: CertificateSubmission[] | { error: string }
  initiatives: InitiativeSummary[]
  submissions: CertificateSubmission[]
  parseExclusions?: InitiativeParseExclusionDiagnostic[]
  rawInitiativesContentCount?: number
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

  const rawInitiativesContentCount =
    input.rawInitiativesContentCount ??
    ('error' in input.rawInitiativesResponse ? 0 : (input.rawInitiativesResponse.content?.length ?? 0))
  const parseExclusions = input.parseExclusions ?? []

  if ('error' in input.rawInitiativesResponse) {
    notes.push(`Initiatives request failed: ${input.rawInitiativesResponse.error}`)
  } else if (rawInitiativesContentCount === 0) {
    notes.push('Initiatives response content is empty for the authenticated employee.')
    notes.push(
      'Compare with admin Swagger: employee GET /initiatives only returns ACTIVE initiatives whose UTC start date is today or earlier and expiry date is today or later.',
    )
  } else if (input.initiatives.length === 0 && parseExclusions.length > 0) {
    notes.push('Every initiative record in the response was dropped during frontend parsing.')
  }

  if ('error' in input.rawSubmissionsResponse) {
    notes.push(`Submissions request failed: ${input.rawSubmissionsResponse.error}`)
  }

  if (input.initiatives.length > 0 && availableInitiatives.length === 0) {
    notes.push('Every loaded initiative was excluded because a submission already exists for it.')
    notes.push('Disabled initiatives should still appear in the dropdown when initiativesCount > 0.')
  }

  for (const exclusion of parseExclusions) {
    notes.push(`Parse exclusion at content[${exclusion.index}]: ${exclusion.reason}`)
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
    rawInitiativesContentCount,
    parseExclusions,
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
