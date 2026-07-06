import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  CreateProjectPayload,
  ProjectDetail,
  ProjectMember,
  ProjectMemberPayload,
  ProjectStatus,
  ProjectSummary,
  ProjectAccessType,
  UpdateProjectPayload,
} from '../types/projects'

export type { ProjectSummary, ProjectDetail } from '../types/projects'

export interface ProjectListParams {
  search?: string
  status?: ProjectStatus
  accessType?: ProjectAccessType
  assigned?: boolean
  includeArchived?: boolean
  page?: number
  size?: number
  sort?: string
}

export const projectsApi = {
  list: async (params?: ProjectListParams) => {
    const response = await httpClient.get<PageResponse<ProjectSummary>>('/projects', { params })
    return response.data
  },

  get: async (projectId: string) => {
    const response = await httpClient.get<ProjectDetail>(`/projects/${projectId}`)
    return response.data
  },

  create: async (payload: CreateProjectPayload) => {
    const response = await httpClient.post<ProjectDetail>('/projects', payload)
    return response.data
  },

  update: async (projectId: string, payload: UpdateProjectPayload) => {
    const response = await httpClient.put<ProjectDetail>(`/projects/${projectId}`, payload)
    return response.data
  },

  archive: async (projectId: string) => {
    const response = await httpClient.post<ProjectDetail>(`/projects/${projectId}/archive`)
    return response.data
  },

  listMembers: async (projectId: string) => {
    const response = await httpClient.get<ProjectMember[]>(`/projects/${projectId}/members`)
    return response.data
  },

  addOrUpdateMember: async (projectId: string, payload: ProjectMemberPayload) => {
    const response = await httpClient.post<ProjectMember>(`/projects/${projectId}/members`, payload)
    return response.data
  },

  removeMember: async (projectId: string, userId: string) => {
    await httpClient.delete(`/projects/${projectId}/members/${userId}`)
  },
}
