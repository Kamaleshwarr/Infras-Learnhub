import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ProfileAvatar } from './ProfileAvatar'
import { profileApi } from '../../api/profileApi'

vi.mock('../../api/profileApi', () => ({
  profileApi: {
    getAvatarBlob: vi.fn(),
  },
}))

describe('ProfileAvatar', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows initials when no avatar exists', () => {
    render(<ProfileAvatar fullName="Jane Doe" hasAvatar={false} />)

    expect(screen.getByLabelText('Profile avatar for Jane Doe')).toHaveTextContent('JD')
  })

  it('loads avatar image when avatar exists', async () => {
    vi.mocked(profileApi.getAvatarBlob).mockResolvedValue(new Blob(['avatar'], { type: 'image/png' }))
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:avatar')
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined)

    render(<ProfileAvatar avatarCacheKey="2026-06-01T00:00:00Z" fullName="Jane Doe" hasAvatar />)

    await waitFor(() => expect(profileApi.getAvatarBlob).toHaveBeenCalledWith('2026-06-01T00:00:00Z'))
    expect(screen.getByRole('img', { name: 'Jane Doe' })).toHaveAttribute('src', 'blob:avatar')
  })
})
