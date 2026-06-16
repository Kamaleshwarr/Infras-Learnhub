import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function MustChangePasswordRoute() {
  const { user } = useAuth()
  const location = useLocation()

  const allowedPaths = ['/change-password', '/notifications']
  if (user?.mustChangePassword && !allowedPaths.includes(location.pathname)) {
    return <Navigate to="/change-password" replace />
  }

  return <Outlet />
}
