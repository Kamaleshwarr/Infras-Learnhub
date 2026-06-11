import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link as RouterLink } from 'react-router-dom'
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

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const trimmedEmail = email.trim()
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)
  const canSubmit = emailValid && !submitting

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canSubmit) {
      setError('Enter a valid email address.')
      return
    }
    setSubmitting(true)
    setError(null)
    setMessage(null)
    try {
      const response = await authApi.forgotPassword({ email: trimmedEmail })
      setMessage(response.message)
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
                <Typography variant="h4">Forgot Password</Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Enter your email and we will send reset instructions if an account exists.
                </Typography>
              </Box>
              {message && <Alert severity="success">{message}</Alert>}
              {error && <Alert severity="error">{error}</Alert>}
              <TextField
                autoComplete="email"
                disabled={submitting}
                label="Email"
                onChange={(event) => setEmail(event.target.value)}
                required
                type="email"
                value={email}
              />
              <Button disabled={!canSubmit} size="large" type="submit" variant="contained">
                {submitting ? <CircularProgress color="inherit" size={24} /> : 'Send reset link'}
              </Button>
              <Link component={RouterLink} to="/login" underline="hover">
                Back to sign in
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
    return error.response?.data?.message ?? 'Unable to process the request. Please try again.'
  }
  return 'Unable to process the request. Please try again.'
}
