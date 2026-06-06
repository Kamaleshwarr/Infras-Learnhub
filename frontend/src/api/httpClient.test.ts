import { describe, expect, it, vi, beforeEach } from 'vitest'
import { AUTH_UNAUTHORIZED_EVENT, httpClient, tokenStorage } from './httpClient'

describe('httpClient authentication integration', () => {
  beforeEach(() => {
    window.sessionStorage.clear()
  })

  it('stores JWT in session storage', () => {
    tokenStorage.set('jwt-token')

    expect(tokenStorage.get()).toBe('jwt-token')

    tokenStorage.clear()

    expect(tokenStorage.get()).toBeNull()
  })

  it('attaches bearer token to outgoing Axios requests', async () => {
    tokenStorage.set('jwt-token')
    const adapter = vi.fn(async (config) => ({
      config,
      data: {},
      headers: {},
      status: 200,
      statusText: 'OK',
    }))

    await httpClient.get('/auth/me', { adapter })

    expect(adapter.mock.calls[0][0].headers.Authorization).toBe('Bearer jwt-token')
  })

  it('clears token and dispatches event when API returns unauthorized', async () => {
    tokenStorage.set('expired-token')
    const listener = vi.fn()
    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, listener)
    const adapter = vi.fn(async (config) => {
      throw {
        config,
        isAxiosError: true,
        response: {
          status: 401,
          data: {},
          headers: {},
          statusText: 'Unauthorized',
        },
      }
    })

    await expect(httpClient.get('/auth/me', { adapter })).rejects.toBeTruthy()

    expect(tokenStorage.get()).toBeNull()
    expect(listener).toHaveBeenCalledTimes(1)
  })
})

