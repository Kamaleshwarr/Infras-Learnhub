import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material'
import { usersApi } from '../../api/usersApi'
import {
  isPasswordPolicyCompliant,
  isPasswordSameAsEmail,
  PASSWORD_POLICY_MESSAGE,
} from '../../types/auth'
import type { UserRole } from '../../types/auth'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { normalizeEmail } from '../../utils/email'
import { normalizeEmployeeId } from '../../utils/employeeId'

interface CreateUserDialogProps {
  open: boolean
  onClose: () => void
  onSuccess: () => void
}

const EMPTY_FORM = {
  employeeId: '',
  fullName: '',
  email: '',
  role: 'EMPLOYEE' as UserRole,
  password: '',
  confirmPassword: '',
}

export function CreateUserDialog({ open, onClose, onSuccess }: CreateUserDialogProps) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (open) {
      setForm({ ...EMPTY_FORM })
      setFieldErrors({})
      setFormError(null)
      setSubmitting(false)
    }
  }, [open])

  const normalizedEmail = normalizeEmail(form.email)
  const policyCompliant = isPasswordPolicyCompliant(form.password)
  const passwordsMatch = form.password === form.confirmPassword
  const passwordMatchesEmail = isPasswordSameAsEmail(form.password, normalizedEmail)
  const canSubmit =
    form.employeeId.trim().length > 0 &&
    form.fullName.trim().length > 0 &&
    normalizedEmail.length > 0 &&
    policyCompliant &&
    form.confirmPassword.length >= 8 &&
    passwordsMatch &&
    !passwordMatchesEmail &&
    !submitting

  function updateField<K extends keyof typeof form>(field: K, value: (typeof form)[K]) {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      if (!(field in current)) {
        return current
      }
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canSubmit) {
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      await usersApi.create({
        employeeId: normalizeEmployeeId(form.employeeId),
        fullName: form.fullName.trim(),
        email: normalizedEmail,
        role: form.role,
        password: form.password,
      })
      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to create user. Please try again.'))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={submitting ? undefined : onClose} open={open}>
      <DialogTitle>Create User</DialogTitle>
      <form autoComplete="off" onSubmit={handleSubmit}>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {formError ? <Alert severity="error">{formError}</Alert> : null}
            <TextField
              autoComplete="off"
              autoFocus
              disabled={submitting}
              error={Boolean(fieldErrors.employeeId)}
              helperText={fieldErrors.employeeId}
              label="Employee ID"
              name="newEmployeeId"
              slotProps={{ htmlInput: { maxLength: 64 } }}
              onChange={(event) => updateField('employeeId', event.target.value)}
              required
              value={form.employeeId}
            />
            <TextField
              autoComplete="off"
              disabled={submitting}
              error={Boolean(fieldErrors.fullName)}
              helperText={fieldErrors.fullName}
              label="Full Name"
              name="newFullName"
              slotProps={{ htmlInput: { maxLength: 200 } }}
              onChange={(event) => updateField('fullName', event.target.value)}
              required
              value={form.fullName}
            />
            <TextField
              autoComplete="off"
              disabled={submitting}
              error={Boolean(fieldErrors.email)}
              helperText={fieldErrors.email}
              label="Email"
              name="newEmail"
              slotProps={{ htmlInput: { maxLength: 320 } }}
              onChange={(event) => updateField('email', event.target.value)}
              required
              type="text"
              value={form.email}
            />
            <TextField
              disabled={submitting}
              error={Boolean(fieldErrors.role)}
              helperText={fieldErrors.role}
              label="Role"
              onChange={(event) => updateField('role', event.target.value as UserRole)}
              required
              select
              value={form.role}
            >
              <MenuItem value="EMPLOYEE">Employee</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </TextField>
            <TextField
              autoComplete="new-password"
              disabled={submitting}
              error={
                Boolean(fieldErrors.password) ||
                (form.password.length > 0 && (!policyCompliant || passwordMatchesEmail))
              }
              helperText={
                fieldErrors.password ||
                (form.password.length > 0 && passwordMatchesEmail
                  ? 'Password must not match the email address.'
                  : PASSWORD_POLICY_MESSAGE)
              }
              label="Password"
              name="newPassword"
              onChange={(event) => updateField('password', event.target.value)}
              required
              type="password"
              value={form.password}
            />
            <TextField
              autoComplete="new-password"
              disabled={submitting}
              error={form.confirmPassword.length > 0 && !passwordsMatch}
              helperText={
                form.confirmPassword.length > 0 && !passwordsMatch ? 'Passwords do not match.' : ' '
              }
              label="Confirm Password"
              name="newConfirmPassword"
              onChange={(event) => updateField('confirmPassword', event.target.value)}
              required
              type="password"
              value={form.confirmPassword}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button disabled={submitting} onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={!canSubmit} type="submit" variant="contained">
            {submitting ? <CircularProgress color="inherit" size={24} /> : 'Save'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
