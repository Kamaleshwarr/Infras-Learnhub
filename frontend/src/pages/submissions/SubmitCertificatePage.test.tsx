import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import { loadAllMySubmissions } from '../../api/loadAllMySubmissions'
import { submissionsApi } from '../../api/submissionsApi'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import { SubmitCertificatePage } from './SubmitCertificatePage'
import { MySubmissionsPage } from './MySubmissionsPage'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    list: vi.fn(),
  },
}))

vi.mock('../../api/loadAllMySubmissions', () => ({
  loadAllMySubmissions: vi.fn(),
}))

vi.mock('../../api/submissionsApi', () => ({
  submissionsApi: {
    submit: vi.fn(),
  },
}))

const initiative = {
  description: 'AWS certification program',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: '550E8400-E29B-41D4-A716-446655440001',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE' as const,
  title: 'AWS Certification',
}

const secondInitiative = {
  ...initiative,
  id: '550e8400-e29b-41d4-a716-446655440002',
  title: 'Azure Certification',
}

const existingSubmission = {
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
    id: '550E8400-E29B-41D4-A716-446655440001',
    status: 'ACTIVE' as const,
    title: 'AWS Certification',
  },
  submittedAtUtc: '2026-06-01T00:00:00Z',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

function renderSubmitPage(initialEntry = '/submissions/new') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<SubmitCertificatePage />} path="/submissions/new" />
        <Route element={<MySubmissionsPage />} path="/submissions" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('SubmitCertificatePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(initiativesApi.list).mockResolvedValue({
      content: [initiative, secondInitiative],
      first: true,
      last: true,
      page: 0,
      size: 100,
      sort: [],
      totalElements: 2,
      totalPages: 1,
    })
    vi.mocked(loadAllMySubmissions).mockResolvedValue([existingSubmission])
  })

  it('shows unsubmitted initiatives and disables already-submitted options', async () => {
    renderSubmitPage()

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    await userEvent.click(screen.getByRole('combobox', { name: /Initiative/i }))

    expect(screen.getByRole('option', { name: 'AWS Certification (already submitted)' })).toHaveAttribute(
      'aria-disabled',
      'true',
    )
    expect(screen.getByRole('option', { name: 'Azure Certification' })).toBeInTheDocument()
  })

  it('still shows initiatives when submission history cannot be loaded', async () => {
    vi.mocked(loadAllMySubmissions).mockRejectedValue(new Error('forbidden'))

    renderSubmitPage()

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    await userEvent.click(screen.getByRole('combobox', { name: /Initiative/i }))

    expect(screen.getByRole('option', { name: 'AWS Certification' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Azure Certification' })).toBeInTheDocument()
  })

  it('submits a certificate and redirects to My Submissions with success state', async () => {
    const user = userEvent.setup()
    vi.mocked(submissionsApi.submit).mockResolvedValue(existingSubmission)

    renderSubmitPage()

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    await user.click(screen.getByRole('combobox', { name: /Initiative/i }))
    await user.click(screen.getByRole('option', { name: 'Azure Certification' }))

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    const certificateFile = new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' })
    fireEvent.change(fileInput, { target: { files: [certificateFile] } })
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    await waitFor(() => expect(submissionsApi.submit).toHaveBeenCalled())
    const [initiativeId, formData] = vi.mocked(submissionsApi.submit).mock.calls[0]
    expect(initiativeId).toBe('550e8400-e29b-41d4-a716-446655440002')
    expect(formData.get('certificateFile')).toBe(certificateFile)

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: 'My Submissions' })).toBeInTheDocument(),
    )
  })

  it('shows an error when initiative loading fails', async () => {
    vi.mocked(initiativesApi.list).mockRejectedValue(new Error('network'))

    renderSubmitPage()

    await waitFor(() =>
      expect(screen.getByText(SUBMISSION_MESSAGES.initiativesLoadError)).toBeInTheDocument(),
    )
  })

  it('pre-selects initiative from initiativeId query parameter', async () => {
    renderSubmitPage(
      '/submissions/new?initiativeId=550e8400-e29b-41d4-a716-446655440002',
    )

    await waitFor(() =>
      expect(screen.getByRole('combobox', { name: /Initiative/i })).toHaveTextContent('Azure Certification'),
    )
  })

  it('does not pre-select initiative when query parameter matches an already-submitted initiative', async () => {
    renderSubmitPage(
      '/submissions/new?initiativeId=550E8400-E29B-41D4-A716-446655440001',
    )

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    expect(screen.getByRole('combobox', { name: /Initiative/i })).not.toHaveTextContent('AWS Certification')
  })

  it('does not pre-select initiative when query parameter is unknown', async () => {
    renderSubmitPage('/submissions/new?initiativeId=unknown-initiative-id')

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    expect(screen.getByRole('combobox', { name: /Initiative/i })).not.toHaveTextContent('AWS Certification')
    expect(screen.getByRole('combobox', { name: /Initiative/i })).not.toHaveTextContent('Azure Certification')
  })

  it('submits a pre-selected certificate and redirects to My Submissions', async () => {
    const user = userEvent.setup()
    vi.mocked(submissionsApi.submit).mockResolvedValue({
      ...existingSubmission,
      initiative: {
        id: '550e8400-e29b-41d4-a716-446655440002',
        status: 'ACTIVE',
        title: 'Azure Certification',
      },
    })

    renderSubmitPage(
      '/submissions/new?initiativeId=550e8400-e29b-41d4-a716-446655440002',
    )

    await waitFor(() =>
      expect(screen.getByRole('combobox', { name: /Initiative/i })).toHaveTextContent('Azure Certification'),
    )

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    const certificateFile = new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' })
    fireEvent.change(fileInput, { target: { files: [certificateFile] } })
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    await waitFor(() => expect(submissionsApi.submit).toHaveBeenCalled())
    const [initiativeId] = vi.mocked(submissionsApi.submit).mock.calls[0]
    expect(initiativeId).toBe('550e8400-e29b-41d4-a716-446655440002')

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: 'My Submissions' })).toBeInTheDocument(),
    )
  })

  it('shows an info message when every initiative has already been submitted', async () => {
    vi.mocked(loadAllMySubmissions).mockResolvedValue([
      existingSubmission,
      {
        ...existingSubmission,
        id: 'submission-2',
        initiative: {
          id: '550e8400-e29b-41d4-a716-446655440002',
          status: 'ACTIVE',
          title: 'Azure Certification',
        },
      },
    ])

    renderSubmitPage()

    await waitFor(() =>
      expect(screen.getByText(SUBMISSION_MESSAGES.allInitiativesSubmitted)).toBeInTheDocument(),
    )
    expect(screen.getByRole('button', { name: 'Submit certificate' })).toBeDisabled()
  })
})
