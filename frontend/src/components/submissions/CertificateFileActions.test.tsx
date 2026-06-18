import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { submissionsApi } from '../../api/submissionsApi'
import { CertificateFileActions } from './CertificateFileActions'
import type { CertificateSubmission } from '../../types/submissions'

vi.mock('../../api/submissionsApi', () => ({
  submissionsApi: {
    getCertificateBlob: vi.fn(),
  },
}))

vi.mock('../../utils/downloadBlob', () => ({
  downloadBlob: vi.fn(),
}))

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

describe('CertificateFileActions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders certificate metadata and actions', () => {
    render(<CertificateFileActions submission={submission} />)

    expect(screen.getByRole('button', { name: 'Preview' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Download' })).toBeInTheDocument()
  })

  it('opens preview dialog after loading inline certificate blob', async () => {
    const user = userEvent.setup()
    vi.mocked(submissionsApi.getCertificateBlob).mockResolvedValue({
      blob: new Blob(['certificate-content'], { type: 'application/pdf' }),
      contentType: 'application/pdf',
    })

    render(<CertificateFileActions submission={submission} />)

    await user.click(screen.getByRole('button', { name: 'Preview' }))

    await waitFor(() => expect(screen.getByRole('dialog', { name: 'Certificate preview' })).toBeInTheDocument())
    expect(submissionsApi.getCertificateBlob).toHaveBeenCalledWith('submission-1', { disposition: 'inline' })
    expect(screen.getByTitle('Certificate preview')).toBeInTheDocument()
    expect(screen.getByText('certificate.pdf')).toBeInTheDocument()
    expect(screen.getByText('PDF · 1 KB')).toBeInTheDocument()
  })

  it('downloads certificate using attachment disposition', async () => {
    const user = userEvent.setup()
    const { downloadBlob } = await import('../../utils/downloadBlob')
    const blob = new Blob(['certificate-content'], { type: 'application/pdf' })
    vi.mocked(submissionsApi.getCertificateBlob).mockResolvedValue({
      blob,
      contentType: 'application/pdf',
    })

    render(<CertificateFileActions submission={submission} />)

    await user.click(screen.getByRole('button', { name: 'Download' }))

    await waitFor(() =>
      expect(submissionsApi.getCertificateBlob).toHaveBeenCalledWith('submission-1', { disposition: 'attachment' }),
    )
    expect(downloadBlob).toHaveBeenCalledWith(blob, 'certificate.pdf')
  })
})
