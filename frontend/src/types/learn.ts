export type TechnologyStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'

export type TechnologyCategory =
  | 'CLOUD'
  | 'LANGUAGES'
  | 'DEVOPS'
  | 'DATA'
  | 'SECURITY'
  | 'PLATFORM'
  | 'OTHER'

export type TechnologyDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

export interface TechnologyCreatedBy {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface RelatedProjectSummary {
  id: string
  name: string
}

export interface RelatedTechnologySummary {
  id: string
  name: string
  shortName: string
}

export interface Technology {
  id: string
  name: string
  shortName: string
  description?: string | null
  category: TechnologyCategory
  difficulty: TechnologyDifficulty
  status: TechnologyStatus
  featured: boolean
  relatedProjects?: RelatedProjectSummary[]
  createdBy?: TechnologyCreatedBy
  createdAtUtc?: string
  updatedAtUtc?: string
}

export interface TechnologyListParams {
  page?: number
  size?: number
  sort?: string
  search?: string
  category?: TechnologyCategory
  difficulty?: TechnologyDifficulty
  status?: TechnologyStatus
}

export type TechnologyStatusFilter = '' | TechnologyStatus

export interface TechnologyListQuery {
  page: number
  size: number
  sort: string
  search: string
  category: TechnologyCategory | ''
  difficulty: TechnologyDifficulty | ''
  status: TechnologyStatusFilter
}

export const DEFAULT_TECHNOLOGY_LIST_QUERY: TechnologyListQuery = {
  page: 0,
  size: 20,
  sort: 'name,asc',
  search: '',
  category: '',
  difficulty: '',
  status: '',
}

export const TECHNOLOGY_PAGE_SIZE_OPTIONS = [10, 20, 50] as const

export const TECHNOLOGY_SORTABLE_COLUMNS = [
  'name',
  'category',
  'difficulty',
  'status',
  'featured',
  'createdAtUtc',
  'updatedAtUtc',
] as const

export type TechnologySortableColumn = (typeof TECHNOLOGY_SORTABLE_COLUMNS)[number]

export interface TechnologyCreateRequest {
  name: string
  shortName: string
  description?: string | null
  category: TechnologyCategory
  difficulty: TechnologyDifficulty
}

export interface TechnologyUpdateRequest extends TechnologyCreateRequest {
  featured?: boolean
}

export type TechnologyLifecycleAction = 'publish' | 'archive'

export const TECHNOLOGY_CATEGORY_OPTIONS: Array<{ value: TechnologyCategory; label: string }> = [
  { value: 'CLOUD', label: 'Cloud' },
  { value: 'LANGUAGES', label: 'Languages' },
  { value: 'DEVOPS', label: 'DevOps' },
  { value: 'DATA', label: 'Data' },
  { value: 'SECURITY', label: 'Security' },
  { value: 'PLATFORM', label: 'Platform' },
  { value: 'OTHER', label: 'Other' },
]

export const TECHNOLOGY_DIFFICULTY_OPTIONS: Array<{ value: TechnologyDifficulty; label: string }> = [
  { value: 'BEGINNER', label: 'Beginner' },
  { value: 'INTERMEDIATE', label: 'Intermediate' },
  { value: 'ADVANCED', label: 'Advanced' },
]
