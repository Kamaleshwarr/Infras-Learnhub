import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'

export interface LeaderboardEntry {
  rank: number
  employee: {
    id: string
    fullName: string
    email: string
  }
  totalApprovedCertifications?: number
  submittedAtUtc?: string
  approvedAtUtc?: string
}

export const leaderboardsApi = {
  global: async () => {
    const response = await httpClient.get<PageResponse<LeaderboardEntry>>('/leaderboards/global')
    return response.data
  },
  initiative: async (initiativeId: string) => {
    const response = await httpClient.get<PageResponse<LeaderboardEntry>>(
      `/leaderboards/initiatives/${initiativeId}`,
    )
    return response.data
  },
  me: async () => {
    const response = await httpClient.get('/leaderboards/me')
    return response.data
  },
}
