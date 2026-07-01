import { Box, Typography } from '@mui/material'
import { longTextWrapSx } from './textStyles'

interface PageHeaderProps {
  title: string
  description: string
}

export function PageHeader({ title, description }: PageHeaderProps) {
  return (
    <Box sx={{ mb: 3, minWidth: 0 }}>
      <Typography component="h1" gutterBottom sx={longTextWrapSx} variant="h4">
        {title}
      </Typography>
      <Typography color="text.secondary" sx={longTextWrapSx} variant="body1">
        {description}
      </Typography>
    </Box>
  )
}
