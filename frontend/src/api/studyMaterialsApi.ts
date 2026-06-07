import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'

export interface StudyMaterial {
  id: string
  title: string
  description?: string
  materialType: 'PDF' | 'PPT' | 'DOCX' | 'VIDEO_LINK' | 'EXTERNAL_LINK'
  sourceType: 'FILE' | 'LINK'
  downloadCount: number
}

export interface StudyMaterialFolder {
  id: string
  name: string
  parentId?: string
}

export const studyMaterialsApi = {
  folders: async (parentId?: string) => {
    const response = await httpClient.get<PageResponse<StudyMaterialFolder>>('/study-materials/folders', {
      params: { parentId },
    })
    return response.data
  },
  search: async (search?: string, params?: { page?: number; size?: number; sort?: string }) => {
    const response = await httpClient.get<PageResponse<StudyMaterial>>('/study-materials/materials', {
      params: { search, ...params },
    })
    return response.data
  },
}
