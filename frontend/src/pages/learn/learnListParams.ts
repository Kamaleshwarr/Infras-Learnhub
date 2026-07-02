import type {
  TechnologyCategory,
  TechnologyDifficulty,
  TechnologyListParams,
  TechnologyListQuery,
} from '../../types/learn'
import {
  DEFAULT_TECHNOLOGY_LIST_QUERY,
  TECHNOLOGY_PAGE_SIZE_OPTIONS,
} from '../../types/learn'

export function parseTechnologyListQuery(searchParams: URLSearchParams): TechnologyListQuery {
  const page = parsePositiveInt(searchParams.get('page'), DEFAULT_TECHNOLOGY_LIST_QUERY.page)
  const rawSize = parsePositiveInt(searchParams.get('size'), DEFAULT_TECHNOLOGY_LIST_QUERY.size)
  const size = TECHNOLOGY_PAGE_SIZE_OPTIONS.includes(rawSize as (typeof TECHNOLOGY_PAGE_SIZE_OPTIONS)[number])
    ? rawSize
    : DEFAULT_TECHNOLOGY_LIST_QUERY.size
  const sort = searchParams.get('sort')?.trim() || DEFAULT_TECHNOLOGY_LIST_QUERY.sort

  return {
    page,
    size,
    sort,
    search: searchParams.get('search')?.trim() ?? '',
    category: parseCategory(searchParams.get('category')),
    difficulty: parseDifficulty(searchParams.get('difficulty')),
    status: parseStatus(searchParams.get('status')),
  }
}

export function buildTechnologyListSearchParams(query: TechnologyListQuery): URLSearchParams {
  const params = new URLSearchParams()

  if (query.page !== DEFAULT_TECHNOLOGY_LIST_QUERY.page) {
    params.set('page', String(query.page))
  }
  if (query.size !== DEFAULT_TECHNOLOGY_LIST_QUERY.size) {
    params.set('size', String(query.size))
  }
  if (query.sort !== DEFAULT_TECHNOLOGY_LIST_QUERY.sort) {
    params.set('sort', query.sort)
  }
  if (query.search) {
    params.set('search', query.search)
  }
  if (query.category) {
    params.set('category', query.category)
  }
  if (query.difficulty) {
    params.set('difficulty', query.difficulty)
  }
  if (query.status) {
    params.set('status', query.status)
  }

  return params
}

export function toTechnologyApiParams(
  query: TechnologyListQuery,
  options?: { isAdmin?: boolean },
): TechnologyListParams {
  const params: TechnologyListParams = {
    page: query.page,
    size: query.size,
    sort: query.sort,
  }

  if (query.search) {
    params.search = query.search
  }
  if (query.category) {
    params.category = query.category
  }
  if (query.difficulty) {
    params.difficulty = query.difficulty
  }
  if (options?.isAdmin && query.status) {
    params.status = query.status
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

function parseStatus(value: string | null): TechnologyListQuery['status'] {
  if (value === 'HIDDEN' || value === 'PUBLISHED' || value === 'ARCHIVED') {
    return value
  }
  return ''
}

function parseCategory(value: string | null): TechnologyCategory | '' {
  if (
    value === 'BACKEND' ||
    value === 'FRONTEND' ||
    value === 'CLOUD' ||
    value === 'DEVOPS' ||
    value === 'DATABASE' ||
    value === 'AI_AND_GENAI' ||
    value === 'TESTING' ||
    value === 'SECURITY' ||
    value === 'MOBILE' ||
    value === 'ARCHITECTURE' ||
    value === 'DATA_ENGINEERING'
  ) {
    return value
  }
  return ''
}

function parseDifficulty(value: string | null): TechnologyDifficulty | '' {
  if (value === 'BEGINNER' || value === 'INTERMEDIATE' || value === 'ADVANCED') {
    return value
  }
  return ''
}

function parsePositiveInt(value: string | null, fallback: number) {
  if (!value) {
    return fallback
  }
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback
}

export const TECHNOLOGY_STATUS_TABS: Array<{ label: string; value: TechnologyListQuery['status'] }> = [
  { label: 'All', value: '' },
  { label: 'Hidden', value: 'HIDDEN' },
  { label: 'Published', value: 'PUBLISHED' },
  { label: 'Archived', value: 'ARCHIVED' },
]
