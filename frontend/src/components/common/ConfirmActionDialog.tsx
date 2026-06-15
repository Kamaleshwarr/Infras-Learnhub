import type { ReactNode } from 'react'
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'
import type { UserSummary } from '../../types/users'
import { UserStatusChip } from '../users/UserStatusChip'

interface ConfirmActionDialogProps {
  open: boolean
  title: string
  confirmLabel: string
  confirmColor?: 'primary' | 'success' | 'warning' | 'error'
  user: UserSummary | null
  submitting?: boolean
  onClose: () => void
  onConfirm: () => void
  children?: ReactNode
}

export function ConfirmActionDialog({
  open,
  title,
  confirmLabel,
  confirmColor = 'primary',
  user,
  submitting = false,
  onClose,
  onConfirm,
  children,
}: ConfirmActionDialogProps) {
  return (
    <Dialog
      aria-describedby={user ? 'confirm-action-user-details' : undefined}
      aria-labelledby="confirm-action-title"
      fullWidth
      maxWidth="sm"
      onClose={submitting ? undefined : onClose}
      open={open}
    >
      <DialogTitle id="confirm-action-title">{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {user ? (
            <Box
              id="confirm-action-user-details"
              sx={{
                bgcolor: 'action.hover',
                borderRadius: 1,
                p: 2,
              }}
            >
              <Typography variant="subtitle2">{user.fullName}</Typography>
              <Typography color="text.secondary" variant="body2">
                {user.employeeId} · {user.email}
              </Typography>
              <Box sx={{ mt: 1 }}>
                <UserStatusChip active={user.active} />
              </Box>
            </Box>
          ) : null}
          {children}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button disabled={submitting} onClick={onClose}>
          Cancel
        </Button>
        <Button color={confirmColor} disabled={submitting || !user} onClick={onConfirm} variant="contained">
          {submitting ? <CircularProgress color="inherit" size={24} /> : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
