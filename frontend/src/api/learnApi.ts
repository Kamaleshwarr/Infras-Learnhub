import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  CatalogStatus,
  Technology,
  TechnologyCurationRequest,
  TechnologyListParams,
} from '../types/learn'
import type { Roadmap } from '../types/roadmap'
import type { Enrollment, Journey, TechnologyProgress } from '../types/progress'

export const learnApi = {
  listTechnologies: async (params?: TechnologyListParams) => {
    const response = await httpClient.get<PageResponse<Technology>>('/learn/technologies', { params })
    return response.data
  },
  getTechnology: async (technologyId: string) => {
    const response = await httpClient.get<Technology>(`/learn/technologies/${technologyId}`)
    return response.data
  },
  getRoadmapByTechnologyId: async (technologyId: string) => {
    const response = await httpClient.get<Roadmap>(`/learn/technologies/id/${technologyId}/roadmap`)
    return response.data
  },
  getRoadmapBySlug: async (slug: string) => {
    const response = await httpClient.get<Roadmap>(`/learn/technologies/${slug}/roadmap`)
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
  enrollInTechnology: async (technologyId: string) => {
    const response = await httpClient.post<Enrollment>('/learn/enrollments', { technologyId })
    return response.data
  },
  getJourney: async () => {
    const response = await httpClient.get<Journey>('/learn/journey')
    return response.data
  },
  getTechnologyProgress: async (technologyId: string) => {
    const response = await httpClient.get<TechnologyProgress>(`/learn/progress/technologies/${technologyId}`)
    return response.data
  },
  getActiveEnrollment: async (technologyId: string) => {
    const response = await httpClient.get<Enrollment>(`/learn/enrollments/technologies/${technologyId}`)
    return response.data
  },
  completeStage: async (enrollmentId: string, stageId: string) => {
    const response = await httpClient.post<Enrollment>(`/learn/enrollments/${enrollmentId}/complete-stage`, {
      stageId,
    })
    return response.data
  },
  leaveEnrollment: async (enrollmentId: string) => {
    await httpClient.delete(`/learn/enrollments/${enrollmentId}`)
  },
}
