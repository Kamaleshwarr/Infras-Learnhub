import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  KnowledgeCategory,
  KnowledgeSourceType,
  ProjectFolderPayload,
  ProjectKnowledgeFolder,
  ProjectKnowledgeItem,
  ProjectKnowledgeItemUpdatePayload,
  ProjectKnowledgeLinkPayload,
  ProjectLinkAccessResponse,
} from '../types/projectKnowledge'

export interface KnowledgeFolderListParams {
  parentId?: string
  search?: string
  page?: number
  size?: number
  sort?: string
}

export interface KnowledgeItemListParams {
  folderId?: string
  category?: KnowledgeCategory
  sourceType?: KnowledgeSourceType
  search?: string
  page?: number
  size?: number
  sort?: string
}

export const projectKnowledgeApi = {
  getFolder: async (projectId: string, folderId: string) => {
    const response = await httpClient.get<ProjectKnowledgeFolder>(`/projects/${projectId}/folders/${folderId}`)
    return response.data
  },

  listFolders: async (projectId: string, params?: KnowledgeFolderListParams) => {
    const response = await httpClient.get<PageResponse<ProjectKnowledgeFolder>>(`/projects/${projectId}/folders`, {
      params,
    })
    return response.data
  },

  createFolder: async (projectId: string, payload: ProjectFolderPayload) => {
    const response = await httpClient.post<ProjectKnowledgeFolder>(`/projects/${projectId}/folders`, payload)
    return response.data
  },

  updateFolder: async (projectId: string, folderId: string, payload: ProjectFolderPayload) => {
    const response = await httpClient.put<ProjectKnowledgeFolder>(
      `/projects/${projectId}/folders/${folderId}`,
      payload,
    )
    return response.data
  },

  deleteFolder: async (projectId: string, folderId: string) => {
    await httpClient.delete(`/projects/${projectId}/folders/${folderId}`)
  },

  listItems: async (projectId: string, params?: KnowledgeItemListParams) => {
    const response = await httpClient.get<PageResponse<ProjectKnowledgeItem>>(`/projects/${projectId}/items`, {
      params,
    })
    return response.data
  },

  getItem: async (projectId: string, itemId: string) => {
    const response = await httpClient.get<ProjectKnowledgeItem>(`/projects/${projectId}/items/${itemId}`)
    return response.data
  },

  createLink: async (projectId: string, payload: ProjectKnowledgeLinkPayload) => {
    const response = await httpClient.post<ProjectKnowledgeItem>(`/projects/${projectId}/items/links`, payload)
    return response.data
  },

  updateItem: async (projectId: string, itemId: string, payload: ProjectKnowledgeItemUpdatePayload) => {
    const response = await httpClient.put<ProjectKnowledgeItem>(`/projects/${projectId}/items/${itemId}`, payload)
    return response.data
  },

  deleteItem: async (projectId: string, itemId: string) => {
    await httpClient.delete(`/projects/${projectId}/items/${itemId}`)
  },

  accessLink: async (projectId: string, itemId: string) => {
    const response = await httpClient.get<ProjectLinkAccessResponse>(`/projects/${projectId}/items/${itemId}/link`)
    return response.data
  },
}
