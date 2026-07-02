import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type { RelatedTechnologySummary } from '../types/learn'

export interface ProjectSummary {
  id: string
  name: string
  description?: string
  accessType: 'PUBLIC' | 'MEMBERS_ONLY'
  archived: boolean
}

export interface ProjectDetail extends ProjectSummary {
  relatedTechnologies?: RelatedTechnologySummary[]
}

export interface ProjectKnowledgeItem {
  id: string
  title: string
  category: string
  sourceType: 'FILE' | 'LINK'
  accessCount: number
}

export const projectsApi = {
  list: async (search?: string, params?: { page?: number; size?: number; sort?: string }) => {
    const response = await httpClient.get<PageResponse<ProjectSummary>>('/projects', {
      params: { search, ...params },
    })
    return response.data
  },
  get: async (projectId: string) => {
    const response = await httpClient.get<ProjectDetail>(`/projects/${projectId}`)
    return response.data
  },
  knowledgeItems: async (projectId: string, search?: string) => {
    const response = await httpClient.get<PageResponse<ProjectKnowledgeItem>>(
      `/projects/${projectId}/items`,
      { params: { search } },
    )
    return response.data
  },
}
