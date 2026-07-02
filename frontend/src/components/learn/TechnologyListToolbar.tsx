import AddOutlinedIcon from '@mui/icons-material/AddOutlined'
import { Box, Button } from '@mui/material'
import { LEARN_MESSAGES } from './learnMessages'

interface TechnologyListToolbarProps {
  onCreateTechnology: () => void
}

export function TechnologyListToolbar({ onCreateTechnology }: TechnologyListToolbarProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
      <Button onClick={onCreateTechnology} startIcon={<AddOutlinedIcon />} variant="contained">
        {LEARN_MESSAGES.createTechnology}
      </Button>
    </Box>
  )
}
