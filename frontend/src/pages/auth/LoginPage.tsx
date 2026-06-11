import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link as RouterLink, Navigate, useLocation, useNavigate } from 'react-router-dom'
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
import { useAuth } from '../../auth/useAuth'
import type { ApiErrorResponse } from '../../types/api'

export function LoginPage() {
  const { isAuthenticated, isLoading, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const trimmedEmail = email.trim()
  const emailError = trimmedEmail.length > 0 && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)
  const passwordError = password.length > 0 && password.length < 8
  const canSubmit = trimmedEmail.length > 0 && password.length >= 8 && !emailError && !submitting && !isLoading

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canSubmit) {
      setError('Enter a valid email and a password with at least 8 characters.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const response = await login(trimmedEmail, password)
      if (response.user.mustChangePassword) {
        navigate('/change-password', { replace: true })
        return
      }
      const state = location.state as { from?: { pathname?: string } } | null
      navigate(state?.from?.pathname ?? '/', { replace: true })
    } catch (loginError) {
      setError(resolveLoginError(loginError))
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
                <Typography variant="h4">Engineering Learning Hub</Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Sign in with your internal account.
                </Typography>
              </Box>
              {error && <Alert severity="error">{error}</Alert>}
              <TextField
                autoComplete="email"
                disabled={submitting || isLoading}
                error={emailError}
                helperText={emailError ? 'Enter a valid email address.' : 'Use your company email address.'}
                label="Email"
                onChange={(event) => setEmail(event.target.value)}
                required
                type="email"
                value={email}
              />
              <TextField
                autoComplete="current-password"
                disabled={submitting || isLoading}
                error={passwordError}
                helperText={passwordError ? 'Password must be at least 8 characters.' : ' '}
                label="Password"
                onChange={(event) => setPassword(event.target.value)}
                required
                type="password"
                value={password}
              />
              <Button disabled={!canSubmit} size="large" type="submit" variant="contained">
                {submitting ? <CircularProgress color="inherit" size={24} /> : 'Sign in'}
              </Button>
              <Link component={RouterLink} to="/forgot-password" underline="hover">
                Forgot password?
              </Link>
            </Stack>
          </CardContent>
        </Card>
      </Container>
    </Box>
  )
}

function resolveLoginError(error: unknown) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    if (error.response?.data?.message) {
      return error.response.data.message
    }
    if (error.response?.status === 401) {
      return 'Invalid email or password.'
    }
  }
  return 'Unable to sign in. Check your connection and try again.'
}
