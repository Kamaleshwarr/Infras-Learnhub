import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { leaderboardsApi } from './leaderboardsApi'
import type { InitiativeLeaderboardEntry } from './leaderboardsApi'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
  },
}))

const entry: InitiativeLeaderboardEntry = {
  approvedAtUtc: '2026-06-05T00:00:00Z',
  employee: {
    email: 'learner@example.com',
    employeeId: 'EMP002',
    fullName: 'Jane Smith',
    id: 'employee-2',
  },
  initiativeId: 'initiative-1',
  initiativeTitle: 'AWS Certification',
  rank: 1,
  submissionId: 'submission-1',
  submittedAtUtc: '2026-06-01T00:00:00Z',
}

describe('leaderboardsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetches initiative leaderboard entries', async () => {
    const responseData = {
      content: [entry],
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
      params: {
        page: 0,
        size: 5,
        sort: 'rank,asc',
      },
    })
    expect(result.content[0]).toEqual(entry)
  })
})
