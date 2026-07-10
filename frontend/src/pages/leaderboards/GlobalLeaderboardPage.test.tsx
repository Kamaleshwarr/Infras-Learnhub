import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { leaderboardsApi } from '../../api/leaderboardsApi'
import { useAuth } from '../../auth/useAuth'
import { GlobalLeaderboardPage } from './GlobalLeaderboardPage'

vi.mock('../../api/leaderboardsApi', () => ({
  leaderboardsApi: {
    global: vi.fn(),
    me: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(),
}))

const employee = {
  email: 'employee@example.com',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  id: 'employee-1',
}

const globalEntry = {
  earliestSubmittedAtUtc: '2026-06-01T00:00:00Z',
  employee,
  latestApprovedAtUtc: '2026-06-05T00:00:00Z',
  rank: 1,
  totalApprovedCertifications: 2,
}

const pageResponse = {
  content: [globalEntry],
  first: true,
  last: true,
  page: 0,
  size: 20,
  sort: [],
  totalElements: 1,
  totalPages: 1,
}

describe('GlobalLeaderboardPage', () => {
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
        email: employee.email,
        employeeId: employee.employeeId,
        fullName: employee.fullName,
        id: employee.id,
        mustChangePassword: false,
        roles: ['EMPLOYEE'],
      },
    })
    vi.mocked(leaderboardsApi.global).mockResolvedValue(pageResponse)
    vi.mocked(leaderboardsApi.me).mockResolvedValue({
      earliestSubmittedAtUtc: '2026-06-01T00:00:00Z',
      employee,
      globalRank: 1,
      recentApprovals: [],
      totalApprovedCertifications: 2,
    })
  })

  it('renders populated global leaderboard', async () => {
    render(
      <MemoryRouter>
        <GlobalLeaderboardPage />
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Global Leaderboard' })).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getAllByText('Employee One').length).toBeGreaterThan(0)
    })
    expect(screen.getAllByText('#1').length).toBeGreaterThan(0)
    expect(screen.getByText('Top Performers')).toBeInTheDocument()
  })

  it('renders empty state when no rankings exist', async () => {
    vi.mocked(leaderboardsApi.global).mockResolvedValue({
      ...pageResponse,
      content: [],
      totalElements: 0,
    })
    vi.mocked(leaderboardsApi.me).mockResolvedValue({
      earliestSubmittedAtUtc: null,
      employee,
      globalRank: null,
      recentApprovals: [],
      totalApprovedCertifications: 0,
    })

    render(
      <MemoryRouter>
        <GlobalLeaderboardPage />
      </MemoryRouter>,
    )

    expect(
      (await screen.findAllByText(
        'No approved certifications yet. Complete and get certifications approved to appear here.',
      )).length,
    ).toBeGreaterThan(0)
    expect(screen.getByText('Not ranked yet')).toBeInTheDocument()
  })

  it('renders error state', async () => {
    vi.mocked(leaderboardsApi.global).mockRejectedValue(new Error('failed'))

    render(
      <MemoryRouter>
        <GlobalLeaderboardPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('Unable to load the global leaderboard. Please refresh or try again later.')).toBeInTheDocument()
  })
})
