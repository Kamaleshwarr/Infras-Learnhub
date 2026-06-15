import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type { CreateUserRequest, ResetPasswordRequest, UpdateUserRequest, UserListParams, UserSummary } from '../types/users'

export const usersApi = {
  list: async (params?: UserListParams) => {
    const response = await httpClient.get<PageResponse<UserSummary>>('/users', { params })
    return response.data
  },
  get: async (userId: string) => {
    const response = await httpClient.get<UserSummary>(`/users/${userId}`)
    return response.data
  },
  create: async (request: CreateUserRequest) => {
    const response = await httpClient.post<UserSummary>('/users', request)
    return response.data
  },
  update: async (userId: string, request: UpdateUserRequest) => {
    const response = await httpClient.put<UserSummary>(`/users/${userId}`, request)
    return response.data
  },
  activate: async (userId: string) => {
    const response = await httpClient.patch<UserSummary>(`/users/${userId}/activate`)
    return response.data
  },
  deactivate: async (userId: string) => {
    const response = await httpClient.patch<UserSummary>(`/users/${userId}/deactivate`)
    return response.data
  },
  resetPassword: async (userId: string, request: ResetPasswordRequest) => {
    await httpClient.post(`/users/${userId}/reset-password`, request)
  },
}
