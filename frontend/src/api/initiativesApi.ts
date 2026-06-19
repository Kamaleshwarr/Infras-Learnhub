import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type { Initiative, InitiativeListParams } from '../types/initiatives'

export type { Initiative, InitiativeListParams, InitiativeSummary } from '../types/initiatives'

export const initiativesApi = {
  list: async (params?: InitiativeListParams) => {
    const response = await httpClient.get<PageResponse<Initiative>>('/initiatives', { params })
    return response.data
  },
  get: async (initiativeId: string) => {
    const response = await httpClient.get<Initiative>(`/initiatives/${initiativeId}`)
    return response.data
  },
}
