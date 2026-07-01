import { useState, type MouseEvent } from 'react'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import { Alert, Button, IconButton, List, ListItem, ListItemText, Tooltip, Typography } from '@mui/material'
import { initiativesApi } from '../../api/initiativesApi'
import { submissionsApi } from '../../api/submissionsApi'
import type { Initiative } from '../../types/initiatives'
import { isConflictError, resolveApiError } from '../../utils/apiErrors'
import {
  InitiativeDeleteDetails,
  InitiativeLifecycleConfirmDialog,
} from './InitiativeLifecycleConfirmDialog'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

type DeleteDialogMode = 'blocked' | 'confirm' | null

interface InitiativeDeleteActionProps {
  initiative: Initiative
  layout?: 'compact' | 'button'
  disabled?: boolean
  onSuccess: () => void
}

export function InitiativeDeleteAction({
  initiative,
  layout = 'compact',
  disabled = false,
  onSuccess,
}: InitiativeDeleteActionProps) {
  const [dialogMode, setDialogMode] = useState<DeleteDialogMode>(null)
  const [submissionCount, setSubmissionCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function closeDialog() {
    if (submitting) {
      return
    }
    setDialogMode(null)
    setError(null)
  }

  async function handleDeleteClick(event: MouseEvent) {
    event.stopPropagation()
    event.preventDefault()

    setLoading(true)
    setError(null)

    try {
      const response = await submissionsApi.listAll({ initiativeId: initiative.id, size: 1 })
      const count = response.totalElements
      setSubmissionCount(count)
      setDialogMode(count > 0 ? 'blocked' : 'confirm')
    } catch (eligibilityError) {
      setError(resolveApiError(eligibilityError, INITIATIVE_MESSAGES.deleteEligibilityError))
      setDialogMode('confirm')
      setSubmissionCount(0)
    } finally {
      setLoading(false)
    }
  }

  async function handleConfirmDelete() {
    setSubmitting(true)
    setError(null)

    try {
      await initiativesApi.delete(initiative.id)
      setDialogMode(null)
      onSuccess()
    } catch (deleteError) {
      if (isConflictError(deleteError)) {
        setDialogMode('blocked')
        setSubmissionCount(Math.max(submissionCount, 1))
      } else {
        setError(resolveApiError(deleteError, INITIATIVE_MESSAGES.deleteError))
      }
    } finally {
      setSubmitting(false)
    }
  }

  const deleteControl = layout === 'button' ? (
    <Button
      color="error"
      disabled={disabled || loading}
      onClick={(event) => void handleDeleteClick(event)}
      startIcon={<DeleteOutlinedIcon />}
      variant="outlined"
    >
      {INITIATIVE_MESSAGES.confirmDeleteLabel}
    </Button>
  ) : (
    <Tooltip title={INITIATIVE_MESSAGES.confirmDeleteLabel}>
      <span>
        <IconButton
          aria-label={`${INITIATIVE_MESSAGES.confirmDeleteLabel} ${initiative.title}`}
          color="error"
          disabled={disabled || loading}
          onClick={(event) => void handleDeleteClick(event)}
          size="small"
        >
          <DeleteOutlinedIcon fontSize="small" />
        </IconButton>
      </span>
    </Tooltip>
  )

  const statusWarning =
    initiative.status === 'ACTIVE'
      ? INITIATIVE_MESSAGES.confirmDeleteActiveWarning
      : initiative.status === 'EXPIRED'
        ? INITIATIVE_MESSAGES.confirmDeleteExpiredNote
        : null

  const blockedAlternatives =
    initiative.status === 'ACTIVE'
      ? INITIATIVE_MESSAGES.confirmDeleteBlockedAlternativesActive
      : initiative.status === 'EXPIRED'
        ? INITIATIVE_MESSAGES.confirmDeleteBlockedAlternativesExpired
        : INITIATIVE_MESSAGES.confirmDeleteBlockedAlternativesDraft

  return (
    <>
      {deleteControl}

      {dialogMode === 'blocked' ? (
        <InitiativeLifecycleConfirmDialog
          closeLabel={INITIATIVE_MESSAGES.confirmDeleteClose}
          onClose={closeDialog}
          open
          title={INITIATIVE_MESSAGES.confirmDeleteBlockedTitle}
          variant="info"
        >
          <InitiativeDeleteDetails
            status={initiative.status}
            submissionCount={submissionCount}
            title={initiative.title}
          />
          <Typography>{INITIATIVE_MESSAGES.confirmDeleteBlockedSummary}</Typography>
          <Typography sx={{ fontWeight: 600 }}>{INITIATIVE_MESSAGES.confirmDeleteBlockedReason}</Typography>
          <Typography>{INITIATIVE_MESSAGES.confirmDeleteBlockedImpact}</Typography>
          <Typography sx={{ fontWeight: 600 }}>{INITIATIVE_MESSAGES.confirmDeleteBlockedAlternativesHeading}</Typography>
          <List dense disablePadding>
            {blockedAlternatives.map((alternative) => (
              <ListItem key={alternative} disableGutters sx={{ py: 0 }}>
                <ListItemText primary={`• ${alternative}`} />
              </ListItem>
            ))}
          </List>
        </InitiativeLifecycleConfirmDialog>
      ) : null}

      {dialogMode === 'confirm' ? (
        <InitiativeLifecycleConfirmDialog
          closeLabel={INITIATIVE_MESSAGES.formCancel}
          confirmColor="error"
          confirmLabel={INITIATIVE_MESSAGES.confirmDeleteLabel}
          onClose={closeDialog}
          onConfirm={() => void handleConfirmDelete()}
          open
          submitting={submitting}
          title={INITIATIVE_MESSAGES.confirmDeleteTitle}
        >
          {error ? <Alert severity="error">{error}</Alert> : null}
          <InitiativeDeleteDetails
            status={initiative.status}
            submissionCount={submissionCount}
            title={initiative.title}
          />
          <Typography>{INITIATIVE_MESSAGES.confirmDeletePermanentWarning}</Typography>
          <Typography>{INITIATIVE_MESSAGES.confirmDeleteBody}</Typography>
          {statusWarning ? <Typography>{statusWarning}</Typography> : null}
        </InitiativeLifecycleConfirmDialog>
      ) : null}
    </>
  )
}
