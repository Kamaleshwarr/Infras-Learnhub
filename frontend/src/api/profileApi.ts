import { httpClient } from './httpClient'
import type { Profile } from '../types/profile'

export const profileApi = {
  get: async () => {
    const response = await httpClient.get<Profile>('/profile')
    return response.data
  },
}
