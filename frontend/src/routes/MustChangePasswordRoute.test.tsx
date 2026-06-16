import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext } from '../auth/AuthProvider'
import { MustChangePasswordRoute } from './MustChangePasswordRoute'
import type { UserProfile } from '../types/auth'

const baseUser: UserProfile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Jane Doe',
  email: 'jane.doe@company.com',
  roles: ['EMPLOYEE'],
  mustChangePassword: false,
}

function renderRoute(initialEntry: string, user: UserProfile) {
  render(
    <AuthContext.Provider
      value={{
        currentRole: 'EMPLOYEE',
        hasRole: () => true,
        isAdmin: false,
        isAuthenticated: true,
        isEmployee: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        refreshProfile: vi.fn(),
        user,
      }}
    >
      <MemoryRouter initialEntries={[initialEntry]}>
        <Routes>
          <Route element={<MustChangePasswordRoute />}>
            <Route element={<div>Change password page</div>} path="/change-password" />
            <Route element={<div>Profile page</div>} path="/profile" />
            <Route element={<div>Notifications page</div>} path="/notifications" />
          </Route>
          <Route element={<div>Dashboard</div>} path="/" />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('MustChangePasswordRoute', () => {
  it('redirects to change password when password change is required', () => {
    renderRoute('/profile', { ...baseUser, mustChangePassword: true })

    expect(screen.queryByText('Profile page')).not.toBeInTheDocument()
    expect(screen.getByText('Change password page')).toBeInTheDocument()
  })

  it('allows voluntary access to change password when not required', () => {
    renderRoute('/change-password', baseUser)

    expect(screen.getByText('Change password page')).toBeInTheDocument()
    expect(screen.queryByText('Dashboard')).not.toBeInTheDocument()
  })

  it('allows profile access when password change is not required', () => {
    renderRoute('/profile', baseUser)

    expect(screen.getByText('Profile page')).toBeInTheDocument()
  })

  it('allows notifications access when password change is required', () => {
    renderRoute('/notifications', { ...baseUser, mustChangePassword: true })

    expect(screen.getByText('Notifications page')).toBeInTheDocument()
    expect(screen.queryByText('Change password page')).not.toBeInTheDocument()
  })
})
