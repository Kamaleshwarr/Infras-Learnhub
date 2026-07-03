import { useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack } from '@mui/material'
import { learnApi } from '../../api/learnApi'
import type { Technology, TechnologyLifecycleAction } from '../../types/learn'
import { resolveApiError } from '../../utils/apiErrors'
import { LEARN_MESSAGES } from './learnMessages'

interface TechnologyLifecycleActionsProps {
  technology: Technology
  onSuccess: (action: TechnologyLifecycleAction) => void
  onError: (message: string) => void
}

export function TechnologyLifecycleActions({ technology, onSuccess, onError }: TechnologyLifecycleActionsProps) {
  const [confirmAction, setConfirmAction] = useState<TechnologyLifecycleAction | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleConfirm() {
    if (!confirmAction) {
      return
    }

    setSubmitting(true)
    try {
      if (confirmAction === 'publish') {
        await learnApi.publishTechnology(technology.id)
      } else {
        await learnApi.archiveTechnology(technology.id)
      }
      onSuccess(confirmAction)
      setConfirmAction(null)
    } catch (error) {
      onError(
        resolveApiError(
          error,
          confirmAction === 'publish' ? LEARN_MESSAGES.publishError : LEARN_MESSAGES.archiveError,
        ),
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <Stack direction="row" spacing={1}>
        {technology.status === 'DRAFT' ? (
          <Button onClick={() => setConfirmAction('publish')} variant="contained">
            {LEARN_MESSAGES.publishAction}
          </Button>
        ) : null}
        {technology.status === 'PUBLISHED' ? (
          <Button color="warning" onClick={() => setConfirmAction('archive')} variant="outlined">
            {LEARN_MESSAGES.archiveAction}
          </Button>
        ) : null}
      </Stack>

      <Dialog onClose={() => setConfirmAction(null)} open={Boolean(confirmAction)}>
        <DialogTitle>
          {confirmAction === 'publish' ? LEARN_MESSAGES.publishConfirmTitle : LEARN_MESSAGES.archiveConfirmTitle}
        </DialogTitle>
        <DialogContent>
          {confirmAction === 'publish' ? LEARN_MESSAGES.publishConfirmBody : LEARN_MESSAGES.archiveConfirmBody}
        </DialogContent>
        <DialogActions>
          <Button disabled={submitting} onClick={() => setConfirmAction(null)}>
            {LEARN_MESSAGES.formCancel}
          </Button>
          <Button disabled={submitting} onClick={() => void handleConfirm()} variant="contained">
            {LEARN_MESSAGES.confirmAction}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
