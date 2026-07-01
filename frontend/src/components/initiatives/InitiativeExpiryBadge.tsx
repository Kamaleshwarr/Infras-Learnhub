import EventBusyOutlinedIcon from '@mui/icons-material/EventBusyOutlined'
import TimelapseOutlinedIcon from '@mui/icons-material/TimelapseOutlined'
import Chip from '@mui/material/Chip'
import type { InitiativeStatus } from '../../types/initiatives'
import { daysUntilExpiry, isExpiringCritical, isExpiringSoon } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeExpiryBadgeProps {
  expiryDateUtc: string
  status: InitiativeStatus
}

export function InitiativeExpiryBadge({ expiryDateUtc, status }: InitiativeExpiryBadgeProps) {
  if (status === 'DRAFT') {
    return null
  }

  if (status === 'EXPIRED') {
    return (
      <Chip
        color="default"
        icon={<EventBusyOutlinedIcon />}
        label={INITIATIVE_MESSAGES.expiredLabel}
        size="small"
        variant="outlined"
      />
    )
  }

  if (!isExpiringSoon(expiryDateUtc)) {
    return null
  }

  const days = daysUntilExpiry(expiryDateUtc)
  const label =
    days === 0 || days === 1
      ? days === 0
        ? INITIATIVE_MESSAGES.expiresToday
        : INITIATIVE_MESSAGES.expiresInDays(1)
      : days != null && days > 1
        ? INITIATIVE_MESSAGES.expiresInDays(days)
        : INITIATIVE_MESSAGES.expiresToday

  return (
    <Chip
      color={isExpiringCritical(expiryDateUtc) ? 'error' : 'warning'}
      icon={<TimelapseOutlinedIcon />}
      label={label}
      size="small"
      variant="outlined"
    />
  )
}
