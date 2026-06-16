import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LoginPage } from './LoginPage'
import { AuthContext } from '../../auth/AuthProvider'
import type { LoginResponse, UserProfile } from '../../types/auth'

const employeeUser: UserProfile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  email: 'employee@example.com',
  roles: ['EMPLOYEE'],
  mustChangePassword: false,
}

function renderLogin(login = vi.fn(), initialPath = '/login') {
  return render(
    <AuthContext.Provider
      value={{
        currentRole: null,
        hasRole: () => false,
        isAdmin: false,
        isAuthenticated: false,
        isEmployee: false,
        isLoading: false,
        login,
        logout: vi.fn(),
        refreshProfile: vi.fn(),
        user: null,
      }}
    >
      <MemoryRouter initialEntries={[initialPath]}>
        <Routes>
          <Route element={<LoginPage />} path="/login" />
          <Route element={<div>Dashboard</div>} path="/" />
          <Route element={<div>Requested page</div>} path="/requested" />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('validates email and password before calling login', async () => {
    const login = vi.fn()
    const user = userEvent.setup()
    renderLogin(login)

    await user.type(screen.getByLabelText(/email/i), 'not-an-email')
    await user.type(screen.getByLabelText(/password/i), 'short')

    expect(screen.getByText('Enter a valid email address.')).toBeInTheDocument()
    expect(screen.getByText('Password must be at least 8 characters.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeDisabled()
    expect(login).not.toHaveBeenCalled()
  })

  it('shows backend error for invalid credentials', async () => {
    const login = vi.fn().mockRejectedValue({
      isAxiosError: true,
      name: 'AxiosError',
      message: 'Request failed',
      toJSON: () => ({}),
      response: {
        data: { message: 'Invalid email or password' },
        status: 401,
      },
    })
    const user = userEvent.setup()
    renderLogin(login)

    await user.type(screen.getByLabelText(/email/i), 'employee@example.com')
    await user.type(screen.getByLabelText(/password/i), 'Password123')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText('Invalid email or password')).toBeInTheDocument()
  })

  it('shows loading state and redirects after successful login', async () => {
    let resolveLogin: (value: LoginResponse) => void = () => undefined
    const login = vi.fn(
      () =>
        new Promise<LoginResponse>((resolve) => {
          resolveLogin = resolve
        }),
    )
    const user = userEvent.setup()
    renderLogin(login)

    await user.type(screen.getByLabelText(/email/i), 'employee@example.com')
    await user.type(screen.getByLabelText(/password/i), 'Password123')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(screen.getByRole('progressbar')).toBeInTheDocument()

    resolveLogin({
      accessToken: 'jwt-token',
      expiresInSeconds: 3600,
      tokenType: 'Bearer',
      user: employeeUser,
    })

    await waitFor(() => expect(screen.getByText('Dashboard')).toBeInTheDocument())
    expect(login).toHaveBeenCalledWith('employee@example.com', 'Password123')
  })
})

