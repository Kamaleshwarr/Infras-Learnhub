import { Alert } from '@mui/material'
import type { Initiative } from '../../types/initiatives'
import { daysUntilExpiry, isExpiringCritical, isExpiringSoon } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeDetailAlertsProps {
  initiative: Initiative
}

export function InitiativeDetailAlerts({ initiative }: InitiativeDetailAlertsProps) {
  if (!isExpiringSoon(initiative.expiryDateUtc)) {
    return null
  }

  const days = daysUntilExpiry(initiative.expiryDateUtc)
  const message =
    days === 0
      ? INITIATIVE_MESSAGES.expiresToday
      : days === 1
        ? INITIATIVE_MESSAGES.expiresInDays(1)
        : days != null
          ? INITIATIVE_MESSAGES.expiresInDays(days)
          : INITIATIVE_MESSAGES.expiresToday

  return (
    <Alert severity={isExpiringCritical(initiative.expiryDateUtc) ? 'error' : 'warning'} sx={{ mb: 3 }}>
      {message}
    </Alert>
  )
}
