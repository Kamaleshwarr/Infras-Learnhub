import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ProfilePage } from './ProfilePage'
import { profileApi } from '../../api/profileApi'
import { tokenStorage } from '../../api/httpClient'
import { useAuth } from '../../auth/useAuth'
import type { Profile } from '../../types/profile'

vi.mock('../../api/profileApi', () => ({
  profileApi: {
    get: vi.fn(),
    update: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(),
}))

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

describe('ProfilePage', () => {
  const refreshProfile = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    window.sessionStorage.clear()
    vi.mocked(useAuth).mockReturnValue({
      refreshProfile,
    } as unknown as ReturnType<typeof useAuth>)
    vi.mocked(profileApi.get).mockResolvedValue(profile)
  })

  it('renders profile details after loading', async () => {
    render(<ProfilePage />)

    expect(screen.getByRole('heading', { name: 'My Profile' })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByDisplayValue('Jane Doe')).toBeInTheDocument())
    expect(screen.getByDisplayValue('jane.doe@company.com')).toBeInTheDocument()
    expect(screen.getByDisplayValue('EMP001')).toBeInTheDocument()
    expect(screen.getByDisplayValue('EMPLOYEE')).toBeInTheDocument()
    expect(screen.getByText('Active')).toBeInTheDocument()
    expect(screen.getByLabelText('Profile avatar for Jane Doe')).toHaveTextContent('JD')
  })

  it('renders loading state while fetching profile', () => {
    vi.mocked(profileApi.get).mockReturnValue(new Promise(() => undefined))

    render(<ProfilePage />)

    expect(screen.getByLabelText('Loading profile')).toBeInTheDocument()
  })

  it('renders error state when profile loading fails', async () => {
    vi.mocked(profileApi.get).mockRejectedValue(new Error('network'))

    render(<ProfilePage />)

    expect(
      await screen.findByText('Unable to load profile. Please try again.'),
    ).toBeInTheDocument()
  })

  it('updates profile and refreshes auth context on save', async () => {
    const user = userEvent.setup()
    const updatedProfile: Profile = {
      ...profile,
      fullName: 'Jane Smith',
      email: 'jane.smith@company.com',
    }
    vi.mocked(profileApi.update).mockResolvedValue({
      profile: updatedProfile,
      accessToken: 'new-jwt',
    })
    refreshProfile.mockResolvedValue(undefined)

    render(<ProfilePage />)
    await screen.findByRole('button', { name: 'Edit Profile' })
    await user.click(screen.getByRole('button', { name: 'Edit Profile' }))

    const fullNameInput = screen.getByRole('textbox', { name: 'Full Name' })
    await user.clear(fullNameInput)
    await user.type(fullNameInput, 'Jane Smith')

    const emailInput = screen.getByRole('textbox', { name: 'Email' })
    await user.clear(emailInput)
    await user.type(emailInput, 'jane.smith@company.com')

    await user.click(screen.getByRole('button', { name: 'Save' }))

    await waitFor(() => expect(profileApi.update).toHaveBeenCalledWith({
      fullName: 'Jane Smith',
      email: 'jane.smith@company.com',
    }))
    expect(tokenStorage.get()).toBe('new-jwt')
    expect(refreshProfile).toHaveBeenCalled()
    expect(await screen.findByText('Profile updated successfully.')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Jane Smith')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Save' })).not.toBeInTheDocument()
  })

  it('keeps save disabled until profile form is dirty', async () => {
    const user = userEvent.setup()

    render(<ProfilePage />)
    await user.click(await screen.findByRole('button', { name: 'Edit Profile' }))

    expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled()

    const fullNameInput = screen.getByRole('textbox', { name: 'Full Name' })
    await user.clear(fullNameInput)
    await user.type(fullNameInput, 'Jane Doe Updated')

    expect(screen.getByRole('button', { name: 'Save' })).toBeEnabled()
  })

  it('cancels edit mode without saving', async () => {
    const user = userEvent.setup()

    render(<ProfilePage />)
    await user.click(await screen.findByRole('button', { name: 'Edit Profile' }))
    const fullNameInput = screen.getByRole('textbox', { name: 'Full Name' })
    await user.clear(fullNameInput)
    await user.type(fullNameInput, 'Jane Doe Updated')
    await user.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(screen.getByRole('button', { name: 'Edit Profile' })).toBeInTheDocument()
    expect(profileApi.update).not.toHaveBeenCalled()
  })
})
