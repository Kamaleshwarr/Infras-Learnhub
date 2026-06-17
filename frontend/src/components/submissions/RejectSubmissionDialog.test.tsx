import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { RejectSubmissionDialog } from './RejectSubmissionDialog'
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

describe('RejectSubmissionDialog', () => {
  it('requires a rejection reason before submit', async () => {
    const user = userEvent.setup({ delay: null })
    const onSubmit = vi.fn()

    render(<RejectSubmissionDialog onClose={vi.fn()} onSubmit={onSubmit} open submission={submission} />)

    expect(screen.getByRole('button', { name: 'Reject certificate' })).toBeDisabled()

    await user.type(screen.getByRole('textbox', { name: 'Rejection reason' }), 'Certificate is not legible.')
    await user.click(screen.getByRole('button', { name: 'Reject certificate' }))

    expect(onSubmit).toHaveBeenCalledWith('Certificate is not legible.')
  })

  it('keeps reject disabled for whitespace-only reason', async () => {
    const user = userEvent.setup({ delay: null })

    render(<RejectSubmissionDialog onClose={vi.fn()} onSubmit={vi.fn()} open submission={submission} />)

    await user.type(screen.getByRole('textbox', { name: 'Rejection reason' }), '   ')

    expect(screen.getByRole('button', { name: 'Reject certificate' })).toBeDisabled()
  })

  it('disables submit while submitting', () => {
    render(
      <RejectSubmissionDialog
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        open
        submission={submission}
        submitting
      />,
    )

    expect(screen.getByRole('progressbar')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeDisabled()
  })
})
