import ArrowBackOutlinedIcon from '@mui/icons-material/ArrowBackOutlined'
import { Button } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

export function InitiativeDetailBackLink() {
  return (
    <Button
      component={RouterLink}
      startIcon={<ArrowBackOutlinedIcon />}
      sx={{ mb: 2 }}
      to="/initiatives"
      variant="text"
    >
      {INITIATIVE_MESSAGES.backToInitiatives}
    </Button>
  )
}
