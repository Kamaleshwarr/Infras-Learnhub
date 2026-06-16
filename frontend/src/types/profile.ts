import type { UserRole } from './auth'

export interface Profile {
  id: string
  employeeId: string
  fullName: string
  email: string
  role: UserRole
  active: boolean
  mustChangePassword: boolean
  hasAvatar: boolean
  avatarUrl: string | null
  createdAtUtc: string
  updatedAtUtc: string
}

export interface UpdateProfileRequest {
  fullName: string
  email: string
}

export interface ProfileUpdateResponse {
  profile: Profile
  accessToken?: string | null
}
