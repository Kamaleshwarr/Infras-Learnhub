export type InitiativeStatus = 'DRAFT' | 'ACTIVE' | 'EXPIRED'

export interface InitiativeCreatedBy {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface Initiative {
  id: string
  title: string
  description: string
  rewardDescription?: string | null
  startDateUtc: string
  expiryDateUtc: string
  status: InitiativeStatus
  createdBy?: InitiativeCreatedBy
  createdAtUtc?: string
  updatedAtUtc?: string
}

/** @deprecated Use `Initiative` — retained until all initiative UI work is migrated. */
export type InitiativeSummary = Initiative

export interface InitiativeListParams {
  page?: number
  size?: number
  sort?: string
  status?: InitiativeStatus
  search?: string
}

export type InitiativeStatusFilter = '' | InitiativeStatus

export interface InitiativeListQuery {
  page: number
  size: number
  sort: string
  search: string
  status: InitiativeStatusFilter
}

export const DEFAULT_INITIATIVE_LIST_QUERY: InitiativeListQuery = {
  page: 0,
  size: 20,
  sort: 'expiryDateUtc,asc',
  search: '',
  status: '',
}

export const INITIATIVE_PAGE_SIZE_OPTIONS = [10, 20, 50] as const

export const INITIATIVE_SORTABLE_COLUMNS = ['title', 'expiryDateUtc', 'startDateUtc', 'createdAtUtc', 'updatedAtUtc'] as const

export type InitiativeSortableColumn = (typeof INITIATIVE_SORTABLE_COLUMNS)[number]

export interface UpsertInitiativeRequest {
  title: string
  description: string
  rewardDescription?: string | null
  startDateUtc: string
  expiryDateUtc: string
}

export type CreateInitiativeRequest = UpsertInitiativeRequest

export type UpdateInitiativeRequest = UpsertInitiativeRequest

export interface ReactivateInitiativeRequest {
  expiryDateUtc: string
}

export type InitiativeLifecycleAction = 'publish' | 'returnToDraft' | 'markExpired' | 'reactivate'
