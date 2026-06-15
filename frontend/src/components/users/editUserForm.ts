import type { UserRole } from '../../types/auth'
import { normalizeEmail } from '../../utils/email'

export interface EditFormState {
  fullName: string
  email: string
  role: UserRole
}

export interface EditFormBaseline {
  fullName: string
  email: string
  role: UserRole
}

export function isEditFormDirty(
  form: EditFormState,
  baseline: EditFormBaseline | null,
  includeRole: boolean,
) {
  if (!baseline) {
    return false
  }

  if (form.fullName.trim() !== baseline.fullName.trim()) {
    return true
  }

  if (normalizeEmail(form.email) !== normalizeEmail(baseline.email)) {
    return true
  }

  if (includeRole && form.role !== baseline.role) {
    return true
  }

  return false
}

export function hasEditFormValidationErrors(
  form: EditFormState,
  fieldErrors: Record<string, string>,
) {
  if (form.fullName.trim().length === 0) {
    return true
  }

  if (normalizeEmail(form.email).length === 0) {
    return true
  }

  return Object.keys(fieldErrors).length > 0
}
