import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import axios from 'axios'
import { authApi } from '../../api/authApi'
import { useAuth } from '../../auth/useAuth'
import type { ApiErrorResponse } from '../../types/api'
import { isPasswordPolicyCompliant, PASSWORD_POLICY_MESSAGE } from '../../types/auth'

export function ChangePasswordPage() {
  const navigate = useNavigate()
  const { user, login } = useAuth()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const policyCompliant = isPasswordPolicyCompliant(newPassword)
  const passwordsMatch = newPassword === confirmNewPassword
  const canSubmit =
    currentPassword.length >= 8 &&
    policyCompliant &&
    confirmNewPassword.length >= 8 &&
    passwordsMatch &&
    !submitting

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canSubmit) {
      setError('Enter your current password and a valid new password.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await authApi.changePassword({ currentPassword, newPassword, confirmNewPassword })
      if (user) {
        await login(user.email, newPassword)
      }
      navigate('/', { replace: true })
    } catch (submitError) {
      setError(resolveError(submitError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box sx={{ bgcolor: 'background.default', display: 'grid', minHeight: '100vh', placeItems: 'center' }}>
      <Container maxWidth="sm">
        <Card elevation={8}>
          <CardContent sx={{ p: 4 }}>
            <Stack component="form" onSubmit={handleSubmit} spacing={3}>
              <Box>
                <Typography variant="h4">Change Password</Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  {user?.mustChangePassword
                    ? 'You must set a new password before continuing.'
                    : 'Update your account password.'}
                </Typography>
              </Box>
              {error && <Alert severity="error">{error}</Alert>}
              <TextField
                autoComplete="current-password"
                disabled={submitting}
                label="Current password"
                onChange={(event) => setCurrentPassword(event.target.value)}
                required
                type="password"
                value={currentPassword}
              />
              <TextField
                autoComplete="new-password"
                disabled={submitting}
                error={newPassword.length > 0 && !policyCompliant}
                helperText={newPassword.length > 0 && !policyCompliant ? PASSWORD_POLICY_MESSAGE : PASSWORD_POLICY_MESSAGE}
                label="New password"
                onChange={(event) => setNewPassword(event.target.value)}
                required
                type="password"
                value={newPassword}
              />
              <TextField
                autoComplete="new-password"
                disabled={submitting}
                error={confirmNewPassword.length > 0 && !passwordsMatch}
                helperText={
                  confirmNewPassword.length > 0 && !passwordsMatch ? 'Passwords do not match.' : ' '
                }
                label="Confirm new password"
                onChange={(event) => setConfirmNewPassword(event.target.value)}
                required
                type="password"
                value={confirmNewPassword}
              />
              <Button disabled={!canSubmit} size="large" type="submit" variant="contained">
                {submitting ? <CircularProgress color="inherit" size={24} /> : 'Update password'}
              </Button>
            </Stack>
          </CardContent>
        </Card>
      </Container>
    </Box>
  )
}

function resolveError(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    if (error.response?.data?.message) {
      return error.response.data.message
    }
    if (error.response?.status === 401) {
      return 'Current password is incorrect.'
    }
  }
  return 'Unable to change password. Please try again.'
}
