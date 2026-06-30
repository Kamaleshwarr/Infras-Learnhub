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
} from '@mui/material'
import { WrappingText } from '../common/WrappingText'

interface InitiativeLifecycleConfirmDialogProps {
  open: boolean
  title: string
  confirmLabel: string
  confirmColor?: 'primary' | 'success' | 'warning' | 'error'
  submitting?: boolean
  confirmDisabled?: boolean
  onClose: () => void
  onConfirm: () => void
  children?: ReactNode
}

export function InitiativeLifecycleConfirmDialog({
  open,
  title,
  confirmLabel,
  confirmColor = 'primary',
  submitting = false,
  confirmDisabled = false,
  onClose,
  onConfirm,
  children,
}: InitiativeLifecycleConfirmDialogProps) {
  return (
    <Dialog
      aria-labelledby="initiative-lifecycle-confirm-title"
      fullWidth
      maxWidth="sm"
      onClose={submitting ? undefined : onClose}
      open={open}
    >
      <DialogTitle id="initiative-lifecycle-confirm-title">{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {children}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button disabled={submitting} onClick={onClose}>
          Cancel
        </Button>
        <Button
          color={confirmColor}
          disabled={submitting || confirmDisabled}
          onClick={onConfirm}
          variant="contained"
        >
          {submitting ? <CircularProgress color="inherit" size={24} /> : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

interface InitiativeLifecycleSummaryProps {
  title: string
  startDate: string
  expiryDate: string
}

export function InitiativeLifecycleSummary({ title, startDate, expiryDate }: InitiativeLifecycleSummaryProps) {
  return (
    <Box
      sx={{
        bgcolor: 'action.hover',
        borderRadius: 1,
        minWidth: 0,
        p: 2,
      }}
    >
      <WrappingText variant="subtitle2">{title}</WrappingText>
      <WrappingText color="text.secondary" sx={{ mt: 0.5 }} variant="body2">
        Start date: {startDate}
      </WrappingText>
      <WrappingText color="text.secondary" variant="body2">
        Expiry date: {expiryDate}
      </WrappingText>
    </Box>
  )
}
