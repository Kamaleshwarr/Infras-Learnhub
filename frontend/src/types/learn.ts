export type TechnologyStatus = 'HIDDEN' | 'PUBLISHED' | 'ARCHIVED'

export type TechnologyCategory =
  | 'BACKEND'
  | 'FRONTEND'
  | 'CLOUD'
  | 'DEVOPS'
  | 'DATABASE'
  | 'AI_AND_GENAI'
  | 'TESTING'
  | 'SECURITY'
  | 'MOBILE'
  | 'ARCHITECTURE'
  | 'DATA_ENGINEERING'

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
  slug: string
  name: string
  shortName: string
  description?: string | null
  category: TechnologyCategory
  difficulty: TechnologyDifficulty
  status: TechnologyStatus
  featured: boolean
  featuredOverride?: boolean | null
  catalogFeatured: boolean
  estimatedDuration?: string | null
  officialWebsite?: string | null
  officialDocumentation?: string | null
  tags?: string[]
  orgNotes?: string | null
  catalogVersion?: string | null
  catalogSource?: string | null
  catalogPresent: boolean
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
  'slug',
  'category',
  'difficulty',
  'status',
  'featured',
  'createdAtUtc',
  'updatedAtUtc',
] as const

export type TechnologySortableColumn = (typeof TECHNOLOGY_SORTABLE_COLUMNS)[number]

export interface TechnologyCurationRequest {
  featured?: boolean
  status?: TechnologyStatus
  orgNotes?: string | null
}

export interface CatalogStatus {
  catalogVersion: string
  importedAt: string
  packageType: string
  recordsUpserted: number
  status: string
  technologyCount: number
}

export type TechnologyLifecycleAction = 'publish' | 'hide' | 'archive'

export const TECHNOLOGY_CATEGORY_OPTIONS: Array<{ value: TechnologyCategory; label: string }> = [
  { value: 'BACKEND', label: 'Backend' },
  { value: 'FRONTEND', label: 'Frontend' },
  { value: 'CLOUD', label: 'Cloud' },
  { value: 'DEVOPS', label: 'DevOps' },
  { value: 'DATABASE', label: 'Database' },
  { value: 'AI_AND_GENAI', label: 'AI & GenAI' },
  { value: 'TESTING', label: 'Testing' },
  { value: 'SECURITY', label: 'Security' },
  { value: 'MOBILE', label: 'Mobile' },
  { value: 'ARCHITECTURE', label: 'Architecture' },
  { value: 'DATA_ENGINEERING', label: 'Data Engineering' },
]

export const TECHNOLOGY_DIFFICULTY_OPTIONS: Array<{ value: TechnologyDifficulty; label: string }> = [
  { value: 'BEGINNER', label: 'Beginner' },
  { value: 'INTERMEDIATE', label: 'Intermediate' },
  { value: 'ADVANCED', label: 'Advanced' },
]
