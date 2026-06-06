import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import type { UserRole } from '../types/auth'

interface RoleRouteProps {
  roles: UserRole[]
}

export function RoleRoute({ roles }: RoleRouteProps) {
  const { hasRole } = useAuth()
  const allowed = roles.some((role) => hasRole(role))

  return allowed ? <Outlet /> : <Navigate to="/" replace />
}
