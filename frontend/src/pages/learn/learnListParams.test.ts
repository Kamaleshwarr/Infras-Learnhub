import { describe, expect, it } from 'vitest'
import {
  buildTechnologyListSearchParams,
  parseTechnologyListQuery,
  toTechnologyApiParams,
  toggleSort,
} from './learnListParams'

describe('learnListParams', () => {
  it('parses search, filters, and pagination from URL params', () => {
    const params = new URLSearchParams('search=aws&category=CLOUD&difficulty=BEGINNER&page=1&size=50&sort=name,desc')
    expect(parseTechnologyListQuery(params)).toEqual({
      page: 1,
      size: 50,
      sort: 'name,desc',
      search: 'aws',
      category: 'CLOUD',
      difficulty: 'BEGINNER',
      status: '',
    })
  })

  it('builds URL params from query state', () => {
    const params = buildTechnologyListSearchParams({
      page: 2,
      size: 10,
      sort: 'name,asc',
      search: 'spring',
      category: 'LANGUAGES',
      difficulty: '',
      status: 'DRAFT',
    })

    expect(params.get('page')).toBe('2')
    expect(params.get('size')).toBe('10')
    expect(params.get('search')).toBe('spring')
    expect(params.get('category')).toBe('LANGUAGES')
    expect(params.get('status')).toBe('DRAFT')
  })

  it('includes admin status only for manage list API params', () => {
    expect(
      toTechnologyApiParams(
        {
          page: 0,
          size: 20,
          sort: 'name,asc',
          search: '',
          category: '',
          difficulty: '',
          status: 'DRAFT',
        },
        { isAdmin: true },
      ).status,
    ).toBe('DRAFT')

    expect(
      toTechnologyApiParams(
        {
          page: 0,
          size: 20,
          sort: 'name,asc',
          search: '',
          category: '',
          difficulty: '',
          status: 'DRAFT',
        },
        { isAdmin: false },
      ).status,
    ).toBeUndefined()
  })

  it('toggles sort direction', () => {
    expect(toggleSort('name,asc', 'name')).toBe('name,desc')
    expect(toggleSort('name,desc', 'category')).toBe('category,asc')
  })
})
