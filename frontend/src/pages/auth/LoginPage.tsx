import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useAuth } from '../../auth/useAuth'

export function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('employee@learninghub.local')
  const [password, setPassword] = useState('Employee@12345')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await login(email, password)
      const state = location.state as { from?: { pathname?: string } } | null
      navigate(state?.from?.pathname ?? '/', { replace: true })
    } catch {
      setError('Unable to sign in. Check your credentials and try again.')
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
                label="Email"
                onChange={(event) => setEmail(event.target.value)}
                required
                type="email"
                value={email}
              />
              <TextField
                autoComplete="current-password"
                label="Password"
                onChange={(event) => setPassword(event.target.value)}
                required
                type="password"
                value={password}
              />
              <Button disabled={submitting} size="large" type="submit" variant="contained">
                {submitting ? 'Signing in...' : 'Sign in'}
              </Button>
            </Stack>
          </CardContent>
        </Card>
      </Container>
    </Box>
  )
}
