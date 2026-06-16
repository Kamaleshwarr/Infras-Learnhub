import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthContext } from '../../auth/AuthProvider'
import { AppLayout } from '../../layout/AppLayout'
import { DashboardPage } from '../dashboard/DashboardPage'
import { ProfilePage } from './ProfilePage'
import { profileApi } from '../../api/profileApi'
import type { UserProfile } from '../../types/auth'
import type { Profile } from '../../types/profile'

vi.mock('../../api/profileApi', () => ({
  profileApi: {
    get: vi.fn(),
  },
}))

vi.mock('../dashboard/DashboardPage', () => ({
  DashboardPage: () => <div>Dashboard content</div>,
}))

const employeeUser: UserProfile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Jane Doe',
  email: 'jane.doe@company.com',
  mustChangePassword: false,
  roles: ['EMPLOYEE'],
}

const profile: Profile = {
  id: 'user-1',
  employeeId: 'EMP001',
  fullName: 'Jane Doe',
  email: 'jane.doe@company.com',
  role: 'EMPLOYEE',
  active: true,
  mustChangePassword: false,
  hasAvatar: false,
  avatarUrl: null,
  createdAtUtc: '2026-01-01T00:00:00Z',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

function renderApp(initialEntry: string) {
  return render(
    <AuthContext.Provider
      value={{
        currentRole: 'EMPLOYEE',
        hasRole: (role) => employeeUser.roles.includes(role),
        isAdmin: false,
        isAuthenticated: true,
        isEmployee: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        user: employeeUser,
      }}
    >
      <MemoryRouter initialEntries={[initialEntry]}>
        <Routes>
          <Route element={<AppLayout />}>
            <Route element={<DashboardPage />} index />
            <Route element={<ProfilePage />} path="profile" />
          </Route>
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('Profile navigation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(profileApi.get).mockResolvedValue(profile)
  })

  it('navigates from dashboard to my profile via sidebar', async () => {
    const user = userEvent.setup()
    renderApp('/')

    expect(screen.getByText('Dashboard content')).toBeInTheDocument()

    await user.click(screen.getAllByRole('link', { name: 'My Profile' })[0])

    await waitFor(() => expect(screen.getByRole('heading', { name: 'My Profile' })).toBeInTheDocument())
    expect(await screen.findByDisplayValue('Jane Doe')).toBeInTheDocument()
    expect(profileApi.get).toHaveBeenCalled()
  })

  it('renders profile page when visiting /profile directly', async () => {
    renderApp('/profile')

    expect(screen.getByRole('heading', { name: 'My Profile' })).toBeInTheDocument()
    expect(await screen.findByDisplayValue('jane.doe@company.com')).toBeInTheDocument()
    expect(profileApi.get).toHaveBeenCalledTimes(1)
  })

  it('reloads profile data when /profile is mounted again', async () => {
    const view = renderApp('/profile')

    expect(await screen.findByDisplayValue('EMP001')).toBeInTheDocument()
    expect(profileApi.get).toHaveBeenCalledTimes(1)

    view.unmount()
    renderApp('/profile')

    await waitFor(() => expect(profileApi.get).toHaveBeenCalledTimes(2))
    expect(await screen.findByDisplayValue('Jane Doe')).toBeInTheDocument()
  })
})
