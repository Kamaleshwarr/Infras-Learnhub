export type UserRole = 'ADMIN' | 'EMPLOYEE'

export interface UserProfile {
  id: string
  employeeId: string
  fullName: string
  email: string
  roles: UserRole[]
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresInSeconds: number
  user: UserProfile
}
