import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { usersApi } from '../../api/usersApi'
import {
  isPasswordPolicyCompliant,
  isPasswordSameAsEmail,
  PASSWORD_POLICY_MESSAGE,
} from '../../types/auth'
import type { UserSummary } from '../../types/users'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { normalizeEmail } from '../../utils/email'

interface ResetPasswordDialogProps {
  open: boolean
  user: UserSummary | null
  onClose: () => void
  onSuccess: () => void
}

type ResetPasswordStep = 'confirm' | 'password'

const EMPTY_FORM = {
  password: '',
  confirmPassword: '',
}

export function ResetPasswordDialog({ open, user, onClose, onSuccess }: ResetPasswordDialogProps) {
  const [step, setStep] = useState<ResetPasswordStep>('confirm')
  const [form, setForm] = useState(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (open) {
      setStep('confirm')
      setForm({ ...EMPTY_FORM })
      setFieldErrors({})
      setFormError(null)
      setSubmitting(false)
    }
  }, [open, user?.id])

  const targetEmail = normalizeEmail(user?.email ?? '')
  const policyCompliant = isPasswordPolicyCompliant(form.password)
  const passwordsMatch = form.password === form.confirmPassword
  const passwordMatchesEmail = isPasswordSameAsEmail(form.password, targetEmail)
  const canSubmit =
    Boolean(user) &&
    policyCompliant &&
    form.confirmPassword.length >= 8 &&
    passwordsMatch &&
    !passwordMatchesEmail &&
    !submitting

  function updateField(field: 'password' | 'confirmPassword', value: string) {
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
    if (!user || !canSubmit) {
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      await usersApi.resetPassword(user.id, { password: form.password })
      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to reset password. Please try again.'))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  function handleClose() {
    if (!submitting) {
      onClose()
    }
  }

  return (
    <Dialog
      aria-labelledby="reset-password-title"
      fullWidth
      maxWidth="sm"
      onClose={submitting ? undefined : handleClose}
      open={open}
    >
      <DialogTitle id="reset-password-title">Reset Password</DialogTitle>
      {step === 'confirm' ? (
        <>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {user ? (
                <Box
                  sx={{
                    bgcolor: 'action.hover',
                    borderRadius: 1,
                    p: 2,
                  }}
                >
                  <Typography variant="subtitle2">{user.fullName}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    {user.employeeId} · {user.email}
                  </Typography>
                </Box>
              ) : null}
              <Alert severity="info">
                The user will be required to change this password on next sign-in.
              </Alert>
              <Typography color="text.secondary" variant="body2">
                You are about to set a new temporary password for this account. Confirm to continue.
              </Typography>
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button disabled={submitting} onClick={handleClose}>
              Cancel
            </Button>
            <Button disabled={!user} onClick={() => setStep('password')} variant="contained">
              Continue
            </Button>
          </DialogActions>
        </>
      ) : (
        <form autoComplete="off" onSubmit={handleSubmit}>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {formError ? <Alert severity="error">{formError}</Alert> : null}
              <Alert severity="info">
                User will be required to change their password at next sign-in.
              </Alert>
              <TextField
                autoComplete="new-password"
                autoFocus
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
                label="New Password"
                name="resetPassword"
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
                name="resetConfirmPassword"
                onChange={(event) => updateField('confirmPassword', event.target.value)}
                required
                type="password"
                value={form.confirmPassword}
              />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button disabled={submitting} onClick={() => setStep('confirm')}>
              Back
            </Button>
            <Button disabled={submitting} onClick={handleClose}>
              Cancel
            </Button>
            <Button disabled={!canSubmit} type="submit" variant="contained">
              {submitting ? <CircularProgress color="inherit" size={24} /> : 'Reset password'}
            </Button>
          </DialogActions>
        </form>
      )}
    </Dialog>
  )
}
