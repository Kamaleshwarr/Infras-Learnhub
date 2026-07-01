import axios from 'axios'
import type { ApiErrorResponse } from '../types/api'

export function resolveApiError(error: unknown, fallback = 'Something went wrong. Please try again.') {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    if (error.response?.data?.message) {
      return error.response.data.message
    }
  }
  return fallback
}

export function getValidationErrors(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.validationErrors
  }
  return undefined
}

export function isNotFoundError(error: unknown) {
  return axios.isAxiosError(error) && error.response?.status === 404
}

export function isConflictError(error: unknown) {
  return axios.isAxiosError(error) && error.response?.status === 409
}
