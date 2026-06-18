import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { submissionsApi } from './submissionsApi'
import type { CertificateSubmission } from '../types/submissions'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
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
  comments: 'Completed exam',
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
  submittedAtUtc: '2026-06-01T00:00:00Z',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('submissionsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('lists current user submissions', async () => {
    const responseData = {
      content: [submission],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: responseData })

    const result = await submissionsApi.listMine({ status: 'SUBMITTED', page: 0, size: 20 })

    expect(httpClient.get).toHaveBeenCalledWith('/me/submissions', {
      params: { status: 'SUBMITTED', page: 0, size: 20 },
    })
    expect(result).toEqual(responseData)
  })

  it('lists all submissions for admin review', async () => {
    const responseData = {
      content: [submission],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: responseData })

    const result = await submissionsApi.listAll({ status: 'SUBMITTED', sort: 'submittedAtUtc,desc' })

    expect(httpClient.get).toHaveBeenCalledWith('/submissions', {
      params: { status: 'SUBMITTED', sort: 'submittedAtUtc,desc' },
    })
    expect(result).toEqual(responseData)
  })

  it('gets a submission by id', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: submission })

    const result = await submissionsApi.getById('submission-1')

    expect(httpClient.get).toHaveBeenCalledWith('/submissions/submission-1')
    expect(result).toEqual(submission)
  })

  it('submits a certificate for an initiative', async () => {
    const formData = new FormData()
    vi.mocked(httpClient.post).mockResolvedValue({ data: submission })

    const result = await submissionsApi.submit('initiative-1', formData)

    expect(httpClient.post).toHaveBeenCalledWith('/initiatives/initiative-1/submissions', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    expect(result).toEqual(submission)
  })

  it('approves a submission', async () => {
    const approvedSubmission = { ...submission, approvalStatus: 'APPROVED' as const }
    vi.mocked(httpClient.post).mockResolvedValue({ data: approvedSubmission })

    const result = await submissionsApi.approve('submission-1')

    expect(httpClient.post).toHaveBeenCalledWith('/submissions/submission-1/approve')
    expect(result).toEqual(approvedSubmission)
  })

  it('rejects a submission with a reason', async () => {
    const rejectedSubmission = {
      ...submission,
      approvalStatus: 'REJECTED' as const,
      rejectionReason: 'Certificate is not legible.',
    }
    vi.mocked(httpClient.post).mockResolvedValue({ data: rejectedSubmission })

    const result = await submissionsApi.reject('submission-1', {
      rejectionReason: 'Certificate is not legible.',
    })

    expect(httpClient.post).toHaveBeenCalledWith('/submissions/submission-1/reject', {
      rejectionReason: 'Certificate is not legible.',
    })
    expect(result).toEqual(rejectedSubmission)
  })

  it('downloads certificate blob with disposition', async () => {
    const blob = new Blob(['certificate-content'], { type: 'application/pdf' })
    vi.mocked(httpClient.get).mockResolvedValue({
      data: blob,
      headers: { 'content-type': 'application/pdf' },
    })

    const result = await submissionsApi.getCertificateBlob('submission-1', { disposition: 'inline' })

    expect(httpClient.get).toHaveBeenCalledWith('/submissions/submission-1/certificate', {
      params: { disposition: 'inline' },
      responseType: 'blob',
    })
    expect(result).toEqual({ blob, contentType: 'application/pdf' })
  })
})
