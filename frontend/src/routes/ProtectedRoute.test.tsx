import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext } from '../auth/AuthProvider'
import { UserListPage } from '../pages/users/UserListPage'
import { ProtectedRoute } from './ProtectedRoute'
import { RoleRoute } from './RoleRoute'

vi.mock('../pages/users/UserListPage', () => ({
  UserListPage: () => <div>User management</div>,
}))
import type { UserProfile } from '../types/auth'

const employeeUser: UserProfile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  email: 'employee@example.com',
  mustChangePassword: false,
  roles: ['EMPLOYEE'],
}

function authValue(user: UserProfile | null, isLoading = false) {
  return {
    currentRole: user?.roles[0] ?? null,
    hasRole: (role: 'ADMIN' | 'EMPLOYEE') => Boolean(user?.roles.includes(role)),
    isAdmin: Boolean(user?.roles.includes('ADMIN')),
    isAuthenticated: Boolean(user),
    isEmployee: Boolean(user?.roles.includes('EMPLOYEE')),
    isLoading,
    login: vi.fn(),
    logout: vi.fn(),
    refreshProfile: vi.fn(),
    user,
  }
}

describe('route guards', () => {
  it('redirects unauthenticated users to login', () => {
    render(
      <AuthContext.Provider value={authValue(null)}>
        <MemoryRouter initialEntries={['/protected']}>
          <Routes>
            <Route element={<ProtectedRoute />}>
              <Route element={<div>Protected content</div>} path="/protected" />
            </Route>
            <Route element={<div>Login screen</div>} path="/login" />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>,
    )

    expect(screen.getByText('Login screen')).toBeInTheDocument()
  })

  it('renders protected content for authenticated users', () => {
    render(
      <AuthContext.Provider value={authValue(employeeUser)}>
        <MemoryRouter initialEntries={['/protected']}>
          <Routes>
            <Route element={<ProtectedRoute />}>
              <Route element={<div>Protected content</div>} path="/protected" />
            </Route>
            <Route element={<div>Login screen</div>} path="/login" />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>,
    )

    expect(screen.getByText('Protected content')).toBeInTheDocument()
  })

  it('redirects users without required role', () => {
    render(
      <AuthContext.Provider value={authValue(employeeUser)}>
        <MemoryRouter initialEntries={['/admin']}>
          <Routes>
            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route element={<div>Admin content</div>} path="/admin" />
            </Route>
            <Route element={<div>Dashboard</div>} path="/" />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>,
    )

    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })

  it('allows admins to access admin-only user management route', () => {
    render(
      <AuthContext.Provider value={authValue({ ...employeeUser, roles: ['ADMIN'] })}>
        <MemoryRouter initialEntries={['/users']}>
          <Routes>
            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route element={<UserListPage />} path="/users" />
            </Route>
            <Route element={<div>Dashboard</div>} path="/" />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>,
    )

    expect(screen.getByText('User management')).toBeInTheDocument()
  })

  it('redirects employees away from admin-only user management route', () => {
    render(
      <AuthContext.Provider value={authValue(employeeUser)}>
        <MemoryRouter initialEntries={['/users']}>
          <Routes>
            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route element={<UserListPage />} path="/users" />
            </Route>
            <Route element={<div>Dashboard</div>} path="/" />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>,
    )

    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })
})

