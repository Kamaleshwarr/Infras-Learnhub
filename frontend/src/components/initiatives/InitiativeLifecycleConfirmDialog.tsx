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
import type { InitiativeStatus } from '../../types/initiatives'
import { WrappingText } from '../common/WrappingText'
import { InitiativeStatusChip } from './InitiativeStatusChip'

interface InitiativeLifecycleConfirmDialogProps {
  open: boolean
  title: string
  confirmLabel?: string
  confirmColor?: 'primary' | 'success' | 'warning' | 'error'
  submitting?: boolean
  confirmDisabled?: boolean
  variant?: 'confirm' | 'info'
  closeLabel?: string
  onClose: () => void
  onConfirm?: () => void
  children?: ReactNode
}

export function InitiativeLifecycleConfirmDialog({
  open,
  title,
  confirmLabel = 'Confirm',
  confirmColor = 'primary',
  submitting = false,
  confirmDisabled = false,
  variant = 'confirm',
  closeLabel = 'Cancel',
  onClose,
  onConfirm,
  children,
}: InitiativeLifecycleConfirmDialogProps) {
  const isInfo = variant === 'info'

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
        {isInfo ? (
          <Button disabled={submitting} onClick={onClose} variant="contained">
            {closeLabel}
          </Button>
        ) : (
          <>
            <Button disabled={submitting} onClick={onClose}>
              {closeLabel}
            </Button>
            <Button
              color={confirmColor}
              disabled={submitting || confirmDisabled}
              onClick={onConfirm}
              variant="contained"
            >
              {submitting ? <CircularProgress color="inherit" size={24} /> : confirmLabel}
            </Button>
          </>
        )}
      </DialogActions>
    </Dialog>
  )
}

interface InitiativeDeleteDetailsProps {
  title: string
  status: InitiativeStatus
  submissionCount: number
}

export function InitiativeDeleteDetails({ title, status, submissionCount }: InitiativeDeleteDetailsProps) {
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
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mt: 1 }}>
        <Typography color="text.secondary" variant="body2">
          Status:
        </Typography>
        <InitiativeStatusChip status={status} />
      </Stack>
      <WrappingText color="text.secondary" sx={{ mt: 1 }} variant="body2">
        Certificate submissions: {submissionCount}
      </WrappingText>
    </Box>
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
