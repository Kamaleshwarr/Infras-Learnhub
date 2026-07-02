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
      } else if (confirmAction === 'hide') {
        await learnApi.hideTechnology(technology.id)
      } else {
        await learnApi.archiveTechnology(technology.id)
      }
      onSuccess(confirmAction)
      setConfirmAction(null)
    } catch (error) {
      onError(
        resolveApiError(
          error,
          confirmAction === 'publish'
            ? LEARN_MESSAGES.publishError
            : confirmAction === 'hide'
              ? LEARN_MESSAGES.hideError
              : LEARN_MESSAGES.archiveError,
        ),
      )
    } finally {
      setSubmitting(false)
    }
  }

  function confirmTitle() {
    if (confirmAction === 'publish') {
      return LEARN_MESSAGES.publishConfirmTitle
    }
    if (confirmAction === 'hide') {
      return LEARN_MESSAGES.hideConfirmTitle
    }
    return LEARN_MESSAGES.archiveConfirmTitle
  }

  function confirmBody() {
    if (confirmAction === 'publish') {
      return LEARN_MESSAGES.publishConfirmBody
    }
    if (confirmAction === 'hide') {
      return LEARN_MESSAGES.hideConfirmBody
    }
    return LEARN_MESSAGES.archiveConfirmBody
  }

  return (
    <>
      <Stack direction="row" spacing={1}>
        {technology.status === 'HIDDEN' ? (
          <Button onClick={() => setConfirmAction('publish')} variant="contained">
            {LEARN_MESSAGES.publishAction}
          </Button>
        ) : null}
        {technology.status === 'PUBLISHED' ? (
          <>
            <Button onClick={() => setConfirmAction('hide')} variant="outlined">
              {LEARN_MESSAGES.hideAction}
            </Button>
            <Button color="warning" onClick={() => setConfirmAction('archive')} variant="outlined">
              {LEARN_MESSAGES.archiveAction}
            </Button>
          </>
        ) : null}
      </Stack>

      <Dialog onClose={() => setConfirmAction(null)} open={Boolean(confirmAction)}>
        <DialogTitle>{confirmTitle()}</DialogTitle>
        <DialogContent>{confirmBody()}</DialogContent>
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
