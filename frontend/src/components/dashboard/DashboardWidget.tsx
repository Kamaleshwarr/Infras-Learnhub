import { Alert, Box, Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import { Link as RouterLink } from 'react-router-dom'

interface DashboardWidgetProps {
  title: string
  value: string
  helperText: string
  icon?: ReactNode
  loading?: boolean
  error?: string | null
  href?: string
  linkAriaLabel?: string
}

export function DashboardWidget({
  error,
  helperText,
  href,
  icon,
  linkAriaLabel,
  loading = false,
  title,
  value,
}: DashboardWidgetProps) {
  const content = (
    <CardContent>
      <Stack spacing={1.5}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography color="text.secondary" variant="body2">
            {title}
          </Typography>
          {icon ? <Box sx={{ color: 'primary.main' }}>{icon}</Box> : null}
        </Stack>
        {loading ? <Skeleton height={48} width={96} /> : <Typography variant="h4">{value}</Typography>}
        <Typography color="text.secondary" variant="body2">
          {helperText}
        </Typography>
        {error ? <Alert severity="error">{error}</Alert> : null}
      </Stack>
    </CardContent>
  )

  if (href) {
    return (
      <Card
        aria-label={linkAriaLabel ?? title}
        component={RouterLink}
        sx={{
          color: 'inherit',
          cursor: 'pointer',
          height: '100%',
          textDecoration: 'none',
          transition: (theme) => theme.transitions.create(['box-shadow', 'border-color']),
          '&:hover': {
            borderColor: 'primary.main',
            boxShadow: 1,
          },
        }}
        to={href}
        variant="outlined"
      >
        {content}
      </Card>
    )
  }

  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      {content}
    </Card>
  )
}
