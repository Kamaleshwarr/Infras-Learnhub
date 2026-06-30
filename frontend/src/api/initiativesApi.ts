import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  CreateInitiativeRequest,
  Initiative,
  InitiativeListParams,
  ReactivateInitiativeRequest,
  UpdateInitiativeRequest,
} from '../types/initiatives'

export type {
  CreateInitiativeRequest,
  Initiative,
  InitiativeListParams,
  InitiativeSummary,
  ReactivateInitiativeRequest,
  UpdateInitiativeRequest,
} from '../types/initiatives'

export const initiativesApi = {
  list: async (params?: InitiativeListParams) => {
    const response = await httpClient.get<PageResponse<Initiative>>('/initiatives', { params })
    return response.data
  },
  get: async (initiativeId: string) => {
    const response = await httpClient.get<Initiative>(`/initiatives/${initiativeId}`)
    return response.data
  },
  create: async (request: CreateInitiativeRequest) => {
    const response = await httpClient.post<Initiative>('/initiatives', request)
    return response.data
  },
  update: async (initiativeId: string, request: UpdateInitiativeRequest) => {
    const response = await httpClient.put<Initiative>(`/initiatives/${initiativeId}`, request)
    return response.data
  },
  delete: async (initiativeId: string) => {
    await httpClient.delete(`/initiatives/${initiativeId}`)
  },
  publish: async (initiativeId: string) => {
    const response = await httpClient.post<Initiative>(`/initiatives/${initiativeId}/publish`)
    return response.data
  },
  returnToDraft: async (initiativeId: string) => {
    const response = await httpClient.post<Initiative>(`/initiatives/${initiativeId}/return-to-draft`)
    return response.data
  },
  markExpired: async (initiativeId: string) => {
    const response = await httpClient.post<Initiative>(`/initiatives/${initiativeId}/mark-expired`)
    return response.data
  },
  reactivate: async (initiativeId: string, request: ReactivateInitiativeRequest) => {
    const response = await httpClient.post<Initiative>(`/initiatives/${initiativeId}/reactivate`, request)
    return response.data
  },
}
