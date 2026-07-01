import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  CreateInitiativeRequest,
  Initiative,
  InitiativeListParams,
  UpdateInitiativeRequest,
} from '../types/initiatives'

export type {
  CreateInitiativeRequest,
  Initiative,
  InitiativeListParams,
  InitiativeSummary,
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
}
