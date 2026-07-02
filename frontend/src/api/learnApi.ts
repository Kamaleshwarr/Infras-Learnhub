import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  Technology,
  TechnologyCreateRequest,
  TechnologyListParams,
  TechnologyUpdateRequest,
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
  createTechnology: async (request: TechnologyCreateRequest) => {
    const response = await httpClient.post<Technology>('/learn/manage/technologies', request)
    return response.data
  },
  updateTechnology: async (technologyId: string, request: TechnologyUpdateRequest) => {
    const response = await httpClient.put<Technology>(`/learn/manage/technologies/${technologyId}`, request)
    return response.data
  },
  publishTechnology: async (technologyId: string) => {
    const response = await httpClient.post<Technology>(`/learn/manage/technologies/${technologyId}/publish`)
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
