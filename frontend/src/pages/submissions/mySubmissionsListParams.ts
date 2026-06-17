import type { ApprovalStatus } from '../../types/submissions'

export type SubmissionStatusFilter = 'ALL' | ApprovalStatus

export interface MySubmissionsListQuery {
  page: number
  size: number
  sort: string
  status: SubmissionStatusFilter
}

export const DEFAULT_MY_SUBMISSIONS_QUERY: MySubmissionsListQuery = {
  page: 0,
  size: 20,
  sort: 'submittedAtUtc,desc',
  status: 'ALL',
}

const STATUS_FILTERS = new Set<SubmissionStatusFilter>(['ALL', 'SUBMITTED', 'APPROVED', 'REJECTED'])

export function parseMySubmissionsListQuery(searchParams: URLSearchParams): MySubmissionsListQuery {
  const statusParam = searchParams.get('status')?.toUpperCase() ?? 'ALL'
  const status = STATUS_FILTERS.has(statusParam as SubmissionStatusFilter)
    ? (statusParam as SubmissionStatusFilter)
    : DEFAULT_MY_SUBMISSIONS_QUERY.status

  const page = Number.parseInt(searchParams.get('page') ?? String(DEFAULT_MY_SUBMISSIONS_QUERY.page), 10)
  const size = Number.parseInt(searchParams.get('size') ?? String(DEFAULT_MY_SUBMISSIONS_QUERY.size), 10)

  return {
    page: Number.isFinite(page) && page >= 0 ? page : DEFAULT_MY_SUBMISSIONS_QUERY.page,
    size: Number.isFinite(size) && size > 0 ? size : DEFAULT_MY_SUBMISSIONS_QUERY.size,
    sort: searchParams.get('sort') ?? DEFAULT_MY_SUBMISSIONS_QUERY.sort,
    status,
  }
}

export function buildMySubmissionsSearchParams(query: MySubmissionsListQuery) {
  const params = new URLSearchParams()
  params.set('page', String(query.page))
  params.set('size', String(query.size))
  params.set('sort', query.sort)
  if (query.status !== 'ALL') {
    params.set('status', query.status)
  }
  return params
}

export function toMySubmissionsApiParams(query: MySubmissionsListQuery) {
  return {
    page: query.page,
    size: query.size,
    sort: query.sort,
    ...(query.status === 'ALL' ? {} : { status: query.status }),
  }
}
