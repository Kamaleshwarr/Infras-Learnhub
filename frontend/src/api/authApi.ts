import { httpClient } from './httpClient'
import type { LoginResponse, UserProfile } from '../types/auth'

export interface LoginRequest {
  email: string
  password: string
}

export const authApi = {
  login: async (request: LoginRequest) => {
    const response = await httpClient.post<LoginResponse>('/auth/login', request)
    return response.data
  },
  me: async () => {
    const response = await httpClient.get<UserProfile>('/auth/me')
    return response.data
  },
}
