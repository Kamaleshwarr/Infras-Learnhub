import type { SyntheticEvent } from 'react'
import { Alert, Snackbar } from '@mui/material'

export interface InitiativeManagementNotification {
  message: string
  severity: 'success' | 'error'
}

interface InitiativeManagementSnackbarProps {
  notification: InitiativeManagementNotification | null
  onClose: () => void
}

export function InitiativeManagementSnackbar({
  notification,
  onClose,
}: InitiativeManagementSnackbarProps) {
  function handleClose(_event: Event | SyntheticEvent, reason?: string) {
    if (reason === 'clickaway') {
      return
    }
    onClose()
  }

  return (
    <Snackbar
      anchorOrigin={{ horizontal: 'center', vertical: 'bottom' }}
      autoHideDuration={5000}
      onClose={handleClose}
      open={Boolean(notification)}
    >
      {notification ? (
        <Alert onClose={handleClose} severity={notification.severity} sx={{ width: '100%' }} variant="filled">
          {notification.message}
        </Alert>
      ) : (
        <span />
      )}
    </Snackbar>
  )
}
