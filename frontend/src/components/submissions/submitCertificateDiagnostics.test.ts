import { describe, expect, it } from 'vitest'
import type { InitiativeSummary } from '../../api/initiativesApi'
import type { CertificateSubmission } from '../../types/submissions'
import { buildSubmitCertificateDiagnostics, parseInitiativesContent } from './submitCertificateDiagnostics'

const initiative: InitiativeSummary = {
  description: 'AWS certification program',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: '550e8400-e29b-41d4-a716-446655440001',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE',
  title: 'AWS Certification',
}

const submission: CertificateSubmission = {
  approvalStatus: 'SUBMITTED',
  certificateDocument: {
    contentType: 'application/pdf',
    fileSizeBytes: 1024,
    id: 'document-1',
    originalFilename: 'certificate.pdf',
  },
  certificateDocumentId: 'document-1',
  createdAtUtc: '2026-06-01T00:00:00Z',
  employee: {
    email: 'employee@example.com',
    employeeId: 'EMP001',
    fullName: 'Employee One',
    id: 'employee-1',
  },
  id: 'submission-1',
  initiative: {
    id: '550e8400-e29b-41d4-a716-446655440001',
    status: 'ACTIVE',
    title: 'AWS Certification',
  },
  submittedAtUtc: '2026-06-01T00:00:00Z',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('buildSubmitCertificateDiagnostics', () => {
  it('captures raw responses, filtering output, and exclusion reasons', () => {
    const diagnostics = buildSubmitCertificateDiagnostics({
      initiativeParams: { size: 100, status: 'ACTIVE', sort: 'expiryDateUtc,asc' },
      submissionParams: { page: 0, size: 100, sort: 'submittedAtUtc,desc' },
      rawInitiativesResponse: {
        content: [initiative],
        first: true,
        last: true,
        page: 0,
        size: 100,
        sort: [],
        totalElements: 1,
        totalPages: 1,
      },
      rawSubmissionsResponse: [submission],
      initiatives: [initiative],
      submissions: [submission],
    })

    expect(diagnostics.initiativesCount).toBe(1)
    expect(diagnostics.submissionsCount).toBe(1)
    expect(diagnostics.submittedInitiativeIds).toEqual(['550e8400-e29b-41d4-a716-446655440001'])
    expect(diagnostics.availableInitiativesCount).toBe(0)
    expect(diagnostics.excludedInitiatives).toEqual([
      {
        initiativeId: initiative.id,
        title: initiative.title,
        reason: 'already_submitted',
      },
    ])
  })

  it('records parse exclusions when initiative records are missing required fields', () => {
    const diagnostics = buildSubmitCertificateDiagnostics({
      initiativeParams: { size: 100, status: 'ACTIVE', sort: 'expiryDateUtc,asc' },
      submissionParams: { page: 0, size: 100, sort: 'submittedAtUtc,desc' },
      rawInitiativesResponse: {
        content: [{ title: 'Missing id' } as InitiativeSummary],
        first: true,
        last: true,
        page: 0,
        size: 100,
        sort: [],
        totalElements: 1,
        totalPages: 1,
      },
      rawSubmissionsResponse: [],
      initiatives: [],
      submissions: [],
      parseExclusions: parseInitiativesContent([{ title: 'Missing id' } as InitiativeSummary]).exclusions,
      rawInitiativesContentCount: 1,
    })

    expect(diagnostics.initiativesCount).toBe(0)
    expect(diagnostics.rawInitiativesContentCount).toBe(1)
    expect(diagnostics.parseExclusions).toEqual([
      {
        index: 0,
        reason: 'missing_id',
        record: { title: 'Missing id' },
      },
    ])
  })
})
