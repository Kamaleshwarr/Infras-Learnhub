import { Box, Typography } from '@mui/material'
import { alpha, useTheme } from '@mui/material/styles'
import { useAuth } from '../../auth/useAuth'

interface DashboardWelcomeBannerProps {
  isAdmin: boolean
}

export function DashboardWelcomeBanner({ isAdmin }: DashboardWelcomeBannerProps) {
  const theme = useTheme()
  const { user } = useAuth()
  const displayName = user?.fullName?.trim() || 'there'

  const title = isAdmin ? 'Admin Dashboard' : 'Employee Dashboard'
  const description = isAdmin
    ? 'Operational overview for learning administrators.'
    : 'Your learning activity and resources.'

  return (
    <Box
      sx={{
        mb: 3,
        minWidth: 0,
        px: { xs: 2.5, sm: 3 },
        py: { xs: 2.5, sm: 3 },
        borderRadius: 2,
        border: 1,
        borderColor: 'divider',
        background: `linear-gradient(135deg, ${theme.palette.background.paper} 0%, ${alpha(theme.palette.primary.main, 0.06)} 100%)`,
      }}
    >
      <Typography color="text.secondary" sx={{ mb: 0.5, fontWeight: 500 }} variant="body2">
        Welcome back, {displayName}
      </Typography>
      <Typography component="h1" sx={{ fontWeight: 700, letterSpacing: '-0.02em', mb: 0.75 }} variant="h4">
        {title}
      </Typography>
      <Typography color="text.secondary" sx={{ maxWidth: 720, lineHeight: 1.6 }} variant="body1">
        {description}
      </Typography>
    </Box>
  )
}
