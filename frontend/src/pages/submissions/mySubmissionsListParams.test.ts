import { describe, expect, it } from 'vitest'
import {
  buildMySubmissionsSearchParams,
  DEFAULT_MY_SUBMISSIONS_QUERY,
  parseMySubmissionsListQuery,
  toMySubmissionsApiParams,
} from './mySubmissionsListParams'

describe('mySubmissionsListParams', () => {
  it('parses defaults when search params are empty', () => {
    expect(parseMySubmissionsListQuery(new URLSearchParams())).toEqual(DEFAULT_MY_SUBMISSIONS_QUERY)
  })

  it('parses status filter and pagination from the URL', () => {
    const params = new URLSearchParams({
      page: '2',
      size: '50',
      sort: 'submittedAtUtc,asc',
      status: 'APPROVED',
    })

    expect(parseMySubmissionsListQuery(params)).toEqual({
      page: 2,
      size: 50,
      sort: 'submittedAtUtc,asc',
      status: 'APPROVED',
    })
  })

  it('omits status from API params when filter is ALL', () => {
    expect(toMySubmissionsApiParams(DEFAULT_MY_SUBMISSIONS_QUERY)).toEqual({
      page: 0,
      size: 20,
      sort: 'submittedAtUtc,desc',
    })
  })

  it('round-trips query params through the URL builder', () => {
    const query = {
      page: 1,
      size: 10,
      sort: 'submittedAtUtc,desc',
      status: 'REJECTED' as const,
    }

    expect(parseMySubmissionsListQuery(buildMySubmissionsSearchParams(query))).toEqual(query)
  })
})
