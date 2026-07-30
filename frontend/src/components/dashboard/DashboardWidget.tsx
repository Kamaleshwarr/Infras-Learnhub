import { Alert, Box, Card, CardContent, Skeleton, Stack, Typography, useTheme } from '@mui/material'
import type { ReactNode } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { cardContentPadding, metricCardHoverSx } from '../../theme/uiTokens'

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
  const theme = useTheme()
  const cardSx = {
    ...metricCardHoverSx(theme, true),
    ...(href
      ? {
          color: 'inherit',
          cursor: 'pointer',
          textDecoration: 'none',
        }
      : {}),
  }

  const content = (
    <CardContent sx={cardContentPadding}>
      <Stack spacing={1.5}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography color="text.secondary" sx={{ fontWeight: 500 }} variant="body2">
            {title}
          </Typography>
          {icon ? (
            <Box
              className="metric-icon"
              sx={{
                color: 'text.secondary',
                display: 'flex',
                transition: theme.transitions.create(['color', 'filter'], {
                  duration: theme.transitions.duration.short,
                }),
                '& .MuiSvgIcon-root': { fontSize: 22 },
              }}
            >
              {icon}
            </Box>
          ) : null}
        </Stack>
        {loading ? <Skeleton height={48} width={96} /> : <Typography sx={{ fontWeight: 700, letterSpacing: '-0.02em' }} variant="h4">{value}</Typography>}
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
        sx={cardSx}
        to={href}
        variant="outlined"
      >
        {content}
      </Card>
    )
  }

  return (
    <Card sx={cardSx} variant="outlined">
      {content}
    </Card>
  )
}
