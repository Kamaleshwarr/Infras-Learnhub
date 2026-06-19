import type { InitiativeListParams, InitiativeListQuery } from '../../types/initiatives'
import {
  DEFAULT_INITIATIVE_LIST_QUERY,
  INITIATIVE_PAGE_SIZE_OPTIONS,
} from '../../types/initiatives'

export function parseInitiativeListQuery(searchParams: URLSearchParams): InitiativeListQuery {
  const page = parsePositiveInt(searchParams.get('page'), DEFAULT_INITIATIVE_LIST_QUERY.page)
  const rawSize = parsePositiveInt(searchParams.get('size'), DEFAULT_INITIATIVE_LIST_QUERY.size)
  const size = INITIATIVE_PAGE_SIZE_OPTIONS.includes(rawSize as (typeof INITIATIVE_PAGE_SIZE_OPTIONS)[number])
    ? rawSize
    : DEFAULT_INITIATIVE_LIST_QUERY.size
  const sort = searchParams.get('sort')?.trim() || DEFAULT_INITIATIVE_LIST_QUERY.sort

  return {
    page,
    size,
    sort,
    search: searchParams.get('search')?.trim() ?? '',
  }
}

export function buildInitiativeListSearchParams(query: InitiativeListQuery): URLSearchParams {
  const params = new URLSearchParams()

  if (query.page !== DEFAULT_INITIATIVE_LIST_QUERY.page) {
    params.set('page', String(query.page))
  }
  if (query.size !== DEFAULT_INITIATIVE_LIST_QUERY.size) {
    params.set('size', String(query.size))
  }
  if (query.sort !== DEFAULT_INITIATIVE_LIST_QUERY.sort) {
    params.set('sort', query.sort)
  }
  if (query.search) {
    params.set('search', query.search)
  }

  return params
}

export function toInitiativeApiParams(query: InitiativeListQuery): InitiativeListParams {
  const params: InitiativeListParams = {
    page: query.page,
    size: query.size,
    sort: query.sort,
  }

  if (query.search) {
    params.search = query.search
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
