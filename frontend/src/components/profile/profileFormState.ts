import { normalizeEmail } from '../../utils/email'

export interface ProfileFormState {
  fullName: string
  email: string
}

export interface ProfileFormBaseline {
  fullName: string
  email: string
}

export function isProfileFormDirty(form: ProfileFormState, baseline: ProfileFormBaseline | null) {
  if (!baseline) {
    return false
  }

  if (form.fullName.trim() !== baseline.fullName.trim()) {
    return true
  }

  return normalizeEmail(form.email) !== normalizeEmail(baseline.email)
}

export function hasProfileFormValidationErrors(
  form: ProfileFormState,
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
