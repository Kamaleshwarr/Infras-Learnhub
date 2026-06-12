import axios from 'axios'
import { describe, expect, it } from 'vitest'
import { getValidationErrors, resolveApiError } from './apiErrors'

describe('apiErrors', () => {
  it('resolves API error messages from axios responses', () => {
    const error = new axios.AxiosError(
      'Request failed',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        data: {
          error: 'Bad Request',
          message: 'Email already exists',
          path: '/users',
          status: 400,
          timestamp: '2026-06-12T00:00:00Z',
        },
        headers: {},
        status: 400,
        statusText: 'Bad Request',
        config: { headers: new axios.AxiosHeaders() },
      },
    )

    expect(resolveApiError(error)).toBe('Email already exists')
  })

  it('returns fallback message for unknown errors', () => {
    expect(resolveApiError(new Error('network'), 'Fallback message')).toBe('Fallback message')
  })

  it('extracts validation errors from axios responses', () => {
    const error = new axios.AxiosError(
      'Request failed',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        data: {
          error: 'Bad Request',
          message: 'Validation failed',
          path: '/users',
          status: 400,
          timestamp: '2026-06-12T00:00:00Z',
          validationErrors: {
            email: 'must be a well-formed email address',
          },
        },
        headers: {},
        status: 400,
        statusText: 'Bad Request',
        config: { headers: new axios.AxiosHeaders() },
      },
    )

    expect(getValidationErrors(error)).toEqual({
      email: 'must be a well-formed email address',
    })
  })
})
