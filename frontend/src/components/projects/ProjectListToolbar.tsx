import AddIcon from '@mui/icons-material/Add'
import { Box, Button } from '@mui/material'
import { PROJECT_MESSAGES } from './projectMessages'

interface ProjectListToolbarProps {
  onCreateProject: () => void
}

export function ProjectListToolbar({ onCreateProject }: ProjectListToolbarProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
      <Button onClick={onCreateProject} startIcon={<AddIcon />} variant="contained">
        {PROJECT_MESSAGES.createProject}
      </Button>
    </Box>
  )
}
