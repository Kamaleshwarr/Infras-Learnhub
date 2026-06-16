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
}
