import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type { UserListParams, UserSummary } from '../types/users'

export const usersApi = {
  list: async (params?: UserListParams) => {
    const response = await httpClient.get<PageResponse<UserSummary>>('/users', { params })
    return response.data
  },
}
