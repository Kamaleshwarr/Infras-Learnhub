import type { SyntheticEvent } from 'react'
import { Alert, Snackbar } from '@mui/material'

export interface UserManagementNotification {
  message: string
  severity: 'success' | 'error'
}

interface UserManagementSnackbarProps {
  notification: UserManagementNotification | null
  onClose: () => void
}

export function UserManagementSnackbar({ notification, onClose }: UserManagementSnackbarProps) {
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
