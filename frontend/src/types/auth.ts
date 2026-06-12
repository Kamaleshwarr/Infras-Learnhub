export type UserRole = 'ADMIN' | 'EMPLOYEE'

export interface UserProfile {
  id: string
  employeeId: string
  fullName: string
  email: string
  roles: UserRole[]
  mustChangePassword: boolean
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresInSeconds: number
  user: UserProfile
}

export interface ForgotPasswordResponse {
  message: string
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

export const PASSWORD_POLICY_MESSAGE =
  'Password must be 8-128 characters and include uppercase, lowercase, number, and special character.'

export function isPasswordPolicyCompliant(password: string) {
  if (password.length < 8 || password.length > 128) {
    return false
  }
  return /[A-Z]/.test(password) && /[a-z]/.test(password) && /[0-9]/.test(password) && /[^A-Za-z0-9]/.test(password)
}

export function isPasswordSameAsEmail(password: string, email: string) {
  const normalizedEmail = email.trim().toLowerCase()
  return normalizedEmail.length > 0 && password === normalizedEmail
}
