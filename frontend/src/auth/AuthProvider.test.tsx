import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { act } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from './AuthProvider'
import { useAuth } from './useAuth'
import { AUTH_UNAUTHORIZED_EVENT, tokenStorage } from '../api/httpClient'
import { authApi } from '../api/authApi'
import type { LoginResponse, UserProfile } from '../types/auth'

vi.mock('../api/authApi', () => ({
  authApi: {
    login: vi.fn(),
    me: vi.fn(),
  },
}))

const employeeUser: UserProfile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  email: 'employee@example.com',
  roles: ['EMPLOYEE'],
}

const adminUser: UserProfile = {
  ...employeeUser,
  roles: ['ADMIN', 'EMPLOYEE'],
}

function AuthProbe() {
  const { currentRole, hasRole, isAdmin, isAuthenticated, isEmployee, login, logout, user } = useAuth()
  return (
    <div>
      <div data-testid="auth-state">{isAuthenticated ? 'authenticated' : 'anonymous'}</div>
      <div data-testid="email">{user?.email ?? 'none'}</div>
      <div data-testid="role">{currentRole ?? 'none'}</div>
      <div data-testid="is-admin">{String(isAdmin)}</div>
      <div data-testid="is-employee">{String(isEmployee)}</div>
      <div data-testid="has-admin">{String(hasRole('ADMIN'))}</div>
      <button onClick={() => login('admin@example.com', 'Password123')}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  )
}

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.sessionStorage.clear()
  })

  it('restores current user from stored JWT on page refresh', async () => {
    tokenStorage.set('stored-token')
    vi.mocked(authApi.me).mockResolvedValue(employeeUser)

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    )

    await waitFor(() => expect(screen.getByTestId('auth-state')).toHaveTextContent('authenticated'))
    expect(screen.getByTestId('email')).toHaveTextContent('employee@example.com')
    expect(screen.getByTestId('role')).toHaveTextContent('EMPLOYEE')
  })

  it('logs in, derives admin role, and logs out', async () => {
    const loginResponse: LoginResponse = {
      accessToken: 'jwt-token',
      expiresInSeconds: 3600,
      tokenType: 'Bearer',
      user: adminUser,
    }
    vi.mocked(authApi.login).mockResolvedValue(loginResponse)
    const user = userEvent.setup()

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'login' }))

    await waitFor(() => expect(screen.getByTestId('auth-state')).toHaveTextContent('authenticated'))
    expect(tokenStorage.get()).toBe('jwt-token')
    expect(screen.getByTestId('role')).toHaveTextContent('ADMIN')
    expect(screen.getByTestId('is-admin')).toHaveTextContent('true')
    expect(screen.getByTestId('is-employee')).toHaveTextContent('true')
    expect(screen.getByTestId('has-admin')).toHaveTextContent('true')

    await user.click(screen.getByRole('button', { name: 'logout' }))

    expect(screen.getByTestId('auth-state')).toHaveTextContent('anonymous')
    expect(tokenStorage.get()).toBeNull()
  })

  it('clears auth state when expired JWT event is dispatched', async () => {
    tokenStorage.set('stored-token')
    vi.mocked(authApi.me).mockResolvedValue(employeeUser)

    render(
      <AuthProvider>
        <AuthProbe />
      </AuthProvider>,
    )

    await waitFor(() => expect(screen.getByTestId('auth-state')).toHaveTextContent('authenticated'))

    act(() => {
      tokenStorage.clear()
      window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT))
    })

    await waitFor(() => expect(screen.getByTestId('auth-state')).toHaveTextContent('anonymous'))
  })
})

