import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'

export interface ProjectSummary {
  id: string
  name: string
  description?: string
  accessType: 'PUBLIC' | 'MEMBERS_ONLY'
  archived: boolean
}

export interface ProjectKnowledgeItem {
  id: string
  title: string
  category: string
  sourceType: 'FILE' | 'LINK'
  accessCount: number
}

export const projectsApi = {
  list: async (search?: string) => {
    const response = await httpClient.get<PageResponse<ProjectSummary>>('/projects', {
      params: { search },
    })
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
