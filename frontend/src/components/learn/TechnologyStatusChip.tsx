import Chip from '@mui/material/Chip'
import type { TechnologyStatus } from '../../types/learn'

interface TechnologyStatusChipProps {
  status: TechnologyStatus
}

const STATUS_LABELS: Record<TechnologyStatus, string> = {
  DRAFT: 'Draft',
  PUBLISHED: 'Published',
  ARCHIVED: 'Archived',
}

const STATUS_COLORS: Record<TechnologyStatus, 'success' | 'default' | 'warning'> = {
  DRAFT: 'warning',
  PUBLISHED: 'success',
  ARCHIVED: 'default',
}

export function TechnologyStatusChip({ status }: TechnologyStatusChipProps) {
  return (
    <Chip
      color={STATUS_COLORS[status]}
      label={STATUS_LABELS[status]}
      size="small"
      variant={status === 'PUBLISHED' ? 'filled' : 'outlined'}
    />
  )
}
