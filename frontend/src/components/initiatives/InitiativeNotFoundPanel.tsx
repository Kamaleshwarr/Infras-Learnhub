import { Box, Button, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

export function InitiativeNotFoundPanel() {
  return (
    <Box sx={{ alignItems: 'flex-start', display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h5">Initiative unavailable</Typography>
      <Typography color="text.secondary">{INITIATIVE_MESSAGES.detailNotFound}</Typography>
      <Button component={RouterLink} to="/initiatives" variant="contained">
        {INITIATIVE_MESSAGES.browseInitiatives}
      </Button>
    </Box>
  )
}
