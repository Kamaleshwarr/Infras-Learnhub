import { Box, Typography } from '@mui/material'
import { longTextWrapSx } from './textStyles'
import { pageSectionSpacing } from '../../theme/uiTokens'

interface PageHeaderProps {
  title: string
  description: string
}

export function PageHeader({ title, description }: PageHeaderProps) {
  return (
    <Box sx={{ ...pageSectionSpacing, minWidth: 0 }}>
      <Typography
        component="h1"
        gutterBottom
        sx={{ ...longTextWrapSx, letterSpacing: '-0.02em' }}
        variant="h4"
      >
        {title}
      </Typography>
      <Typography color="text.secondary" sx={{ ...longTextWrapSx, lineHeight: 1.6, maxWidth: 720 }} variant="body1">
        {description}
      </Typography>
    </Box>
  )
}
