import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'

export interface InitiativeSummary {
  id: string
  title: string
  description: string
  rewardDescription?: string
  startDateUtc: string
  expiryDateUtc: string
  status: 'DRAFT' | 'ACTIVE' | 'EXPIRED'
}

export interface InitiativeListParams {
  page?: number
  size?: number
  sort?: string
  status?: InitiativeSummary['status']
  search?: string
}

export const initiativesApi = {
  list: async (params?: InitiativeListParams) => {
    const response = await httpClient.get<PageResponse<InitiativeSummary>>('/initiatives', { params })
    return response.data
  },
  get: async (initiativeId: string) => {
    const response = await httpClient.get<InitiativeSummary>(`/initiatives/${initiativeId}`)
    return response.data
  },
}
