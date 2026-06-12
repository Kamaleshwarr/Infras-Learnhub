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
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { usersApi } from '../../api/usersApi'
import type { UserRole } from '../../types/auth'
import type { UserSummary } from '../../types/users'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { normalizeEmail } from '../../utils/email'
import { UserStatusChip } from './UserStatusChip'

interface EditUserDialogProps {
  open: boolean
  user: UserSummary | null
  currentUserId?: string
  onClose: () => void
  onSuccess: () => void
}

interface EditFormState {
  fullName: string
  email: string
  role: UserRole
}

export function EditUserDialog({ open, user, currentUserId, onClose, onSuccess }: EditUserDialogProps) {
  const [form, setForm] = useState<EditFormState>({ fullName: '', email: '', role: 'EMPLOYEE' })
  const [loadedUser, setLoadedUser] = useState<UserSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const isEditingSelf = Boolean(user && currentUserId && user.id === currentUserId)
  const normalizedEmail = normalizeEmail(form.email)
  const canSubmit =
    form.fullName.trim().length > 0 &&
    normalizedEmail.length > 0 &&
    !loading &&
    !submitting

  useEffect(() => {
    if (!open || !user) {
      setLoadedUser(null)
      setForm({ fullName: '', email: '', role: 'EMPLOYEE' })
      setFieldErrors({})
      setFormError(null)
      setLoading(false)
      setSubmitting(false)
      return
    }

    const selectedUser = user

    setForm({
      fullName: selectedUser.fullName,
      email: selectedUser.email,
      role: selectedUser.role,
    })
    setLoadedUser(selectedUser)

    let mounted = true

    async function loadUser() {
      setLoading(true)
      setFormError(null)
      try {
        const freshUser = await usersApi.get(selectedUser.id)
        if (mounted) {
          setLoadedUser(freshUser)
          setForm({
            fullName: freshUser.fullName,
            email: freshUser.email,
            role: freshUser.role,
          })
        }
      } catch (error) {
        if (mounted) {
          setFormError(resolveApiError(error, 'Unable to load user details. Please try again.'))
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    loadUser()
    return () => {
      mounted = false
    }
  }, [open, user])

  function updateField<K extends keyof EditFormState>(field: K, value: EditFormState[K]) {
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
    if (!canSubmit || !loadedUser) {
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      await usersApi.update(loadedUser.id, {
        fullName: form.fullName.trim(),
        email: normalizedEmail,
        role: form.role,
      })
      onSuccess()
      onClose()
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to update user. Please try again.'))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={submitting ? undefined : onClose} open={open}>
      <DialogTitle>Edit User</DialogTitle>
      <form onSubmit={handleSubmit}>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {formError ? <Alert severity="error">{formError}</Alert> : null}
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
                <CircularProgress />
              </Box>
            ) : (
              <>
                <TextField
                  disabled
                  label="Employee ID"
                  value={loadedUser?.employeeId ?? ''}
                />
                <TextField
                  autoFocus
                  disabled={submitting}
                  error={Boolean(fieldErrors.fullName)}
                  helperText={fieldErrors.fullName}
                  label="Full Name"
                  slotProps={{ htmlInput: { maxLength: 200 } }}
                  onChange={(event) => updateField('fullName', event.target.value)}
                  required
                  value={form.fullName}
                />
                <TextField
                  disabled={submitting}
                  error={Boolean(fieldErrors.email)}
                  helperText={fieldErrors.email}
                  label="Email"
                  slotProps={{ htmlInput: { maxLength: 320 } }}
                  onChange={(event) => updateField('email', event.target.value)}
                  required
                  type="email"
                  value={form.email}
                />
                <TextField
                  disabled={submitting || isEditingSelf}
                  error={Boolean(fieldErrors.role)}
                  helperText={
                    fieldErrors.role ||
                    (isEditingSelf
                      ? 'You cannot change your own role. Ask another administrator if a role change is required.'
                      : undefined)
                  }
                  label="Role"
                  onChange={(event) => updateField('role', event.target.value as UserRole)}
                  required
                  select
                  value={form.role}
                >
                  <MenuItem value="EMPLOYEE">Employee</MenuItem>
                  <MenuItem value="ADMIN">Admin</MenuItem>
                </TextField>
                {loadedUser ? (
                  <Box>
                    <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
                      <Typography color="text.secondary" variant="body2">
                        Status:
                      </Typography>
                      <UserStatusChip active={loadedUser.active} />
                    </Stack>
                    <Typography color="text.secondary" variant="caption">
                      Created {formatTimestamp(loadedUser.createdAtUtc)} · Updated{' '}
                      {formatTimestamp(loadedUser.updatedAtUtc)}
                    </Typography>
                  </Box>
                ) : null}
              </>
            )}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button disabled={submitting} onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={!canSubmit || !loadedUser} type="submit" variant="contained">
            {submitting ? <CircularProgress color="inherit" size={24} /> : 'Save'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}

function formatTimestamp(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(
    new Date(value),
  )
}
