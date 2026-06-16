import Chip from '@mui/material/Chip'
import type { ApprovalStatus } from '../../types/submissions'

interface SubmissionStatusChipProps {
  status: ApprovalStatus
}

const STATUS_LABELS: Record<ApprovalStatus, string> = {
  SUBMITTED: 'Submitted',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
}

const STATUS_COLORS: Record<ApprovalStatus, 'warning' | 'success' | 'error'> = {
  SUBMITTED: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
}

export function SubmissionStatusChip({ status }: SubmissionStatusChipProps) {
  return (
    <Chip
      color={STATUS_COLORS[status]}
      label={STATUS_LABELS[status]}
      size="small"
      variant={status === 'SUBMITTED' ? 'outlined' : 'filled'}
    />
  )
}
