import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AppRoutes } from './AppRoutes'
import { AuthContext } from '../auth/AuthProvider'
import type { UserProfile } from '../types/auth'

const adminUser: UserProfile = {
  id: 'admin-1',
  employeeId: 'ADMIN001',
  fullName: 'Admin User',
  email: 'admin@example.com',
  mustChangePassword: false,
  roles: ['ADMIN'],
}

const employeeUser: UserProfile = {
  id: 'employee-1',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  email: 'employee@example.com',
  mustChangePassword: false,
  roles: ['EMPLOYEE'],
}

function renderRoute(path: string, user: UserProfile) {
  render(
    <AuthContext.Provider
      value={{
        currentRole: user.roles[0],
        hasRole: (role) => user.roles.includes(role),
        isAdmin: user.roles.includes('ADMIN'),
        isAuthenticated: true,
        isEmployee: user.roles.includes('EMPLOYEE'),
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        refreshProfile: vi.fn(),
        user,
      }}
    >
      <MemoryRouter initialEntries={[path]}>
        <AppRoutes />
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('AppRoutes certificate workflow preparation', () => {
  it('renders admin review route for admins', () => {
    renderRoute('/submissions/review', adminUser)

    expect(screen.getByRole('heading', { name: 'Certificate Review' })).toBeInTheDocument()
  })

  it('renders my submissions route for employees', () => {
    renderRoute('/submissions', employeeUser)

    expect(screen.getByRole('heading', { name: 'My Submissions' })).toBeInTheDocument()
  })

  it('redirects employees away from admin review route', () => {
    renderRoute('/submissions/review', employeeUser)

    expect(screen.queryByRole('heading', { name: 'Certificate Review' })).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Employee Dashboard' })).toBeInTheDocument()
  })
})
