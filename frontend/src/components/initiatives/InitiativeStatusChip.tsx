import Chip from '@mui/material/Chip'
import type { InitiativeStatus } from '../../types/initiatives'

interface InitiativeStatusChipProps {
  status: InitiativeStatus
}

const STATUS_LABELS: Record<InitiativeStatus, string> = {
  ACTIVE: 'Active',
  DRAFT: 'Draft',
  EXPIRED: 'Expired',
}

const STATUS_COLORS: Record<InitiativeStatus, 'success' | 'default' | 'warning'> = {
  ACTIVE: 'success',
  DRAFT: 'warning',
  EXPIRED: 'default',
}

export function InitiativeStatusChip({ status }: InitiativeStatusChipProps) {
  return (
    <Chip
      color={STATUS_COLORS[status]}
      label={STATUS_LABELS[status]}
      size="small"
      variant={status === 'ACTIVE' ? 'filled' : 'outlined'}
    />
  )
}
