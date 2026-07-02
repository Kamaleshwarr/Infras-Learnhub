import { describe, expect, it } from 'vitest'
import {
  buildTechnologyListSearchParams,
  parseTechnologyListQuery,
  toTechnologyApiParams,
  toggleSort,
} from './learnListParams'

describe('learnListParams', () => {
  it('parses admin filters from search params', () => {
    const params = buildTechnologyListSearchParams({
      page: 1,
      size: 10,
      sort: 'name,desc',
      search: 'spring',
      category: 'BACKEND',
      difficulty: 'INTERMEDIATE',
      status: 'HIDDEN',
    })

    expect(params.get('category')).toBe('BACKEND')
    expect(params.get('status')).toBe('HIDDEN')
    expect(parseTechnologyListQuery(params).status).toBe('HIDDEN')
  })

  it('includes status only for admin API params', () => {
    const query = parseTechnologyListQuery(new URLSearchParams('status=HIDDEN'))

    expect(toTechnologyApiParams(query, { isAdmin: true }).status).toBe('HIDDEN')
    expect(toTechnologyApiParams(query, { isAdmin: false }).status).toBeUndefined()
  })

  it('toggles sort direction', () => {
    expect(toggleSort('name,asc', 'name')).toBe('name,desc')
    expect(toggleSort('name,desc', 'slug')).toBe('slug,asc')
  })
})
