import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function MustChangePasswordRoute() {
  const { user } = useAuth()
  const location = useLocation()

  if (user?.mustChangePassword && location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />
  }

  if (!user?.mustChangePassword && location.pathname === '/change-password') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
