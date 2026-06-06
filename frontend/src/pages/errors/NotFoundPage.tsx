import { Button, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <Stack alignItems="flex-start" spacing={2}>
      <Typography variant="h4">Page not found</Typography>
      <Typography color="text.secondary">The requested page does not exist.</Typography>
      <Button component={RouterLink} to="/" variant="contained">
        Back to dashboard
      </Button>
    </Stack>
  )
}
