import { describe, expect, it } from 'vitest'
import { DEFAULT_USER_LIST_QUERY } from '../../types/users'
import {
  buildUserListSearchParams,
  parseUserListQuery,
  toApiParams,
  toggleSort,
} from './userListParams'

describe('userListParams', () => {
  it('parses URL search params into a normalized query', () => {
    const params = new URLSearchParams({
      page: '2',
      size: '50',
      sort: 'fullName,desc',
      employeeId: 'EMP',
      fullName: 'Jane',
      email: 'jane@example.com',
      role: 'ADMIN',
      active: 'true',
    })

    expect(parseUserListQuery(params)).toEqual({
      page: 2,
      size: 50,
      sort: 'fullName,desc',
      employeeId: 'EMP',
      fullName: 'Jane',
      email: 'jane@example.com',
      role: 'ADMIN',
      active: 'true',
    })
  })

  it('falls back to defaults for invalid values', () => {
    expect(parseUserListQuery(new URLSearchParams({ page: '-1', size: '999', role: 'MANAGER' }))).toEqual(
      DEFAULT_USER_LIST_QUERY,
    )
  })

  it('builds sparse URL search params from query state', () => {
    const params = buildUserListSearchParams({
      ...DEFAULT_USER_LIST_QUERY,
      fullName: 'Jane',
      role: 'EMPLOYEE',
      page: 1,
    })

    expect(params.get('fullName')).toBe('Jane')
    expect(params.get('role')).toBe('EMPLOYEE')
    expect(params.get('page')).toBe('1')
    expect(params.get('employeeId')).toBeNull()
    expect(params.get('sort')).toBeNull()
  })

  it('maps query state to API params', () => {
    expect(
      toApiParams({
        page: 0,
        size: 20,
        sort: 'email,asc',
        employeeId: 'EMP001',
        fullName: '',
        email: 'user@example.com',
        role: 'ADMIN',
        active: 'false',
      }),
    ).toEqual({
      page: 0,
      size: 20,
      sort: 'email,asc',
      employeeId: 'EMP001',
      email: 'user@example.com',
      role: 'ADMIN',
      active: false,
    })
  })

  it('toggles sort direction for the same column', () => {
    expect(toggleSort('employeeId,asc', 'employeeId')).toBe('employeeId,desc')
    expect(toggleSort('employeeId,desc', 'employeeId')).toBe('employeeId,asc')
    expect(toggleSort('employeeId,asc', 'fullName')).toBe('fullName,asc')
  })
})
