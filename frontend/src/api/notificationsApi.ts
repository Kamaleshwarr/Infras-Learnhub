import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type { Notification, NotificationListParams, UnreadCountResponse } from '../types/notifications'

export const notificationsApi = {
  list: async (params?: NotificationListParams) => {
    const response = await httpClient.get<PageResponse<Notification>>('/notifications', { params })
    return response.data
  },
  unreadCount: async () => {
    const response = await httpClient.get<UnreadCountResponse>('/notifications/unread-count')
    return response.data
  },
  markRead: async (notificationId: string) => {
    const response = await httpClient.patch<Notification>(`/notifications/${notificationId}/read`)
    return response.data
  },
  markAllRead: async () => {
    await httpClient.patch('/notifications/read-all')
  },
}
