export interface AdminReviewListQuery {
  page: number
  size: number
  sort: string
}

export const DEFAULT_ADMIN_REVIEW_QUERY: AdminReviewListQuery = {
  page: 0,
  size: 20,
  sort: 'submittedAtUtc,desc',
}

export function parseAdminReviewListQuery(searchParams: URLSearchParams): AdminReviewListQuery {
  const page = Number.parseInt(searchParams.get('page') ?? String(DEFAULT_ADMIN_REVIEW_QUERY.page), 10)
  const size = Number.parseInt(searchParams.get('size') ?? String(DEFAULT_ADMIN_REVIEW_QUERY.size), 10)

  return {
    page: Number.isFinite(page) && page >= 0 ? page : DEFAULT_ADMIN_REVIEW_QUERY.page,
    size: Number.isFinite(size) && size > 0 ? size : DEFAULT_ADMIN_REVIEW_QUERY.size,
    sort: searchParams.get('sort') ?? DEFAULT_ADMIN_REVIEW_QUERY.sort,
  }
}

export function buildAdminReviewSearchParams(query: AdminReviewListQuery) {
  const params = new URLSearchParams()
  params.set('page', String(query.page))
  params.set('size', String(query.size))
  params.set('sort', query.sort)
  return params
}

export function toAdminReviewApiParams(query: AdminReviewListQuery) {
  return {
    page: query.page,
    size: query.size,
    sort: query.sort,
    status: 'SUBMITTED' as const,
  }
}
