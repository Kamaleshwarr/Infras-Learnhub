export type NotificationType =
  | 'CERTIFICATE_SUBMITTED'
  | 'CERTIFICATE_APPROVED'
  | 'CERTIFICATE_REJECTED'
  | 'PASSWORD_RESET_BY_ADMIN'
  | 'ACCOUNT_ACTIVATED'
  | 'ACCOUNT_DEACTIVATED'
  | 'ACCOUNT_CREATED'

export type NotificationEntityType = 'CERTIFICATE_SUBMISSION' | 'USER'

export interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  entityType?: NotificationEntityType
  entityId?: string
  actionPath?: string | null
  read: boolean
  readAtUtc?: string | null
  createdAtUtc: string
}

export interface NotificationListParams {
  page?: number
  size?: number
  sort?: string
  read?: boolean
  type?: NotificationType
}

export interface UnreadCountResponse {
  count: number
}
