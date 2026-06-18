import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { CertificatePreviewDialog } from './CertificatePreviewDialog'
import type { CertificateSubmission } from '../../types/submissions'

const submission: CertificateSubmission = {
  approvalStatus: 'SUBMITTED',
  certificateDocument: {
    contentType: 'image/png',
    fileSizeBytes: 2048,
    id: 'document-1',
    originalFilename: 'certificate.png',
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

describe('CertificatePreviewDialog', () => {
  it('renders image preview and metadata', () => {
    const blob = new Blob(['image'], { type: 'image/png' })

    render(
      <CertificatePreviewDialog
        blob={blob}
        onClose={vi.fn()}
        onDownload={vi.fn()}
        open
        submission={submission}
      />,
    )

    expect(screen.getByRole('dialog', { name: 'Certificate preview' })).toBeInTheDocument()
    expect(screen.getByAltText('Certificate preview')).toBeInTheDocument()
    expect(screen.getByText('certificate.png')).toBeInTheDocument()
    expect(screen.getByText('PNG · 2 KB')).toBeInTheDocument()
  })

  it('calls close and download handlers', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    const onDownload = vi.fn()

    render(
      <CertificatePreviewDialog
        blob={new Blob(['image'], { type: 'image/png' })}
        onClose={onClose}
        onDownload={onDownload}
        open
        submission={submission}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Close' }))
    await user.click(screen.getByRole('button', { name: 'Download' }))

    expect(onClose).toHaveBeenCalled()
    expect(onDownload).toHaveBeenCalled()
  })
})
