import { httpClient } from './httpClient'
import type {
  ProjectLinkedRepository,
  ProjectLinkedRepositoryPayload,
  RepositoryProvider,
  RepositoryType,
} from '../types/projectOperational'

export const projectRepositoriesApi = {
  list: async (
    projectId: string,
    params?: {
      search?: string
      repositoryType?: RepositoryType
      provider?: RepositoryProvider
      includeInactive?: boolean
    },
  ) => {
    const response = await httpClient.get<ProjectLinkedRepository[]>(`/projects/${projectId}/repositories`, { params })
    return response.data
  },

  get: async (projectId: string, repositoryId: string) => {
    const response = await httpClient.get<ProjectLinkedRepository>(`/projects/${projectId}/repositories/${repositoryId}`)
    return response.data
  },

  create: async (projectId: string, payload: ProjectLinkedRepositoryPayload) => {
    const response = await httpClient.post<ProjectLinkedRepository>(`/projects/${projectId}/repositories`, payload)
    return response.data
  },

  update: async (projectId: string, repositoryId: string, payload: ProjectLinkedRepositoryPayload) => {
    const response = await httpClient.put<ProjectLinkedRepository>(
      `/projects/${projectId}/repositories/${repositoryId}`,
      payload,
    )
    return response.data
  },

  remove: async (projectId: string, repositoryId: string) => {
    await httpClient.delete(`/projects/${projectId}/repositories/${repositoryId}`)
  },
}
