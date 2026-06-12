import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { usersApi } from './usersApi'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
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
})
