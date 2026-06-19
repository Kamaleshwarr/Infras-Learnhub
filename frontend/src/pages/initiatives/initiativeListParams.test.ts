import { describe, expect, it } from 'vitest'
import {
  buildInitiativeListSearchParams,
  parseInitiativeListQuery,
  parseSort,
  toInitiativeApiParams,
  toggleSort,
} from './initiativeListParams'
import { DEFAULT_INITIATIVE_LIST_QUERY } from '../../types/initiatives'

describe('initiativeListParams', () => {
  it('parses default query from empty search params', () => {
    expect(parseInitiativeListQuery(new URLSearchParams())).toEqual(DEFAULT_INITIATIVE_LIST_QUERY)
  })

  it('parses custom page, size, sort, search, and status', () => {
    const params = new URLSearchParams({
      page: '2',
      search: 'aws',
      size: '50',
      sort: 'title,desc',
      status: 'ACTIVE',
    })

    expect(parseInitiativeListQuery(params)).toEqual({
      page: 2,
      search: 'aws',
      size: 50,
      sort: 'title,desc',
      status: 'ACTIVE',
    })
  })

  it('ignores invalid status values', () => {
    expect(parseInitiativeListQuery(new URLSearchParams({ status: 'INVALID' })).status).toBe('')
  })

  it('falls back to defaults for invalid page and size', () => {
    const params = new URLSearchParams({
      page: '-1',
      size: '99',
    })

    expect(parseInitiativeListQuery(params)).toEqual({
      ...DEFAULT_INITIATIVE_LIST_QUERY,
      page: 0,
      size: 20,
    })
  })

  it('builds search params omitting defaults', () => {
    expect(buildInitiativeListSearchParams(DEFAULT_INITIATIVE_LIST_QUERY).toString()).toBe('')

    expect(
      buildInitiativeListSearchParams({
        page: 1,
        search: 'cloud',
        size: 10,
        sort: 'title,asc',
        status: 'DRAFT',
      }).toString(),
    ).toBe('page=1&size=10&sort=title%2Casc&search=cloud&status=DRAFT')
  })

  it('maps query to API params with search and admin status', () => {
    expect(
      toInitiativeApiParams(
        {
          page: 0,
          search: 'kubernetes',
          size: 20,
          sort: 'expiryDateUtc,asc',
          status: 'ACTIVE',
        },
        { isAdmin: true },
      ),
    ).toEqual({
      page: 0,
      search: 'kubernetes',
      size: 20,
      sort: 'expiryDateUtc,asc',
      status: 'ACTIVE',
    })
  })

  it('omits status for employees even when present in query', () => {
    expect(
      toInitiativeApiParams(
        {
          ...DEFAULT_INITIATIVE_LIST_QUERY,
          status: 'ACTIVE',
        },
        { isAdmin: false },
      ),
    ).toEqual({
      page: 0,
      size: 20,
      sort: 'expiryDateUtc,asc',
    })
  })

  it('omits empty search and status from API params', () => {
    expect(toInitiativeApiParams(DEFAULT_INITIATIVE_LIST_QUERY, { isAdmin: true })).toEqual({
      page: 0,
      size: 20,
      sort: 'expiryDateUtc,asc',
    })
  })

  it('parses and toggles sort', () => {
    expect(parseSort('expiryDateUtc,desc')).toEqual({
      direction: 'desc',
      property: 'expiryDateUtc',
    })
    expect(toggleSort('expiryDateUtc,asc', 'expiryDateUtc')).toBe('expiryDateUtc,desc')
    expect(toggleSort('expiryDateUtc,desc', 'title')).toBe('title,asc')
  })
})
