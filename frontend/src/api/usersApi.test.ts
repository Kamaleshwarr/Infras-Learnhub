import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { usersApi } from './usersApi'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
  },
}))

describe('usersApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('lists users with query params', async () => {
    const responseData = {
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: responseData })

    const result = await usersApi.list({
      page: 0,
      size: 20,
      sort: 'employeeId,asc',
      fullName: 'Jane',
    })

    expect(httpClient.get).toHaveBeenCalledWith('/users', {
      params: {
        page: 0,
        size: 20,
        sort: 'employeeId,asc',
        fullName: 'Jane',
      },
    })
    expect(result).toEqual(responseData)
  })

  it('gets a user by id', async () => {
    const user = {
      active: true,
      createdAtUtc: '2026-06-01T00:00:00Z',
      email: 'admin@example.com',
      employeeId: 'EMP001',
      fullName: 'Admin User',
      id: 'user-1',
      mustChangePassword: false,
      role: 'ADMIN',
      updatedAtUtc: '2026-06-01T00:00:00Z',
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: user })

    const result = await usersApi.get('user-1')

    expect(httpClient.get).toHaveBeenCalledWith('/users/user-1')
    expect(result).toEqual(user)
  })

  it('creates a user', async () => {
    const request = {
      employeeId: 'EMP010',
      fullName: 'Jane Doe',
      email: 'jane.doe@example.com',
      role: 'EMPLOYEE' as const,
      password: 'Temp@12345',
    }
    const response = {
      id: 'user-10',
      ...request,
      active: true,
      createdAtUtc: '',
      mustChangePassword: true,
      updatedAtUtc: '',
    }
    vi.mocked(httpClient.post).mockResolvedValue({ data: response })

    const result = await usersApi.create(request)

    expect(httpClient.post).toHaveBeenCalledWith('/users', request)
    expect(result).toEqual(response)
  })

  it('updates a user', async () => {
    const request = {
      fullName: 'Updated Admin',
      email: 'updated@example.com',
      role: 'ADMIN' as const,
    }
    const response = {
      active: true,
      createdAtUtc: '2026-06-01T00:00:00Z',
      email: 'updated@example.com',
      employeeId: 'EMP001',
      fullName: 'Updated Admin',
      id: 'user-1',
      mustChangePassword: false,
      role: 'ADMIN',
      updatedAtUtc: '2026-06-02T00:00:00Z',
    }
    vi.mocked(httpClient.put).mockResolvedValue({ data: response })

    const result = await usersApi.update('user-1', request)

    expect(httpClient.put).toHaveBeenCalledWith('/users/user-1', request)
    expect(result).toEqual(response)
  })

  it('activates a user', async () => {
    const response = {
      active: true,
      createdAtUtc: '2026-06-01T00:00:00Z',
      email: 'employee@example.com',
      employeeId: 'EMP002',
      fullName: 'Employee User',
      id: 'user-2',
      mustChangePassword: false,
      role: 'EMPLOYEE',
      updatedAtUtc: '2026-06-02T00:00:00Z',
    }
    vi.mocked(httpClient.patch).mockResolvedValue({ data: response })

    const result = await usersApi.activate('user-2')

    expect(httpClient.patch).toHaveBeenCalledWith('/users/user-2/activate')
    expect(result).toEqual(response)
  })

  it('deactivates a user', async () => {
    const response = {
      active: false,
      createdAtUtc: '2026-06-01T00:00:00Z',
      email: 'employee@example.com',
      employeeId: 'EMP002',
      fullName: 'Employee User',
      id: 'user-2',
      mustChangePassword: false,
      role: 'EMPLOYEE',
      updatedAtUtc: '2026-06-02T00:00:00Z',
    }
    vi.mocked(httpClient.patch).mockResolvedValue({ data: response })

    const result = await usersApi.deactivate('user-2')

    expect(httpClient.patch).toHaveBeenCalledWith('/users/user-2/deactivate')
    expect(result).toEqual(response)
  })

  it('resets a user password', async () => {
    vi.mocked(httpClient.post).mockResolvedValue({ data: undefined })

    await usersApi.resetPassword('user-2', { password: 'NewTemp@12345' })

    expect(httpClient.post).toHaveBeenCalledWith('/users/user-2/reset-password', {
      password: 'NewTemp@12345',
    })
  })
})
