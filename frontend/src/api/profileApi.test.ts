import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { profileApi } from './profileApi'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
  },
}))

describe('profileApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetches the current user profile', async () => {
    const profile = {
      id: 'user-1',
      employeeId: 'EMP001',
      fullName: 'Jane Doe',
      email: 'jane.doe@company.com',
      role: 'EMPLOYEE' as const,
      active: true,
      mustChangePassword: false,
      hasAvatar: false,
      avatarUrl: null,
      createdAtUtc: '2026-01-01T00:00:00Z',
      updatedAtUtc: '2026-06-01T00:00:00Z',
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: profile })

    const result = await profileApi.get()

    expect(httpClient.get).toHaveBeenCalledWith('/profile')
    expect(result).toEqual(profile)
  })
})
