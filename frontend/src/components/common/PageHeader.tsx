import { Box, Typography } from '@mui/material'

interface PageHeaderProps {
  title: string
  description: string
}

export function PageHeader({ title, description }: PageHeaderProps) {
  return (
    <Box sx={{ mb: 3 }}>
      <Typography component="h1" gutterBottom variant="h4">
        {title}
      </Typography>
      <Typography color="text.secondary" variant="body1">
        {description}
      </Typography>
    </Box>
  )
}
