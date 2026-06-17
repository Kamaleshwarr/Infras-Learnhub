import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'
import { MAX_REJECTION_REASON_LENGTH, SUBMISSION_MESSAGES } from './submissionMessages'
import type { CertificateSubmission } from '../../types/submissions'
import { SubmissionReviewSummary } from './SubmissionReviewSummary'

interface RejectSubmissionDialogProps {
  error?: string | null
  onClose: () => void
  onSubmit: (rejectionReason: string) => void
  open: boolean
  submission: CertificateSubmission | null
  submitting?: boolean
}

export function RejectSubmissionDialog({
  error,
  onClose,
  onSubmit,
  open,
  submission,
  submitting = false,
}: RejectSubmissionDialogProps) {
  const [rejectionReason, setRejectionReason] = useState('')
  const [fieldError, setFieldError] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      setRejectionReason('')
      setFieldError(null)
    }
  }, [open, submission?.id])

  const trimmedReason = rejectionReason.trim()
  const canSubmit =
    Boolean(submission) &&
    trimmedReason.length > 0 &&
    trimmedReason.length <= MAX_REJECTION_REASON_LENGTH &&
    !submitting

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!trimmedReason) {
      setFieldError(SUBMISSION_MESSAGES.rejectionReasonRequired)
      return
    }

    if (trimmedReason.length > MAX_REJECTION_REASON_LENGTH) {
      setFieldError(`Rejection reason must be ${MAX_REJECTION_REASON_LENGTH} characters or fewer.`)
      return
    }

    onSubmit(trimmedReason)
  }

  return (
    <Dialog
      aria-labelledby="reject-submission-title"
      fullWidth
      maxWidth="sm"
      onClose={submitting ? undefined : onClose}
      open={open}
    >
      <form onSubmit={handleSubmit}>
        <DialogTitle id="reject-submission-title">Reject certificate submission</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {submission ? <SubmissionReviewSummary submission={submission} /> : null}
            <TextField
              error={Boolean(fieldError)}
              fullWidth
              helperText={fieldError ?? `${trimmedReason.length}/${MAX_REJECTION_REASON_LENGTH}`}
              label="Rejection reason"
              minRows={3}
              multiline
              onChange={(event) => {
                setRejectionReason(event.target.value)
                if (fieldError) {
                  setFieldError(null)
                }
              }}
              required
              value={rejectionReason}
            />
            <Alert severity="warning">
              The employee will be notified that their certificate submission was rejected, including this reason.
            </Alert>
            {error ? <Alert severity="error">{error}</Alert> : null}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button disabled={submitting} onClick={onClose}>
            Cancel
          </Button>
          <Button color="error" disabled={!canSubmit} type="submit" variant="contained">
            {submitting ? <CircularProgress color="inherit" size={24} /> : 'Reject certificate'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
