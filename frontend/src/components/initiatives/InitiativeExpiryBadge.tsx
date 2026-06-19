import TimelapseOutlinedIcon from '@mui/icons-material/TimelapseOutlined'
import Chip from '@mui/material/Chip'
import { daysUntilExpiry, isExpiringCritical, isExpiringSoon } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeExpiryBadgeProps {
  expiryDateUtc: string
}

export function InitiativeExpiryBadge({ expiryDateUtc }: InitiativeExpiryBadgeProps) {
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
