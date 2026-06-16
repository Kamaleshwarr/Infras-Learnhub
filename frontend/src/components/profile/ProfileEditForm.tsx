import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Grid,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { profileApi } from '../../api/profileApi'
import { UserStatusChip } from '../users/UserStatusChip'
import { ProfileAvatar } from './ProfileAvatar'
import type { Profile } from '../../types/profile'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { normalizeEmail } from '../../utils/email'
import type { ProfileFormBaseline, ProfileFormState } from './profileEditForm'
import { hasProfileFormValidationErrors, isProfileFormDirty } from './profileEditForm'

interface ProfileEditFormProps {
  profile: Profile
  onCancel: () => void
  onSuccess: (profile: Profile, accessToken?: string | null) => void
}

export function ProfileEditForm({ profile, onCancel, onSuccess }: ProfileEditFormProps) {
  const [form, setForm] = useState<ProfileFormState>({
    fullName: profile.fullName,
    email: profile.email,
  })
  const [baseline, setBaseline] = useState<ProfileFormBaseline | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const normalizedEmail = normalizeEmail(form.email)
  const isDirty = isProfileFormDirty(form, baseline)
  const hasValidationErrors = hasProfileFormValidationErrors(form, fieldErrors)
  const canSubmit = Boolean(baseline) && isDirty && !hasValidationErrors && !submitting

  useEffect(() => {
    const nextForm = {
      fullName: profile.fullName,
      email: profile.email,
    }
    setForm(nextForm)
    setBaseline(nextForm)
    setFieldErrors({})
    setFormError(null)
    setSubmitting(false)
  }, [profile])

  function updateField<K extends keyof ProfileFormState>(field: K, value: ProfileFormState[K]) {
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
    if (!canSubmit || !isDirty) {
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      const response = await profileApi.update({
        fullName: form.fullName.trim(),
        email: normalizedEmail,
      })
      onSuccess(response.profile, response.accessToken)
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to update profile. Please try again.'))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card>
      <CardContent sx={{ p: { xs: 2, md: 3 } }}>
        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={3}>
            {formError ? <Alert severity="error">{formError}</Alert> : null}

            <Stack spacing={2} sx={{ alignItems: 'center' }}>
              <ProfileAvatar fullName={form.fullName.trim() || profile.fullName} />
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h5">{profile.fullName}</Typography>
                <Typography color="text.secondary" variant="body2">
                  {profile.email}
                </Typography>
              </Box>
            </Stack>

            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  autoComplete="off"
                  autoFocus
                  disabled={submitting}
                  error={Boolean(fieldErrors.fullName)}
                  fullWidth
                  helperText={fieldErrors.fullName}
                  label="Full Name"
                  name="profileFullName"
                  onChange={(event) => updateField('fullName', event.target.value)}
                  required
                  slotProps={{ htmlInput: { maxLength: 200 } }}
                  value={form.fullName}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  autoComplete="off"
                  disabled={submitting}
                  error={Boolean(fieldErrors.email)}
                  fullWidth
                  helperText={fieldErrors.email}
                  label="Email"
                  name="profileEmail"
                  onChange={(event) => updateField('email', event.target.value)}
                  required
                  slotProps={{ htmlInput: { maxLength: 320 } }}
                  type="text"
                  value={form.email}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  disabled
                  fullWidth
                  label="Employee ID"
                  slotProps={{ input: { readOnly: true } }}
                  value={profile.employeeId}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  disabled
                  fullWidth
                  label="Role"
                  slotProps={{ input: { readOnly: true } }}
                  value={profile.role}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <Stack spacing={1}>
                  <Typography color="text.secondary" variant="body2">
                    Status
                  </Typography>
                  <Box>
                    <UserStatusChip active={profile.active} />
                  </Box>
                </Stack>
              </Grid>
            </Grid>

            <Typography color="text.secondary" variant="caption">
              Created {formatTimestamp(profile.createdAtUtc)} · Updated{' '}
              {formatTimestamp(profile.updatedAtUtc)}
            </Typography>

            <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-end' }}>
              <Button disabled={submitting} onClick={onCancel}>
                Cancel
              </Button>
              <Button disabled={!canSubmit} type="submit" variant="contained">
                {submitting ? <CircularProgress color="inherit" size={24} /> : 'Save'}
              </Button>
            </Stack>
          </Stack>
        </Box>
      </CardContent>
    </Card>
  )
}

function formatTimestamp(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(
    new Date(value),
  )
}
