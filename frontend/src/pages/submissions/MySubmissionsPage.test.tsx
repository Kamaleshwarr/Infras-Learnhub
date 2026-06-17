import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { submissionsApi } from '../../api/submissionsApi'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import { MySubmissionsPage } from './MySubmissionsPage'

vi.mock('../../api/submissionsApi', () => ({
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
  submittedAtUtc: '2026-06-01T12:00:00Z',
  updatedAtUtc: '2026-06-01T12:00:00Z',
}

function renderPage(initialEntry: string | { pathname: string; state?: unknown } = '/submissions') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<MySubmissionsPage />} path="/submissions" />
        <Route element={<div>Submit page</div>} path="/submissions/new" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('MySubmissionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(submissionsApi.listMine).mockResolvedValue({
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

  it('loads and renders employee submissions', async () => {
    renderPage()

    await waitFor(() => expect(screen.getByText('AWS Certification')).toBeInTheDocument())
    expect(screen.getByText('certificate.pdf')).toBeInTheDocument()
    expect(submissionsApi.listMine).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: 'submittedAtUtc,desc',
    })
  })

  it('filters submissions by status in the URL and API params', async () => {
    const user = userEvent.setup()
    renderPage()

    await waitFor(() => expect(screen.getByText('AWS Certification')).toBeInTheDocument())
    await user.click(screen.getByRole('tab', { name: 'Approved' }))

    await waitFor(() =>
      expect(submissionsApi.listMine).toHaveBeenLastCalledWith({
        page: 0,
        size: 20,
        sort: 'submittedAtUtc,desc',
        status: 'APPROVED',
      }),
    )
  })

  it('shows success notification from route state after submit redirect', async () => {
    renderPage({
      pathname: '/submissions',
      state: {
        submissionNotification: {
          message: SUBMISSION_MESSAGES.submitSuccess,
          severity: 'success',
        },
      },
    } as never)

    expect(await screen.findByText(SUBMISSION_MESSAGES.submitSuccess)).toBeInTheDocument()
  })

  it('links to Submit Certificate page', async () => {
    renderPage()

    expect(screen.getByRole('link', { name: 'Submit certificate' })).toHaveAttribute('href', '/submissions/new')
  })

  it('shows an error when loading fails', async () => {
    vi.mocked(submissionsApi.listMine).mockRejectedValue(new Error('network'))

    renderPage()

    expect(await screen.findByText(SUBMISSION_MESSAGES.loadError)).toBeInTheDocument()
  })
})
