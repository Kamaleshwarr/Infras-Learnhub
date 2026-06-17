import { describe, expect, it } from 'vitest'
import {
  buildAdminReviewSearchParams,
  DEFAULT_ADMIN_REVIEW_QUERY,
  parseAdminReviewListQuery,
  toAdminReviewApiParams,
} from './adminReviewListParams'

describe('adminReviewListParams', () => {
  it('parses defaults when search params are empty', () => {
    expect(parseAdminReviewListQuery(new URLSearchParams())).toEqual(DEFAULT_ADMIN_REVIEW_QUERY)
  })

  it('parses page and size from search params', () => {
    const params = new URLSearchParams({ page: '2', size: '50', sort: 'submittedAtUtc,asc' })
    expect(parseAdminReviewListQuery(params)).toEqual({
      page: 2,
      size: 50,
      sort: 'submittedAtUtc,asc',
    })
  })

  it('falls back to defaults for invalid page and size', () => {
    const params = new URLSearchParams({ page: '-1', size: '0' })
    expect(parseAdminReviewListQuery(params)).toEqual(DEFAULT_ADMIN_REVIEW_QUERY)
  })

  it('builds search params from query', () => {
    const params = buildAdminReviewSearchParams({ page: 1, size: 10, sort: 'submittedAtUtc,desc' })
    expect(params.get('page')).toBe('1')
    expect(params.get('size')).toBe('10')
    expect(params.get('sort')).toBe('submittedAtUtc,desc')
  })

  it('maps query to admin API params with SUBMITTED status', () => {
    expect(toAdminReviewApiParams(DEFAULT_ADMIN_REVIEW_QUERY)).toEqual({
      page: 0,
      size: 20,
      sort: 'submittedAtUtc,desc',
      status: 'SUBMITTED',
    })
  })
})
