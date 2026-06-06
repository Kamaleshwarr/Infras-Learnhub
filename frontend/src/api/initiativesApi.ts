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

export const initiativesApi = {
  list: async () => {
    const response = await httpClient.get<PageResponse<InitiativeSummary>>('/initiatives')
    return response.data
  },
  get: async (initiativeId: string) => {
    const response = await httpClient.get<InitiativeSummary>(`/initiatives/${initiativeId}`)
    return response.data
  },
}
