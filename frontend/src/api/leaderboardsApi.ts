import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  GlobalLeaderboardEntry,
  InitiativeLeaderboardEntry,
  PersonalLeaderboard,
} from '../types/leaderboards'

export type { GlobalLeaderboardEntry, InitiativeLeaderboardEntry, PersonalLeaderboard }

export const leaderboardsApi = {
  global: async (params?: { page?: number; size?: number; sort?: string }) => {
    const response = await httpClient.get<PageResponse<GlobalLeaderboardEntry>>('/leaderboards/global', { params })
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
