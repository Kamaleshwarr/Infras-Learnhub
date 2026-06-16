import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { profileApi } from './profileApi'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    put: vi.fn(),
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

  it('updates the current user profile', async () => {
    const updateRequest = {
      fullName: 'Jane Smith',
      email: 'jane.smith@company.com',
    }
    const response = {
      profile: {
        id: 'user-1',
        employeeId: 'EMP001',
        fullName: 'Jane Smith',
        email: 'jane.smith@company.com',
        role: 'EMPLOYEE' as const,
        active: true,
        mustChangePassword: false,
        hasAvatar: false,
        avatarUrl: null,
        createdAtUtc: '2026-01-01T00:00:00Z',
        updatedAtUtc: '2026-06-10T00:00:00Z',
      },
      accessToken: 'new-jwt',
    }
    vi.mocked(httpClient.put).mockResolvedValue({ data: response })

    const result = await profileApi.update(updateRequest)

    expect(httpClient.put).toHaveBeenCalledWith('/profile', updateRequest)
    expect(result).toEqual(response)
  })
})
