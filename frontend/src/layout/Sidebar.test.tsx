import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext } from '../auth/AuthProvider'
import { Sidebar } from './Sidebar'
import type { UserProfile } from '../types/auth'

const employeeUser: UserProfile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  email: 'employee@example.com',
  mustChangePassword: false,
  roles: ['EMPLOYEE'],
}

const adminUser: UserProfile = {
  ...employeeUser,
  roles: ['ADMIN'],
}

function renderSidebar(user: UserProfile) {
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
        user,
      }}
    >
      <MemoryRouter>
        <Sidebar mobileOpen={false} onClose={vi.fn()} />
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('Sidebar role-aware navigation', () => {
  it('shows employee-only submission links to employees', () => {
    renderSidebar(employeeUser)

    expect(screen.getAllByText('Submit Certificate').length).toBeGreaterThan(0)
    expect(screen.getAllByText('My Submissions').length).toBeGreaterThan(0)
  })

  it('hides employee-only submission links from admins', () => {
    renderSidebar(adminUser)

    expect(screen.queryAllByText('Submit Certificate')).toHaveLength(0)
    expect(screen.queryAllByText('My Submissions')).toHaveLength(0)
    expect(screen.getAllByText('Dashboard').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Projects').length).toBeGreaterThan(0)
  })

  it('shows admin-only users navigation to admins', () => {
    renderSidebar(adminUser)

    expect(screen.getAllByText('Users').length).toBeGreaterThan(0)
  })

  it('hides admin-only users navigation from employees', () => {
    renderSidebar(employeeUser)

    expect(screen.queryAllByText('Users')).toHaveLength(0)
  })
})

