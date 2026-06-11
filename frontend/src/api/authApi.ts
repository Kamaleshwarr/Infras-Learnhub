import { httpClient } from './httpClient'
import type { ForgotPasswordResponse, LoginResponse, UserProfile } from '../types/auth'

export interface LoginRequest {
  email: string
  password: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmNewPassword: string
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  newPassword: string
  confirmNewPassword: string
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
  changePassword: async (request: ChangePasswordRequest) => {
    await httpClient.post('/auth/change-password', request)
  },
  forgotPassword: async (request: ForgotPasswordRequest) => {
    const response = await httpClient.post<ForgotPasswordResponse>('/auth/forgot-password', request)
    return response.data
  },
  resetPassword: async (request: ResetPasswordRequest) => {
    await httpClient.post('/auth/reset-password', request)
  },
}
