import Chip from '@mui/material/Chip'
import type { ProjectStatus } from '../../types/projects'
import { PROJECT_STATUS_LABELS } from '../../types/projects'

interface ProjectStatusChipProps {
  status: ProjectStatus
}

const STATUS_COLORS: Record<ProjectStatus, 'success' | 'default' | 'warning' | 'info'> = {
  ACTIVE: 'success',
  ON_HOLD: 'warning',
  COMPLETED: 'info',
  ARCHIVED: 'default',
}

export function ProjectStatusChip({ status }: ProjectStatusChipProps) {
  return (
    <Chip
      color={STATUS_COLORS[status]}
      label={PROJECT_STATUS_LABELS[status]}
      size="small"
      variant={status === 'ACTIVE' ? 'filled' : 'outlined'}
    />
  )
}
