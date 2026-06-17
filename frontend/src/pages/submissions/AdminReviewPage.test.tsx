import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { submissionsApi } from '../../api/submissionsApi'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import { AdminReviewPage } from './AdminReviewPage'

vi.mock('../../api/submissionsApi', () => ({
  submissionsApi: {
    listAll: vi.fn(),
    approve: vi.fn(),
    reject: vi.fn(),
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
    status: 'ACTIVE' as const,
    title: 'AWS Certification',
  },
  submittedAtUtc: '2026-06-01T12:00:00Z',
  updatedAtUtc: '2026-06-01T12:00:00Z',
}

function renderPage(initialEntry = '/submissions/review') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<AdminReviewPage />} path="/submissions/review" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('AdminReviewPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [submission],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })
  })

  it('loads pending submissions for admin review', async () => {
    renderPage()

    await waitFor(() => expect(screen.getByText('AWS Certification')).toBeInTheDocument())
    expect(submissionsApi.listAll).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: 'submittedAtUtc,desc',
      status: 'SUBMITTED',
    })
  })

  it('approves a submission and shows success snackbar', async () => {
    const user = userEvent.setup()
    vi.mocked(submissionsApi.approve).mockResolvedValue({
      ...submission,
      approvalStatus: 'APPROVED',
    })

    renderPage()

    await waitFor(() => expect(screen.getByText('Employee One')).toBeInTheDocument())
    await user.click(screen.getByRole('button', { name: 'Approve' }))
    await user.click(screen.getByRole('button', { name: 'Approve certificate' }))

    await waitFor(() => expect(submissionsApi.approve).toHaveBeenCalledWith('submission-1'))
    expect(await screen.findByText(SUBMISSION_MESSAGES.approveSuccess)).toBeInTheDocument()
    expect(submissionsApi.listAll).toHaveBeenCalledTimes(2)
  })

  it('rejects a submission with a reason and shows success snackbar', async () => {
    const user = userEvent.setup()
    vi.mocked(submissionsApi.reject).mockResolvedValue({
      ...submission,
      approvalStatus: 'REJECTED',
      rejectionReason: 'Certificate is not legible.',
    })

    renderPage()

    await waitFor(() => expect(screen.getByText('Employee One')).toBeInTheDocument())
    await user.click(screen.getByRole('button', { name: 'Reject' }))
    await user.type(screen.getByRole('textbox', { name: 'Rejection reason' }), 'Certificate is not legible.')
    await user.click(screen.getByRole('button', { name: 'Reject certificate' }))

    await waitFor(() =>
      expect(submissionsApi.reject).toHaveBeenCalledWith('submission-1', {
        rejectionReason: 'Certificate is not legible.',
      }),
    )
    expect(await screen.findByText(SUBMISSION_MESSAGES.rejectSuccess)).toBeInTheDocument()
  })

  it('shows an error when loading fails', async () => {
    vi.mocked(submissionsApi.listAll).mockRejectedValue(new Error('network'))

    renderPage()

    expect(await screen.findByText(SUBMISSION_MESSAGES.loadError)).toBeInTheDocument()
  })

  it('shows approve error and refreshes when approve fails', async () => {
    const user = userEvent.setup()
    vi.mocked(submissionsApi.approve).mockRejectedValue(
      Object.assign(new Error('bad request'), {
        isAxiosError: true,
        response: { data: { message: 'Only submitted certificate submissions can be reviewed' } },
      }),
    )
    vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

    renderPage()

    await waitFor(() => expect(screen.getByText('Employee One')).toBeInTheDocument())
    await user.click(screen.getByRole('button', { name: 'Approve' }))
    await user.click(screen.getByRole('button', { name: 'Approve certificate' }))

    expect(
      await screen.findByText('Only submitted certificate submissions can be reviewed'),
    ).toBeInTheDocument()
    expect(submissionsApi.listAll).toHaveBeenCalledTimes(2)
  })

  it('shows empty state when no pending submissions exist', async () => {
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })

    renderPage()

    expect(await screen.findByText('No certificate submissions awaiting review.')).toBeInTheDocument()
  })
})
