import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { MyProgressCard } from './MyProgressCard'

const submission = {
  approvalStatus: 'APPROVED' as const,
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
    status: 'ACTIVE' as const,
    title: 'AWS Certification',
  },
  submittedAtUtc: '2026-06-01T00:00:00Z',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('MyProgressCard', () => {
  it('renders not submitted state', () => {
    render(<MyProgressCard error={null} loading={false} submission={null} />)
    expect(screen.getByText('Not submitted')).toBeInTheDocument()
  })

  it('renders submission status', () => {
    render(<MyProgressCard error={null} loading={false} submission={submission} />)
    expect(screen.getByText('Approved')).toBeInTheDocument()
  })

  it('renders load error', () => {
    render(<MyProgressCard error="Unable to load" loading={false} submission={null} />)
    expect(screen.getByText('Unable to load')).toBeInTheDocument()
  })
})
