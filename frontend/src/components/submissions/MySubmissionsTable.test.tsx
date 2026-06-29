import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { MySubmissionsTable } from './MySubmissionsTable'
import type { CertificateSubmission } from '../../types/submissions'

const submission: CertificateSubmission = {
  approvalStatus: 'REJECTED',
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
    id: 'initiative-1',
    status: 'ACTIVE',
    title: 'AWS Certification',
  },
  rejectionReason: 'Name mismatch',
  reviewedAtUtc: '2026-06-02T10:00:00Z',
  submittedAtUtc: '2026-06-01T12:00:00Z',
  updatedAtUtc: '2026-06-02T10:00:00Z',
}

describe('MySubmissionsTable', () => {
  it('renders submission rows with status and rejection reason', () => {
    render(<MySubmissionsTable emptyMessage="No submissions" loading={false} submissions={[submission]} />)

    expect(screen.getByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText('Rejected')).toBeInTheDocument()
    expect(screen.getByText('certificate.pdf')).toBeInTheDocument()
    expect(screen.getByText('Name mismatch')).toBeInTheDocument()
  })

  it('renders empty state when there are no submissions', () => {
    render(<MySubmissionsTable emptyMessage="No certificate submissions yet." loading={false} submissions={[]} />)

    expect(screen.getByText('No certificate submissions yet.')).toBeInTheDocument()
  })

  it('truncates long initiative and rejection reason text', () => {
    render(
      <MySubmissionsTable
        emptyMessage="No submissions"
        loading={false}
        submissions={[
          {
            ...submission,
            certificateDocument: {
              ...submission.certificateDocument,
              originalFilename: `${'f'.repeat(80)}.pdf`,
            },
            initiative: {
              ...submission.initiative,
              title: 't'.repeat(80),
            },
            rejectionReason: 'r'.repeat(100),
          },
        ]}
      />,
    )

    expect(screen.getByText(`${'t'.repeat(60)}…`)).toBeInTheDocument()
    expect(screen.getByText(`${'f'.repeat(50)}…`)).toBeInTheDocument()
    expect(screen.getByText(`${'r'.repeat(80)}…`)).toBeInTheDocument()
  })
})
