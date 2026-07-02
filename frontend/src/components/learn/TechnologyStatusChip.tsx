import Chip from '@mui/material/Chip'
import type { TechnologyStatus } from '../../types/learn'

const STATUS_LABELS: Record<TechnologyStatus, string> = {
  HIDDEN: 'Hidden',
  PUBLISHED: 'Published',
  ARCHIVED: 'Archived',
}

const STATUS_COLORS: Record<TechnologyStatus, 'default' | 'success' | 'warning'> = {
  HIDDEN: 'warning',
  PUBLISHED: 'success',
  ARCHIVED: 'default',
}

export function TechnologyStatusChip({ status }: { status: TechnologyStatus }) {
  return <Chip color={STATUS_COLORS[status]} label={STATUS_LABELS[status]} size="small" />
}
