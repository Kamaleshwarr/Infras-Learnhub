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

  it('parses custom page, size, sort, and search', () => {
    const params = new URLSearchParams({
      page: '2',
      search: 'aws',
      size: '50',
      sort: 'title,desc',
    })

    expect(parseInitiativeListQuery(params)).toEqual({
      page: 2,
      search: 'aws',
      size: 50,
      sort: 'title,desc',
    })
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
      }).toString(),
    ).toBe('page=1&size=10&sort=title%2Casc&search=cloud')
  })

  it('maps query to API params with search', () => {
    expect(
      toInitiativeApiParams({
        page: 0,
        search: 'kubernetes',
        size: 20,
        sort: 'expiryDateUtc,asc',
      }),
    ).toEqual({
      page: 0,
      search: 'kubernetes',
      size: 20,
      sort: 'expiryDateUtc,asc',
    })
  })

  it('omits empty search from API params', () => {
    expect(toInitiativeApiParams(DEFAULT_INITIATIVE_LIST_QUERY)).toEqual({
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
