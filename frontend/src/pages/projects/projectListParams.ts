import type { ProjectAccessType, ProjectListQuery, ProjectStatus } from '../../types/projects'
import { DEFAULT_PROJECT_LIST_QUERY } from '../../types/projects'

export function parseProjectListQuery(searchParams: URLSearchParams): ProjectListQuery {
  const status = searchParams.get('status') as ProjectStatus | ''
  const accessType = searchParams.get('accessType') as ProjectAccessType | ''

  return {
    page: Number(searchParams.get('page') ?? DEFAULT_PROJECT_LIST_QUERY.page),
    size: Number(searchParams.get('size') ?? DEFAULT_PROJECT_LIST_QUERY.size),
    search: searchParams.get('search') ?? DEFAULT_PROJECT_LIST_QUERY.search,
    status: status && ['ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'].includes(status) ? status : '',
    accessType: accessType && ['PUBLIC', 'MEMBERS_ONLY'].includes(accessType) ? accessType : '',
    assigned: searchParams.get('assigned') === 'true',
    includeArchived: searchParams.get('includeArchived') === 'true',
    sort: searchParams.get('sort') ?? DEFAULT_PROJECT_LIST_QUERY.sort,
  }
}

export function buildProjectListSearchParams(query: ProjectListQuery) {
  const params = new URLSearchParams()
  if (query.search) {
    params.set('search', query.search)
  }
  if (query.status) {
    params.set('status', query.status)
  }
  if (query.accessType) {
    params.set('accessType', query.accessType)
  }
  if (query.assigned) {
    params.set('assigned', 'true')
  }
  if (query.includeArchived) {
    params.set('includeArchived', 'true')
  }
  if (query.page > 0) {
    params.set('page', String(query.page))
  }
  if (query.size !== DEFAULT_PROJECT_LIST_QUERY.size) {
    params.set('size', String(query.size))
  }
  if (query.sort !== DEFAULT_PROJECT_LIST_QUERY.sort) {
    params.set('sort', query.sort)
  }
  return params
}

export function toProjectApiParams(query: ProjectListQuery) {
  const [sortField, sortDirection] = query.sort.split(',')
  return {
    search: query.search || undefined,
    status: query.status || undefined,
    accessType: query.accessType || undefined,
    assigned: query.assigned || undefined,
    includeArchived: query.includeArchived || undefined,
    page: query.page,
    size: query.size,
    sort: `${sortField},${sortDirection ?? 'asc'}`,
  }
}

export function toggleSort(currentSort: string, field: string) {
  const [currentField, currentDirection] = currentSort.split(',')
  if (currentField === field) {
    return `${field},${currentDirection === 'asc' ? 'desc' : 'asc'}`
  }
  return `${field},asc`
}
