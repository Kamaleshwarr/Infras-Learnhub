import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { AdminReviewTable } from './AdminReviewTable'
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
  comments: 'Completed the course last week.',
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

describe('AdminReviewTable', () => {
  it('renders pending submissions with action buttons', () => {
    const onApprove = vi.fn()
    const onReject = vi.fn()

    render(
      <AdminReviewTable
        emptyMessage="No pending submissions"
        loading={false}
        onApprove={onApprove}
        onReject={onReject}
        submissions={[submission]}
      />,
    )

    expect(screen.getByText('Employee One')).toBeInTheDocument()
    expect(screen.getByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText('certificate.pdf')).toBeInTheDocument()
    expect(screen.getByText('Completed the course last week.')).toBeInTheDocument()
  })

  it('calls approve and reject handlers', async () => {
    const user = userEvent.setup()
    const onApprove = vi.fn()
    const onReject = vi.fn()

    render(
      <AdminReviewTable
        emptyMessage="No pending submissions"
        loading={false}
        onApprove={onApprove}
        onReject={onReject}
        submissions={[submission]}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Approve' }))
    expect(onApprove).toHaveBeenCalledWith(submission)

    await user.click(screen.getByRole('button', { name: 'Reject' }))
    expect(onReject).toHaveBeenCalledWith(submission)
  })

  it('shows empty message when there are no submissions', () => {
    render(
      <AdminReviewTable
        emptyMessage="No certificate submissions awaiting review."
        loading={false}
        onApprove={vi.fn()}
        onReject={vi.fn()}
        submissions={[]}
      />,
    )

    expect(screen.getByText('No certificate submissions awaiting review.')).toBeInTheDocument()
  })

  it('shows loading indicator', () => {
    render(
      <AdminReviewTable
        emptyMessage="No pending submissions"
        loading
        onApprove={vi.fn()}
        onReject={vi.fn()}
        submissions={[]}
      />,
    )

    expect(screen.getByLabelText('Loading pending submissions')).toBeInTheDocument()
  })
})
