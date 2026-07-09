import {
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material'

interface ConfirmDialogProps {
  open: boolean
  title: string
  message: string
  confirmLabel?: string
  confirmColor?: 'primary' | 'error'
  submitting?: boolean
  onClose: () => void
  onConfirm: () => void
}

export function ConfirmDialog({
  confirmColor = 'error',
  confirmLabel = 'Confirm',
  message,
  onClose,
  onConfirm,
  open,
  submitting = false,
  title,
}: ConfirmDialogProps) {
  return (
    <Dialog fullWidth maxWidth="sm" onClose={submitting ? undefined : onClose} open={open}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button disabled={submitting} onClick={onClose}>
          Cancel
        </Button>
        <Button color={confirmColor} disabled={submitting} onClick={onConfirm} variant="contained">
          {submitting ? <CircularProgress color="inherit" size={20} /> : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
