import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import { leaderboardsApi } from '../../api/leaderboardsApi'
import { submissionsApi } from '../../api/submissionsApi'
import { useAuth } from '../../auth/useAuth'
import { InitiativeDetailPage } from './InitiativeDetailPage'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    get: vi.fn(),
  },
}))

vi.mock('../../api/leaderboardsApi', () => ({
  leaderboardsApi: {
    initiative: vi.fn(),
  },
}))

vi.mock('../../api/submissionsApi', () => ({
  submissionsApi: {
    listMine: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(),
}))

const initiative = {
  description: 'Complete the AWS Cloud Practitioner exam and submit your certificate.',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  rewardDescription: '$500 learning credit',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE' as const,
  title: 'AWS Certification',
}

const topLearnerEntry = {
  approvedAtUtc: '2026-06-05T00:00:00Z',
  employee: {
    email: 'jane@example.com',
    fullName: 'Jane Smith',
    id: 'employee-2',
  },
  initiativeId: 'initiative-1',
  initiativeTitle: 'AWS Certification',
  rank: 1,
  submissionId: 'submission-2',
  submittedAtUtc: '2026-06-01T00:00:00Z',
}

function renderDetailPage(path = '/initiatives/initiative-1') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route element={<InitiativeDetailPage />} path="/initiatives/:initiativeId" />
        <Route element={<div>Initiatives List</div>} path="/initiatives" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('InitiativeDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'EMPLOYEE',
      hasRole: (role) => role === 'EMPLOYEE',
      isAdmin: false,
      isAuthenticated: true,
      isEmployee: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: null,
    })
    vi.mocked(initiativesApi.get).mockResolvedValue(initiative)
    vi.mocked(leaderboardsApi.initiative).mockResolvedValue({
      content: [topLearnerEntry],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })
    vi.mocked(submissionsApi.listMine).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
  })

  it('shows back to initiatives navigation', async () => {
    renderDetailPage()

    const backLink = await screen.findByRole('link', { name: /Back to Initiatives/i })
    expect(backLink).toHaveAttribute('href', '/initiatives')
  })

  it('renders initiative details from direct URL access', async () => {
    renderDetailPage()

    expect(await screen.findByRole('heading', { name: 'AWS Certification' })).toBeInTheDocument()
    expect(screen.getByText(/Complete the AWS Cloud Practitioner exam/i)).toBeInTheDocument()
    expect(screen.getByText('$500 learning credit')).toBeInTheDocument()
    expect(screen.getByText('Active')).toBeInTheDocument()
    expect(screen.getByText(/\(UTC\)/)).toBeInTheDocument()
  })

  it('shows top learner when leaderboard data is available', async () => {
    renderDetailPage()

    expect(await screen.findByText(/#1 Jane Smith/i)).toBeInTheDocument()
  })

  it('shows submit certificate CTA when employee has no submission', async () => {
    renderDetailPage()

    const submitLink = await screen.findByRole('link', { name: /Submit Certificate/i })
    expect(submitLink).toHaveAttribute('href', '/submissions/new?initiativeId=initiative-1')
  })

  it('shows view my submission when employee already submitted', async () => {
    vi.mocked(submissionsApi.listMine).mockResolvedValue({
      content: [
        {
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
          submittedAtUtc: '2026-06-01T00:00:00Z',
          updatedAtUtc: '2026-06-01T00:00:00Z',
        },
      ],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })

    renderDetailPage()

    expect(await screen.findByRole('link', { name: /View My Submission/i })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Submit Certificate/i })).not.toBeInTheDocument()
  })

  it('shows rejected helper text without resubmit CTA', async () => {
    vi.mocked(submissionsApi.listMine).mockResolvedValue({
      content: [
        {
          approvalStatus: 'REJECTED',
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
          submittedAtUtc: '2026-06-01T00:00:00Z',
          updatedAtUtc: '2026-06-01T00:00:00Z',
        },
      ],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })

    renderDetailPage()

    expect(await screen.findByText(/Contact your administrator for next steps/i)).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Submit Certificate/i })).not.toBeInTheDocument()
  })

  it('shows not found panel for 404 responses', async () => {
    vi.mocked(initiativesApi.get).mockRejectedValue(
      new axios.AxiosError('Not found', 'ERR_BAD_REQUEST', undefined, undefined, {
        status: 404,
        data: { message: 'Learning initiative was not found' },
      } as never),
    )

    renderDetailPage('/initiatives/missing-id')

    expect(await screen.findByText(/Initiative unavailable/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Browse Initiatives/i })).toHaveAttribute('href', '/initiatives')
  })

  it('shows retry on primary load failure', async () => {
    vi.mocked(initiativesApi.get).mockRejectedValue(new Error('network'))

    renderDetailPage()

    expect(await screen.findByText(/Unable to load initiative details/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Retry/i })).toBeInTheDocument()
  })

  it('keeps initiative content when secondary submission load fails', async () => {
    vi.mocked(submissionsApi.listMine).mockRejectedValue(new Error('submission failed'))

    renderDetailPage()

    expect(await screen.findByRole('heading', { name: 'AWS Certification' })).toBeInTheDocument()
    expect(screen.getByText(/Unable to load your submission status/i)).toBeInTheDocument()
  })

  it('shows top learner empty state when no approvals exist', async () => {
    vi.mocked(leaderboardsApi.initiative).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })

    renderDetailPage()

    expect(await screen.findByText(/No completions yet/i)).toBeInTheDocument()
  })

  it('hides employee progress and actions for admin viewers', async () => {
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'ADMIN',
      hasRole: (role) => role === 'ADMIN',
      isAdmin: true,
      isAuthenticated: true,
      isEmployee: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: null,
    })

    renderDetailPage()

    expect(await screen.findByRole('heading', { name: 'AWS Certification' })).toBeInTheDocument()
    expect(screen.queryByText('My Progress')).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Submit Certificate/i })).not.toBeInTheDocument()
    expect(submissionsApi.listMine).not.toHaveBeenCalled()
  })

  it('retries primary load when retry is clicked', async () => {
    vi.mocked(initiativesApi.get)
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValueOnce(initiative)

    renderDetailPage()

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: /Retry/i }))

    expect(await screen.findByRole('heading', { name: 'AWS Certification' })).toBeInTheDocument()
    expect(initiativesApi.get).toHaveBeenCalledTimes(2)
  })
})
