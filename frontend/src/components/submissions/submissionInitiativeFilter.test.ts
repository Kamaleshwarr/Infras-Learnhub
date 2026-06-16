import { describe, expect, it } from 'vitest'
import type { InitiativeSummary } from '../../api/initiativesApi'
import type { CertificateSubmission } from '../../types/submissions'
import {
  extractSubmittedInitiativeIds,
  filterAvailableInitiatives,
  normalizeInitiativeId,
} from './submissionInitiativeFilter'

const initiatives: InitiativeSummary[] = [
  {
    description: 'AWS certification program',
    expiryDateUtc: '2026-12-31T00:00:00Z',
    id: '550E8400-E29B-41D4-A716-446655440001',
    startDateUtc: '2026-01-01T00:00:00Z',
    status: 'ACTIVE',
    title: 'AWS Certification',
  },
  {
    description: 'Azure certification program',
    expiryDateUtc: '2026-12-31T00:00:00Z',
    id: '550e8400-e29b-41d4-a716-446655440002',
    startDateUtc: '2026-01-01T00:00:00Z',
    status: 'ACTIVE',
    title: 'Azure Certification',
  },
]

function submissionForInitiative(
  initiative: InitiativeSummary,
  overrides: Partial<CertificateSubmission> = {},
): CertificateSubmission {
  return {
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
      id: initiative.id,
      status: initiative.status,
      title: initiative.title,
    },
    submittedAtUtc: '2026-06-01T00:00:00Z',
    updatedAtUtc: '2026-06-01T00:00:00Z',
    ...overrides,
  }
}

describe('submissionInitiativeFilter', () => {
  it('normalizes initiative ids for stable comparisons', () => {
    expect(normalizeInitiativeId(' 550E8400-E29B-41D4-A716-446655440001 ')).toBe(
      '550e8400-e29b-41d4-a716-446655440001',
    )
  })

  it('extracts submitted initiative ids while ignoring malformed submissions', () => {
    const ids = extractSubmittedInitiativeIds([
      submissionForInitiative(initiatives[0]),
      submissionForInitiative(initiatives[1], {
        initiative: undefined,
      }),
    ])

    expect(ids).toEqual(new Set(['550e8400-e29b-41d4-a716-446655440001']))
  })

  it('filters only initiatives that already have submissions', () => {
    const available = filterAvailableInitiatives(initiatives, [submissionForInitiative(initiatives[0])])

    expect(available).toEqual([initiatives[1]])
  })

  it('matches initiative and submission ids case-insensitively', () => {
    const available = filterAvailableInitiatives(initiatives, [
      submissionForInitiative({
        ...initiatives[0],
        id: '550e8400-e29b-41d4-a716-446655440001',
      }),
    ])

    expect(available).toEqual([initiatives[1]])
  })

  it('does not exclude initiatives when submissions reference other initiatives', () => {
    const available = filterAvailableInitiatives(initiatives, [
      submissionForInitiative({
        ...initiatives[0],
        id: 'initiative-3',
        title: 'GCP Certification',
      }),
    ])

    expect(available).toEqual(initiatives)
  })
})
