import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import axios from 'axios'
import { authApi } from '../../api/authApi'
import type { ApiErrorResponse } from '../../types/api'
import { isPasswordPolicyCompliant, PASSWORD_POLICY_MESSAGE } from '../../types/auth'

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = useMemo(() => searchParams.get('token') ?? '', [searchParams])
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const policyCompliant = isPasswordPolicyCompliant(newPassword)
  const passwordsMatch = newPassword === confirmNewPassword
  const canSubmit =
    token.length > 0 &&
    policyCompliant &&
    confirmNewPassword.length >= 8 &&
    passwordsMatch &&
    !submitting

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canSubmit) {
      setError('Enter a valid new password.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await authApi.resetPassword({ token, newPassword, confirmNewPassword })
      navigate('/login', { replace: true, state: { passwordReset: true } })
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
                <Typography variant="h4">Reset Password</Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Choose a new password for your account.
                </Typography>
              </Box>
              {!token && (
                <Alert severity="error">
                  Reset token is missing. Use the link from your email or request a new reset.
                </Alert>
              )}
              {error && <Alert severity="error">{error}</Alert>}
              <TextField
                autoComplete="new-password"
                disabled={submitting || !token}
                error={newPassword.length > 0 && !policyCompliant}
                helperText={PASSWORD_POLICY_MESSAGE}
                label="New password"
                onChange={(event) => setNewPassword(event.target.value)}
                required
                type="password"
                value={newPassword}
              />
              <TextField
                autoComplete="new-password"
                disabled={submitting || !token}
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
                {submitting ? <CircularProgress color="inherit" size={24} /> : 'Reset password'}
              </Button>
              <Link component={RouterLink} to="/forgot-password" underline="hover">
                Request a new reset link
              </Link>
            </Stack>
          </CardContent>
        </Card>
      </Container>
    </Box>
  )
}

function resolveError(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.message ?? 'Unable to reset password. Please try again.'
  }
  return 'Unable to reset password. Please try again.'
}
