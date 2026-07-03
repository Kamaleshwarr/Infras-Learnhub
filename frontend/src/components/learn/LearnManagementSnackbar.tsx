import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'

export interface LearnManagementNotification {
  message: string
  severity: 'success' | 'error'
}

interface LearnManagementSnackbarProps {
  notification: LearnManagementNotification | null
  onClose: () => void
}

export function LearnManagementSnackbar({ notification, onClose }: LearnManagementSnackbarProps) {
  return (
    <Snackbar autoHideDuration={5000} onClose={onClose} open={Boolean(notification)}>
      {notification ? (
        <Alert onClose={onClose} severity={notification.severity} sx={{ width: '100%' }} variant="filled">
          {notification.message}
        </Alert>
      ) : undefined}
    </Snackbar>
  )
}
