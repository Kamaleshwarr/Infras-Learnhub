import type {
  TechnologyCategory,
  TechnologyCreateRequest,
  TechnologyDifficulty,
  TechnologyUpdateRequest,
} from '../../types/learn'
import { TECHNOLOGY_CATEGORY_OPTIONS, TECHNOLOGY_DIFFICULTY_OPTIONS } from '../../types/learn'

export type TechnologyFormFieldName =
  | 'name'
  | 'shortName'
  | 'description'
  | 'category'
  | 'difficulty'
  | 'featured'
  | 'linkedProjectIds'

export interface TechnologyFormValues {
  name: string
  shortName: string
  description: string
  category: TechnologyCategory | ''
  difficulty: TechnologyDifficulty | ''
  featured: boolean
  linkedProjectIds: string[]
}

export function createEmptyTechnologyForm(): TechnologyFormValues {
  return {
    name: '',
    shortName: '',
    description: '',
    category: '',
    difficulty: '',
    featured: false,
    linkedProjectIds: [],
  }
}

export function createTechnologyFormBaseline(form: TechnologyFormValues) {
  return JSON.stringify(form)
}

export function isTechnologyFormDirty(form: TechnologyFormValues, baseline: string) {
  return JSON.stringify(form) !== baseline
}

export function getTechnologyFormFieldErrors(
  form: TechnologyFormValues,
): Partial<Record<TechnologyFormFieldName, string>> {
  const errors: Partial<Record<TechnologyFormFieldName, string>> = {}

  if (!form.name.trim()) {
    errors.name = 'Name is required.'
  } else if (form.name.trim().length > 100) {
    errors.name = 'Name must be 100 characters or fewer.'
  }

  if (!form.shortName.trim()) {
    errors.shortName = 'Short name is required.'
  } else if (form.shortName.trim().length > 30) {
    errors.shortName = 'Short name must be 30 characters or fewer.'
  }

  if (form.description.length > 2000) {
    errors.description = 'Description must be 2000 characters or fewer.'
  }

  if (!form.category) {
    errors.category = 'Category is required.'
  } else if (!TECHNOLOGY_CATEGORY_OPTIONS.some((option) => option.value === form.category)) {
    errors.category = 'Category is invalid.'
  }

  if (!form.difficulty) {
    errors.difficulty = 'Difficulty is required.'
  } else if (!TECHNOLOGY_DIFFICULTY_OPTIONS.some((option) => option.value === form.difficulty)) {
    errors.difficulty = 'Difficulty is invalid.'
  }

  return errors
}

export function buildTechnologyCreateRequest(form: TechnologyFormValues): TechnologyCreateRequest {
  return {
    name: form.name.trim(),
    shortName: form.shortName.trim(),
    description: form.description.trim() || null,
    category: form.category as TechnologyCategory,
    difficulty: form.difficulty as TechnologyDifficulty,
  }
}

export function buildTechnologyUpdateRequest(form: TechnologyFormValues): TechnologyUpdateRequest {
  return {
    ...buildTechnologyCreateRequest(form),
    featured: form.featured,
  }
}

export function technologyToFormValues(technology: {
  name: string
  shortName: string
  description?: string | null
  category: TechnologyCategory
  difficulty: TechnologyDifficulty
  featured: boolean
  relatedProjects?: Array<{ id: string }>
}): TechnologyFormValues {
  return {
    name: technology.name,
    shortName: technology.shortName,
    description: technology.description ?? '',
    category: technology.category,
    difficulty: technology.difficulty,
    featured: technology.featured,
    linkedProjectIds: technology.relatedProjects?.map((project) => project.id) ?? [],
  }
}
