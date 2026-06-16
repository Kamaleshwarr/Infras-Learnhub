import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ProfileAvatarUpload } from './ProfileAvatarUpload'
import { profileApi } from '../../api/profileApi'
import type { Profile } from '../../types/profile'

vi.mock('../../api/profileApi', () => ({
  profileApi: {
    get: vi.fn(),
    uploadAvatar: vi.fn(),
    deleteAvatar: vi.fn(),
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

describe('ProfileAvatarUpload', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uploads a valid avatar file', async () => {
    const user = userEvent.setup()
    const onUpdated = vi.fn()
    const updatedProfile = { ...profile, hasAvatar: true, avatarUrl: '/api/v1/profile/avatar' }
    vi.mocked(profileApi.uploadAvatar).mockResolvedValue(updatedProfile)

    render(<ProfileAvatarUpload onError={vi.fn()} onUpdated={onUpdated} profile={profile} />)

    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' })
    await user.upload(input, file)

    await waitFor(() => expect(profileApi.uploadAvatar).toHaveBeenCalled())
    expect(onUpdated).toHaveBeenCalledWith(updatedProfile)
  })

  it('rejects files larger than 2 MB', async () => {
    const user = userEvent.setup()
    const onError = vi.fn()

    render(<ProfileAvatarUpload onError={onError} onUpdated={vi.fn()} profile={profile} />)

    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    const largeFile = new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'avatar.png', { type: 'image/png' })
    await user.upload(input, largeFile)

    expect(onError).toHaveBeenCalled()
    expect(profileApi.uploadAvatar).not.toHaveBeenCalled()
  })

  it('deletes an existing avatar', async () => {
    const user = userEvent.setup()
    const onUpdated = vi.fn()
    const profileWithAvatar = { ...profile, hasAvatar: true, avatarUrl: '/api/v1/profile/avatar' }
    vi.mocked(profileApi.deleteAvatar).mockResolvedValue(undefined)
    vi.mocked(profileApi.get).mockResolvedValue(profile)

    render(
      <ProfileAvatarUpload onError={vi.fn()} onUpdated={onUpdated} profile={profileWithAvatar} />,
    )

    await user.click(screen.getByRole('button', { name: 'Delete Photo' }))
    await user.click(screen.getByRole('button', { name: 'Delete Photo' }))

    await waitFor(() => expect(profileApi.deleteAvatar).toHaveBeenCalled())
    expect(profileApi.get).toHaveBeenCalled()
    expect(onUpdated).toHaveBeenCalledWith(profile)
  })
})
