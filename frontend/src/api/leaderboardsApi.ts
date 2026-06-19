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

export interface InitiativeLeaderboardEntry {
  rank: number
  submissionId: string
  employee: {
    id: string
    employeeId?: string
    fullName: string
    email: string
  }
  initiativeId: string
  initiativeTitle: string
  submittedAtUtc: string
  approvedAtUtc: string
}

export interface PersonalLeaderboard {
  globalRank: number | null
  totalApprovedCertifications: number
  recentApprovals: Array<{
    submissionId: string
    initiativeTitle: string
    submittedAtUtc: string
    approvedAtUtc: string
  }>
}

export const leaderboardsApi = {
  global: async (params?: { page?: number; size?: number; sort?: string }) => {
    const response = await httpClient.get<PageResponse<LeaderboardEntry>>('/leaderboards/global', { params })
    return response.data
  },
  initiative: async (initiativeId: string, params?: { page?: number; size?: number; sort?: string }) => {
    const response = await httpClient.get<PageResponse<InitiativeLeaderboardEntry>>(
      `/leaderboards/initiatives/${initiativeId}`,
      { params },
    )
    return response.data
  },
  me: async () => {
    const response = await httpClient.get<PersonalLeaderboard>('/leaderboards/me')
    return response.data
  },
}
