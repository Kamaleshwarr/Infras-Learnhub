import type { UserRole } from './auth'

export interface UserSummary {
  id: string
  employeeId: string
  fullName: string
  email: string
  role: UserRole
  active: boolean
  mustChangePassword: boolean
  createdAtUtc: string
  updatedAtUtc: string
}

export interface UserListParams {
  employeeId?: string
  fullName?: string
  email?: string
  role?: UserRole
  active?: boolean
  page?: number
  size?: number
  sort?: string
}

export interface UserListQuery {
  page: number
  size: number
  sort: string
  employeeId: string
  fullName: string
  email: string
  role: '' | UserRole
  active: '' | 'true' | 'false'
}

export const DEFAULT_USER_LIST_QUERY: UserListQuery = {
  page: 0,
  size: 20,
  sort: 'employeeId,asc',
  employeeId: '',
  fullName: '',
  email: '',
  role: '',
  active: '',
}

export const USER_PAGE_SIZE_OPTIONS = [10, 20, 50] as const

export const USER_SORTABLE_COLUMNS = [
  'employeeId',
  'fullName',
  'email',
  'active',
  'createdAtUtc',
  'updatedAtUtc',
] as const

export type UserSortableColumn = (typeof USER_SORTABLE_COLUMNS)[number]

export interface CreateUserRequest {
  employeeId: string
  fullName: string
  email: string
  role: UserRole
  password: string
}

export interface UpdateUserRequest {
  fullName: string
  email: string
  role: UserRole
}

export interface ResetPasswordRequest {
  password: string
}

export interface UserImportResponse {
  totalRows: number
  imported: number
  failed: number
  errors: string[]
}
