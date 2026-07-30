import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import { Link as RouterLink } from 'react-router-dom'

interface DashboardSectionHeaderProps {
  title: string
  description?: string
  viewAllHref?: string
  viewAllLabel?: string
}

export function DashboardSectionHeader({
  description,
  title,
  viewAllHref,
  viewAllLabel = 'View all',
}: DashboardSectionHeaderProps) {
  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={1}
      sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between', mb: 2 }}
    >
      <Box sx={{ minWidth: 0 }}>
        <Typography component="h2" sx={{ fontWeight: 700 }} variant="h6">
          {title}
        </Typography>
        {description ? (
          <Typography color="text.secondary" variant="body2">
            {description}
          </Typography>
        ) : null}
      </Box>
      {viewAllHref ? (
        <Button component={RouterLink} size="small" sx={{ alignSelf: { xs: 'flex-start', sm: 'center' } }} to={viewAllHref}>
          {viewAllLabel}
        </Button>
      ) : null}
    </Stack>
  )
}

interface DashboardWelcomeBannerProps {
  greeting: string
  subtitle: string
  actions?: ReactNode
}

export function DashboardWelcomeBanner({ actions, greeting, subtitle }: DashboardWelcomeBannerProps) {
  return (
    <Card
      sx={{
        background: (theme) =>
          `linear-gradient(135deg, ${theme.palette.primary.main}14 0%, ${theme.palette.background.paper} 55%, ${theme.palette.secondary.main}10 100%)`,
        mb: 3,
      }}
      variant="outlined"
    >
      <CardContent sx={{ p: { xs: 2.5, md: 3 } }}>
        <Stack
          direction={{ xs: 'column', md: 'row' }}
          spacing={2}
          sx={{ alignItems: { md: 'center' }, justifyContent: 'space-between' }}
        >
          <Box sx={{ minWidth: 0 }}>
            <Typography component="h2" sx={{ fontWeight: 700, mb: 0.5 }} variant="h5">
              {greeting}
            </Typography>
            <Typography color="text.secondary" variant="body1">
              {subtitle}
            </Typography>
          </Box>
          {actions ? <Box sx={{ flexShrink: 0 }}>{actions}</Box> : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
