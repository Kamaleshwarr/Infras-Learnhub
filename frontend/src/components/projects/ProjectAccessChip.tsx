import Chip from '@mui/material/Chip'
import type { ProjectAccessType } from '../../types/projects'
import { PROJECT_ACCESS_LABELS } from '../../types/projects'

interface ProjectAccessChipProps {
  accessType: ProjectAccessType
}

export function ProjectAccessChip({ accessType }: ProjectAccessChipProps) {
  return (
    <Chip
      color={accessType === 'PUBLIC' ? 'primary' : 'default'}
      label={PROJECT_ACCESS_LABELS[accessType]}
      size="small"
      variant="outlined"
    />
  )
}
