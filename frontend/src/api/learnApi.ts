import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  CatalogStatus,
  Technology,
  TechnologyCurationRequest,
  TechnologyListParams,
} from '../types/learn'

export const learnApi = {
  listTechnologies: async (params?: TechnologyListParams) => {
    const response = await httpClient.get<PageResponse<Technology>>('/learn/technologies', { params })
    return response.data
  },
  getTechnology: async (technologyId: string) => {
    const response = await httpClient.get<Technology>(`/learn/technologies/${technologyId}`)
    return response.data
  },
  listManageTechnologies: async (params?: TechnologyListParams) => {
    const response = await httpClient.get<PageResponse<Technology>>('/learn/manage/technologies', { params })
    return response.data
  },
  getCatalogStatus: async () => {
    const response = await httpClient.get<CatalogStatus>('/learn/manage/catalog/status')
    return response.data
  },
  updateTechnologyCuration: async (technologyId: string, request: TechnologyCurationRequest) => {
    const response = await httpClient.patch<Technology>(
      `/learn/manage/technologies/${technologyId}/curation`,
      request,
    )
    return response.data
  },
  publishTechnology: async (technologyId: string) => {
    const response = await httpClient.post<Technology>(`/learn/manage/technologies/${technologyId}/publish`)
    return response.data
  },
  hideTechnology: async (technologyId: string) => {
    const response = await httpClient.post<Technology>(`/learn/manage/technologies/${technologyId}/hide`)
    return response.data
  },
  archiveTechnology: async (technologyId: string) => {
    const response = await httpClient.post<Technology>(`/learn/manage/technologies/${technologyId}/archive`)
    return response.data
  },
  addProjectLink: async (technologyId: string, projectId: string) => {
    const response = await httpClient.post<Technology>(
      `/learn/manage/technologies/${technologyId}/project-links`,
      { projectId },
    )
    return response.data
  },
  removeProjectLink: async (technologyId: string, projectId: string) => {
    const response = await httpClient.delete<Technology>(
      `/learn/manage/technologies/${technologyId}/project-links/${projectId}`,
    )
    return response.data
  },
}
