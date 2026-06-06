export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  sort: Array<{
    property: string
    direction: 'ASC' | 'DESC'
  }>
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  validationErrors?: Record<string, string>
}
