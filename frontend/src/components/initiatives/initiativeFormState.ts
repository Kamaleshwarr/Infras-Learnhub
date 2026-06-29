import type {
  CreateInitiativeRequest,
  Initiative,
  InitiativeStatus,
  UpdateInitiativeRequest,
} from '../../types/initiatives'
import {
  defaultExpiryUtcDateInput,
  isUtcDateBefore,
  isUtcDateOnOrAfter,
  todayUtcDateInput,
  utcDateInputToInstant,
} from './initiativeDateUtils'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

export const INITIATIVE_FORM_LIMITS = {
  title: 100,
  description: 2000,
  rewardDescription: 500,
} as const

export const INITIATIVE_STATUS_OPTIONS: InitiativeStatus[] = ['DRAFT', 'ACTIVE', 'EXPIRED']

export interface InitiativeFormValues {
  title: string
  description: string
  rewardDescription: string
  startDate: string
  expiryDate: string
  status: InitiativeStatus
}

export interface InitiativeFormBaseline {
  title: string
  description: string
  rewardDescription: string
  startDate: string
  expiryDate: string
  status: InitiativeStatus
}

export type InitiativeFormFieldName = keyof InitiativeFormValues

export interface InitiativeFormValidationOptions {
  now?: number
  mode?: 'create' | 'edit'
}

export function createEmptyInitiativeForm(now = Date.now()): InitiativeFormValues {
  const startDate = todayUtcDateInput(now)
  return {
    title: '',
    description: '',
    rewardDescription: '',
    startDate,
    expiryDate: defaultExpiryUtcDateInput(now),
    status: 'DRAFT',
  }
}

export function initiativeToFormValues(initiative: Initiative): InitiativeFormValues {
  return {
    title: initiative.title,
    description: initiative.description,
    rewardDescription: initiative.rewardDescription ?? '',
    startDate: instantToFormDate(initiative.startDateUtc),
    expiryDate: instantToFormDate(initiative.expiryDateUtc),
    status: initiative.status,
  }
}

export function createInitiativeFormBaseline(values: InitiativeFormValues): InitiativeFormBaseline {
  return normalizeInitiativeFormValues(values)
}

export function buildCreateInitiativeRequest(values: InitiativeFormValues): CreateInitiativeRequest {
  return buildUpsertInitiativeRequest(values)
}

export function buildUpdateInitiativeRequest(values: InitiativeFormValues): UpdateInitiativeRequest {
  return buildUpsertInitiativeRequest(values)
}

export function getInitiativeFormFieldErrors(
  values: InitiativeFormValues,
  options: InitiativeFormValidationOptions = {},
): Partial<Record<InitiativeFormFieldName, string>> {
  const errors: Partial<Record<InitiativeFormFieldName, string>> = {}
  const now = options.now ?? Date.now()
  const mode = options.mode ?? 'edit'

  if (!values.title.trim()) {
    errors.title = INITIATIVE_MESSAGES.formTitleRequired
  } else if (values.title.trim().length > INITIATIVE_FORM_LIMITS.title) {
    errors.title = INITIATIVE_MESSAGES.formTitleTooLong
  }

  if (!values.description.trim()) {
    errors.description = INITIATIVE_MESSAGES.formDescriptionRequired
  } else if (values.description.trim().length > INITIATIVE_FORM_LIMITS.description) {
    errors.description = INITIATIVE_MESSAGES.formDescriptionTooLong
  }

  if (values.rewardDescription.trim().length > INITIATIVE_FORM_LIMITS.rewardDescription) {
    errors.rewardDescription = INITIATIVE_MESSAGES.formRewardTooLong
  }

  if (!values.startDate) {
    errors.startDate = INITIATIVE_MESSAGES.formStartDateRequired
  } else if (mode === 'create' && isUtcDateBefore(values.startDate, todayUtcDateInput(now))) {
    errors.startDate = INITIATIVE_MESSAGES.formStartDateBeforeToday
  }

  if (!values.expiryDate) {
    errors.expiryDate = INITIATIVE_MESSAGES.formExpiryDateRequired
  } else if (
    values.startDate &&
    values.expiryDate &&
    !isUtcDateOnOrAfter(values.expiryDate, values.startDate)
  ) {
    errors.expiryDate = INITIATIVE_MESSAGES.formDateRangeInvalid
  }

  return errors
}

export function isInitiativeFormValid(values: InitiativeFormValues, options: InitiativeFormValidationOptions = {}) {
  return Object.keys(getInitiativeFormFieldErrors(values, options)).length === 0
}

export function isInitiativeFormDirty(
  values: InitiativeFormValues,
  baseline: InitiativeFormBaseline | null,
) {
  if (!baseline) {
    return false
  }

  const normalized = normalizeInitiativeFormValues(values)
  return (
    normalized.title !== baseline.title ||
    normalized.description !== baseline.description ||
    normalized.rewardDescription !== baseline.rewardDescription ||
    normalized.startDate !== baseline.startDate ||
    normalized.expiryDate !== baseline.expiryDate ||
    normalized.status !== baseline.status
  )
}

function buildUpsertInitiativeRequest(values: InitiativeFormValues): CreateInitiativeRequest {
  const normalized = normalizeInitiativeFormValues(values)
  const rewardDescription = normalized.rewardDescription.length > 0 ? normalized.rewardDescription : null

  return {
    description: normalized.description,
    expiryDateUtc: utcDateInputToInstant(normalized.expiryDate),
    rewardDescription,
    startDateUtc: utcDateInputToInstant(normalized.startDate),
    status: normalized.status,
    title: normalized.title,
  }
}

function normalizeInitiativeFormValues(values: InitiativeFormValues): InitiativeFormBaseline {
  return {
    title: values.title.trim(),
    description: values.description.trim(),
    rewardDescription: values.rewardDescription.trim(),
    startDate: values.startDate,
    expiryDate: values.expiryDate,
    status: values.status,
  }
}

function instantToFormDate(instant: string) {
  const parsed = Date.parse(instant)
  if (!Number.isFinite(parsed)) {
    return todayUtcDateInput()
  }

  const date = new Date(parsed)
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  const day = String(date.getUTCDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
