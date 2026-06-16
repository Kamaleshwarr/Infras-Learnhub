import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ProfilePage } from './ProfilePage'
import { profileApi } from '../../api/profileApi'
import type { Profile } from '../../types/profile'

vi.mock('../../api/profileApi', () => ({
  profileApi: {
    get: vi.fn(),
  },
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
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders profile details after loading', async () => {
    vi.mocked(profileApi.get).mockResolvedValue(profile)

    render(<ProfilePage />)

    expect(screen.getByText('My Profile')).toBeInTheDocument()
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
})
