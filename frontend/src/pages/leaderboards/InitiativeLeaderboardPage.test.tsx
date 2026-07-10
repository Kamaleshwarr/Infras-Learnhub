import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import { leaderboardsApi } from '../../api/leaderboardsApi'
import { useAuth } from '../../auth/useAuth'
import { InitiativeLeaderboardPage } from './InitiativeLeaderboardPage'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    get: vi.fn(),
    list: vi.fn(),
  },
}))

vi.mock('../../api/leaderboardsApi', () => ({
  leaderboardsApi: {
    initiative: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(),
}))

const initiative = {
  description: 'AWS certification',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-123',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE' as const,
  title: 'AWS Certification',
}

const entry = {
  approvedAtUtc: '2026-06-05T00:00:00Z',
  employee: {
    email: 'employee@example.com',
    employeeId: 'EMP001',
    fullName: 'Employee One',
    id: 'employee-1',
  },
  initiativeId: initiative.id,
  initiativeTitle: initiative.title,
  rank: 1,
  submissionId: 'submission-1',
  submittedAtUtc: '2026-06-01T00:00:00Z',
}

function renderRoutes(initialEntry: string) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<InitiativeLeaderboardPage />} path="/leaderboards/initiatives" />
        <Route element={<InitiativeLeaderboardPage />} path="/leaderboards/initiatives/:initiativeId" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('InitiativeLeaderboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'EMPLOYEE',
      hasRole: () => true,
      isAdmin: false,
      isAuthenticated: true,
      isEmployee: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: {
        email: 'employee@example.com',
        employeeId: 'EMP001',
        fullName: 'Employee One',
        id: 'employee-1',
        mustChangePassword: false,
        roles: ['EMPLOYEE'],
      },
    })
  })

  it('renders initiative leaderboard heading from route params', () => {
    renderRoutes('/leaderboards/initiatives/initiative-123')
    expect(screen.getByRole('heading', { name: 'Initiative Leaderboard' })).toBeInTheDocument()
  })

  it('renders initiative selector when no initiative id is provided', async () => {
    vi.mocked(initiativesApi.list).mockResolvedValue({
      content: [initiative],
      first: true,
      last: true,
      page: 0,
      size: 100,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })

    renderRoutes('/leaderboards/initiatives')

    expect(await screen.findByLabelText('Learning initiative')).toBeInTheDocument()
    expect(screen.getByText('Select an initiative to view its leaderboard.')).toBeInTheDocument()
  })

  it('renders initiative leaderboard rows for a selected initiative', async () => {
    vi.mocked(initiativesApi.get).mockResolvedValue(initiative)
    vi.mocked(leaderboardsApi.initiative).mockResolvedValue({
      content: [entry],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })

    renderRoutes('/leaderboards/initiatives/initiative-123')

    expect(await screen.findByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText(/Ranking is based on certification submission time/i)).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getAllByText('Employee One').length).toBeGreaterThan(0)
    })
  })

  it('navigates when an initiative is selected from the picker', async () => {
    const user = userEvent.setup()
    vi.mocked(initiativesApi.list).mockResolvedValue({
      content: [initiative],
      first: true,
      last: true,
      page: 0,
      size: 100,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })
    vi.mocked(initiativesApi.get).mockResolvedValue(initiative)
    vi.mocked(leaderboardsApi.initiative).mockResolvedValue({
      content: [entry],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    })

    renderRoutes('/leaderboards/initiatives')

    await user.click(await screen.findByLabelText('Learning initiative'))
    await user.click(screen.getByRole('option', { name: 'AWS Certification' }))

    expect(await screen.findByText('AWS Certification')).toBeInTheDocument()
  })
})
