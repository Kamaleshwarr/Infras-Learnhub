import { httpClient } from './httpClient'
import type {
  ProjectExternalContact,
  ProjectExternalContactPayload,
  ProjectMemberCandidate,
} from '../types/projectTeam'

export const projectTeamApi = {
  searchMemberCandidates: async (projectId: string, search: string) => {
    const response = await httpClient.get<ProjectMemberCandidate[]>(
      `/projects/${projectId}/member-candidates`,
      { params: { search } },
    )
    return response.data
  },

  listExternalContacts: async (projectId: string) => {
    const response = await httpClient.get<ProjectExternalContact[]>(`/projects/${projectId}/contacts`)
    return response.data
  },

  createExternalContact: async (projectId: string, payload: ProjectExternalContactPayload) => {
    const response = await httpClient.post<ProjectExternalContact>(`/projects/${projectId}/contacts`, payload)
    return response.data
  },

  updateExternalContact: async (
    projectId: string,
    contactId: string,
    payload: ProjectExternalContactPayload,
  ) => {
    const response = await httpClient.put<ProjectExternalContact>(
      `/projects/${projectId}/contacts/${contactId}`,
      payload,
    )
    return response.data
  },

  deleteExternalContact: async (projectId: string, contactId: string) => {
    await httpClient.delete(`/projects/${projectId}/contacts/${contactId}`)
  },
}
