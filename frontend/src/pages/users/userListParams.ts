import type { UserListParams, UserListQuery } from '../../types/users'
import { DEFAULT_USER_LIST_QUERY, USER_PAGE_SIZE_OPTIONS } from '../../types/users'
import type { UserRole } from '../../types/auth'

export function parseUserListQuery(searchParams: URLSearchParams): UserListQuery {
  const page = parsePositiveInt(searchParams.get('page'), DEFAULT_USER_LIST_QUERY.page)
  const rawSize = parsePositiveInt(searchParams.get('size'), DEFAULT_USER_LIST_QUERY.size)
  const size = USER_PAGE_SIZE_OPTIONS.includes(rawSize as (typeof USER_PAGE_SIZE_OPTIONS)[number])
    ? rawSize
    : DEFAULT_USER_LIST_QUERY.size
  const sort = searchParams.get('sort')?.trim() || DEFAULT_USER_LIST_QUERY.sort

  return {
    page,
    size,
    sort,
    employeeId: searchParams.get('employeeId')?.trim() ?? '',
    fullName: searchParams.get('fullName')?.trim() ?? '',
    email: searchParams.get('email')?.trim() ?? '',
    role: parseRole(searchParams.get('role')),
    active: parseActive(searchParams.get('active')),
  }
}

export function buildUserListSearchParams(query: UserListQuery): URLSearchParams {
  const params = new URLSearchParams()

  if (query.page !== DEFAULT_USER_LIST_QUERY.page) {
    params.set('page', String(query.page))
  }
  if (query.size !== DEFAULT_USER_LIST_QUERY.size) {
    params.set('size', String(query.size))
  }
  if (query.sort !== DEFAULT_USER_LIST_QUERY.sort) {
    params.set('sort', query.sort)
  }
  if (query.employeeId) {
    params.set('employeeId', query.employeeId)
  }
  if (query.fullName) {
    params.set('fullName', query.fullName)
  }
  if (query.email) {
    params.set('email', query.email)
  }
  if (query.role) {
    params.set('role', query.role)
  }
  if (query.active) {
    params.set('active', query.active)
  }

  return params
}

export function toApiParams(query: UserListQuery): UserListParams {
  const params: UserListParams = {
    page: query.page,
    size: query.size,
    sort: query.sort,
  }

  if (query.employeeId) {
    params.employeeId = query.employeeId
  }
  if (query.fullName) {
    params.fullName = query.fullName
  }
  if (query.email) {
    params.email = query.email
  }
  if (query.role) {
    params.role = query.role
  }
  if (query.active === 'true') {
    params.active = true
  }
  if (query.active === 'false') {
    params.active = false
  }

  return params
}

export function parseSort(sort: string): { property: string; direction: 'asc' | 'desc' } {
  const [property, direction = 'asc'] = sort.split(',')
  return {
    property,
    direction: direction.toLowerCase() === 'desc' ? 'desc' : 'asc',
  }
}

export function toggleSort(currentSort: string, property: string): string {
  const { property: activeProperty, direction } = parseSort(currentSort)
  if (activeProperty === property) {
    return `${property},${direction === 'asc' ? 'desc' : 'asc'}`
  }
  return `${property},asc`
}

function parsePositiveInt(value: string | null, fallback: number) {
  if (!value) {
    return fallback
  }
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback
}

function parseRole(value: string | null): '' | UserRole {
  if (value === 'ADMIN' || value === 'EMPLOYEE') {
    return value
  }
  return ''
}

function parseActive(value: string | null): '' | 'true' | 'false' {
  if (value === 'true' || value === 'false') {
    return value
  }
  return ''
}
