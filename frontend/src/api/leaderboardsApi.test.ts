import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { leaderboardsApi } from './leaderboardsApi'
import type { GlobalLeaderboardEntry, InitiativeLeaderboardEntry, PersonalLeaderboard } from '../types/leaderboards'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
  },
}))

const employee = {
  email: 'learner@example.com',
  employeeId: 'EMP002',
  fullName: 'Jane Smith',
  id: 'employee-2',
}

const initiativeEntry: InitiativeLeaderboardEntry = {
  approvedAtUtc: '2026-06-05T00:00:00Z',
  employee,
  initiativeId: 'initiative-1',
  initiativeTitle: 'AWS Certification',
  rank: 1,
  submissionId: 'submission-1',
  submittedAtUtc: '2026-06-01T00:00:00Z',
}

const globalEntry: GlobalLeaderboardEntry = {
  earliestSubmittedAtUtc: '2026-06-01T00:00:00Z',
  employee,
  latestApprovedAtUtc: '2026-06-05T00:00:00Z',
  rank: 1,
  totalApprovedCertifications: 3,
}

const personalLeaderboard: PersonalLeaderboard = {
  earliestSubmittedAtUtc: '2026-06-01T00:00:00Z',
  employee,
  globalRank: 2,
  recentApprovals: [],
  totalApprovedCertifications: 3,
}

describe('leaderboardsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetches global leaderboard entries', async () => {
    const responseData = {
      content: [globalEntry],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: responseData })

    const result = await leaderboardsApi.global({ page: 0, size: 20, sort: 'rank,asc' })

    expect(httpClient.get).toHaveBeenCalledWith('/leaderboards/global', {
      params: { page: 0, size: 20, sort: 'rank,asc' },
    })
    expect(result.content[0]).toEqual(globalEntry)
  })

  it('fetches initiative leaderboard entries', async () => {
    const responseData = {
      content: [initiativeEntry],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: responseData })

    const result = await leaderboardsApi.initiative('initiative-1', {
      page: 0,
      size: 5,
      sort: 'rank,asc',
    })

    expect(httpClient.get).toHaveBeenCalledWith('/leaderboards/initiatives/initiative-1', {
      params: { page: 0, size: 5, sort: 'rank,asc' },
    })
    expect(result.content[0]).toEqual(initiativeEntry)
  })

  it('fetches personal leaderboard response', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: personalLeaderboard })

    const result = await leaderboardsApi.me()

    expect(httpClient.get).toHaveBeenCalledWith('/leaderboards/me')
    expect(result).toEqual(personalLeaderboard)
  })
})
