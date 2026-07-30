import { Box, Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
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
  accentColor?: 'primary' | 'secondary' | 'success' | 'warning' | 'info'
}

const accentPalette = {
  primary: 'primary.main',
  secondary: 'secondary.main',
  success: 'success.main',
  warning: 'warning.main',
  info: 'info.main',
} as const

export function DashboardWidget({
  accentColor = 'primary',
  error,
  helperText,
  href,
  icon,
  linkAriaLabel,
  loading = false,
  title,
  value,
}: DashboardWidgetProps) {
  const accentColorKey = accentPalette[accentColor]

  const content = (
    <CardContent sx={{ height: '100%', p: 2.5 }}>
      <Stack spacing={2} sx={{ height: '100%' }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography color="text.secondary" sx={{ fontWeight: 600, letterSpacing: 0.2 }} variant="overline">
              {title}
            </Typography>
          </Box>
          {icon ? (
            <Box
              sx={{
                alignItems: 'center',
                bgcolor: (theme) => alpha(theme.palette[accentColor].main, 0.1),
                borderRadius: 2,
                color: accentColorKey,
                display: 'flex',
                flexShrink: 0,
                height: 40,
                justifyContent: 'center',
                width: 40,
                '& .MuiSvgIcon-root': { fontSize: 22 },
              }}
            >
              {icon}
            </Box>
          ) : null}
        </Stack>
        {loading ? (
          <Skeleton height={44} sx={{ maxWidth: 120 }} variant="rounded" />
        ) : (
          <Typography sx={{ fontWeight: 700, lineHeight: 1.1 }} variant="h4">
            {value}
          </Typography>
        )}
        <Typography color="text.secondary" sx={{ mt: 'auto' }} variant="body2">
          {helperText}
        </Typography>
        {error ? (
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        ) : null}
      </Stack>
    </CardContent>
  )

  const cardSx = {
    color: 'inherit',
    height: '100%',
    textDecoration: 'none',
    transition: (theme: { transitions: { create: (props: string[]) => string } }) =>
      theme.transitions.create(['box-shadow', 'border-color', 'transform']),
    '&:hover': href
      ? {
          borderColor: 'primary.main',
          boxShadow: 2,
          transform: 'translateY(-1px)',
        }
      : undefined,
  }

  if (href) {
    return (
      <Card
        aria-label={linkAriaLabel ?? title}
        component={RouterLink}
        sx={cardSx}
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
