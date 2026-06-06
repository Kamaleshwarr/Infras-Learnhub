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

export function getPrimaryRole(user: UserProfile | null): UserRole | null {
  if (!user) {
    return null
  }
  if (user.roles.includes('ADMIN')) {
    return 'ADMIN'
  }
  if (user.roles.includes('EMPLOYEE')) {
    return 'EMPLOYEE'
  }
  return null
}
