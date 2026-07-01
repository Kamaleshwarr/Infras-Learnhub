import AddOutlinedIcon from '@mui/icons-material/AddOutlined'
import { Box, Button } from '@mui/material'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeListToolbarProps {
  onCreateInitiative: () => void
}

export function InitiativeListToolbar({ onCreateInitiative }: InitiativeListToolbarProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
      <Button onClick={onCreateInitiative} startIcon={<AddOutlinedIcon />} variant="contained">
        {INITIATIVE_MESSAGES.createInitiative}
      </Button>
    </Box>
  )
}
