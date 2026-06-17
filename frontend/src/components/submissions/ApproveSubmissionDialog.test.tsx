import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ApproveSubmissionDialog } from './ApproveSubmissionDialog'
import type { CertificateSubmission } from '../../types/submissions'

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
    id: 'initiative-1',
    status: 'ACTIVE',
    title: 'AWS Certification',
  },
  submittedAtUtc: '2026-06-01T12:00:00Z',
  updatedAtUtc: '2026-06-01T12:00:00Z',
}

describe('ApproveSubmissionDialog', () => {
  it('renders submission summary and confirms approve', async () => {
    const user = userEvent.setup()
    const onConfirm = vi.fn()

    render(
      <ApproveSubmissionDialog onClose={vi.fn()} onConfirm={onConfirm} open submission={submission} />,
    )

    expect(screen.getByText('Employee One')).toBeInTheDocument()
    expect(screen.getByText(/Initiative: AWS Certification/)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Approve certificate' }))
    expect(onConfirm).toHaveBeenCalledTimes(1)
  })

  it('disables actions while submitting', () => {
    render(
      <ApproveSubmissionDialog
        onClose={vi.fn()}
        onConfirm={vi.fn()}
        open
        submission={submission}
        submitting
      />,
    )

    expect(screen.getByRole('progressbar')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeDisabled()
  })

  it('shows API error when provided', () => {
    render(
      <ApproveSubmissionDialog
        error="Only submitted certificate submissions can be reviewed"
        onClose={vi.fn()}
        onConfirm={vi.fn()}
        open
        submission={submission}
      />,
    )

    expect(screen.getByText('Only submitted certificate submissions can be reviewed')).toBeInTheDocument()
  })
})
