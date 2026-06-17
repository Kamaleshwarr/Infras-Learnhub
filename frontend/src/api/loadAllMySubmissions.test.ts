import { beforeEach, describe, expect, it, vi } from 'vitest'
import { submissionsApi } from './submissionsApi'
import { loadAllMySubmissions } from './loadAllMySubmissions'

vi.mock('./submissionsApi', () => ({
  submissionsApi: {
    listMine: vi.fn(),
  },
}))

const submission = {
  approvalStatus: 'SUBMITTED' as const,
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

describe('loadAllMySubmissions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads every page of submissions', async () => {
    vi.mocked(submissionsApi.listMine)
      .mockResolvedValueOnce({
        content: [submission],
        first: true,
        last: false,
        page: 0,
        size: 100,
        sort: [],
        totalElements: 2,
        totalPages: 2,
      })
      .mockResolvedValueOnce({
        content: [{ ...submission, id: 'submission-2' }],
        first: false,
        last: true,
        page: 1,
        size: 100,
        sort: [],
        totalElements: 2,
        totalPages: 2,
      })

    const result = await loadAllMySubmissions()

    expect(submissionsApi.listMine).toHaveBeenCalledTimes(2)
    expect(result).toHaveLength(2)
  })
})
