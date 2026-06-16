import { httpClient } from './httpClient'
import type { Profile, ProfileUpdateResponse, UpdateProfileRequest } from '../types/profile'

export const profileApi = {
  get: async () => {
    const response = await httpClient.get<Profile>('/profile')
    return response.data
  },
  update: async (request: UpdateProfileRequest) => {
    const response = await httpClient.put<ProfileUpdateResponse>('/profile', request)
    return response.data
  },
  uploadAvatar: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await httpClient.post<Profile>('/profile/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  },
  deleteAvatar: async () => {
    await httpClient.delete('/profile/avatar')
  },
  getAvatarBlob: async (cacheKey?: string) => {
    const response = await httpClient.get<Blob>('/profile/avatar', {
      responseType: 'blob',
      params: cacheKey ? { v: cacheKey } : undefined,
    })
    return response.data
  },
}
