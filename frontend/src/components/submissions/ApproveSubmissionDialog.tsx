import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
} from '@mui/material'
import type { CertificateSubmission } from '../../types/submissions'
import { SubmissionReviewSummary } from './SubmissionReviewSummary'

interface ApproveSubmissionDialogProps {
  error?: string | null
  onClose: () => void
  onConfirm: () => void
  open: boolean
  submission: CertificateSubmission | null
  submitting?: boolean
}

export function ApproveSubmissionDialog({
  error,
  onClose,
  onConfirm,
  open,
  submission,
  submitting = false,
}: ApproveSubmissionDialogProps) {
  return (
    <Dialog
      aria-labelledby="approve-submission-title"
      fullWidth
      maxWidth="sm"
      onClose={submitting ? undefined : onClose}
      open={open}
    >
      <DialogTitle id="approve-submission-title">Approve certificate submission</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {submission ? <SubmissionReviewSummary submission={submission} /> : null}
          <Alert severity="info">
            The employee will be notified that their certificate submission was approved.
          </Alert>
          {error ? <Alert severity="error">{error}</Alert> : null}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button disabled={submitting} onClick={onClose}>
          Cancel
        </Button>
        <Button color="success" disabled={submitting || !submission} onClick={onConfirm} variant="contained">
          {submitting ? <CircularProgress color="inherit" size={24} /> : 'Approve certificate'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
