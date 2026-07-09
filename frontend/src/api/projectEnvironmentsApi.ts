import { httpClient } from './httpClient'
import type {
  ProjectEnvironment,
  ProjectEnvironmentPayload,
  ProjectEnvironmentReference,
  ProjectEnvironmentReferencePayload,
} from '../types/projectOperational'

export const projectEnvironmentsApi = {
  list: async (projectId: string, params?: { search?: string; includeInactive?: boolean }) => {
    const response = await httpClient.get<ProjectEnvironment[]>(`/projects/${projectId}/environments`, { params })
    return response.data
  },

  get: async (projectId: string, environmentId: string) => {
    const response = await httpClient.get<ProjectEnvironment>(`/projects/${projectId}/environments/${environmentId}`)
    return response.data
  },

  create: async (projectId: string, payload: ProjectEnvironmentPayload) => {
    const response = await httpClient.post<ProjectEnvironment>(`/projects/${projectId}/environments`, payload)
    return response.data
  },

  update: async (projectId: string, environmentId: string, payload: ProjectEnvironmentPayload) => {
    const response = await httpClient.put<ProjectEnvironment>(
      `/projects/${projectId}/environments/${environmentId}`,
      payload,
    )
    return response.data
  },

  remove: async (projectId: string, environmentId: string) => {
    await httpClient.delete(`/projects/${projectId}/environments/${environmentId}`)
  },

  createReference: async (
    projectId: string,
    environmentId: string,
    payload: ProjectEnvironmentReferencePayload,
  ) => {
    const response = await httpClient.post<ProjectEnvironmentReference>(
      `/projects/${projectId}/environments/${environmentId}/references`,
      payload,
    )
    return response.data
  },

  updateReference: async (
    projectId: string,
    environmentId: string,
    referenceId: string,
    payload: ProjectEnvironmentReferencePayload,
  ) => {
    const response = await httpClient.put<ProjectEnvironmentReference>(
      `/projects/${projectId}/environments/${environmentId}/references/${referenceId}`,
      payload,
    )
    return response.data
  },

  removeReference: async (projectId: string, environmentId: string, referenceId: string) => {
    await httpClient.delete(`/projects/${projectId}/environments/${environmentId}/references/${referenceId}`)
  },
}
