import { useEffect, useMemo, useState } from 'react'
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
  Typography,
} from '@mui/material'
import { initiativesApi } from '../../api/initiativesApi'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { InitiativeFormFields } from './InitiativeFormFields'
import { todayUtcDateInput } from './initiativeDateUtils'
import {
  buildCreateInitiativeRequest,
  createEmptyInitiativeForm,
  createInitiativeFormBaseline,
  getInitiativeFormFieldErrors,
  isInitiativeFormDirty,
  type InitiativeFormFieldName,
  type InitiativeFormValues,
} from './initiativeFormState'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface CreateInitiativeDialogProps {
  open: boolean
  onClose: () => void
  onSuccess: () => void
}

export function CreateInitiativeDialog({ open, onClose, onSuccess }: CreateInitiativeDialogProps) {
  const [form, setForm] = useState<InitiativeFormValues>(() => createEmptyInitiativeForm())
  const [baseline, setBaseline] = useState(() => createInitiativeFormBaseline(createEmptyInitiativeForm()))
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<InitiativeFormFieldName, string>>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [discardOpen, setDiscardOpen] = useState(false)

  useEffect(() => {
    if (!open) {
      return
    }

    const emptyForm = createEmptyInitiativeForm()
    setForm(emptyForm)
    setBaseline(createInitiativeFormBaseline(emptyForm))
    setFieldErrors({})
    setFormError(null)
    setSubmitting(false)
    setDiscardOpen(false)
  }, [open])

  const isDirty = useMemo(() => isInitiativeFormDirty(form, baseline), [baseline, form])

  function updateField<K extends InitiativeFormFieldName>(field: K, value: InitiativeFormValues[K]) {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      if (!(field in current)) {
        return current
      }
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  function requestClose() {
    if (submitting) {
      return
    }

    if (isDirty) {
      setDiscardOpen(true)
      return
    }

    onClose()
  }

  function confirmDiscard() {
    setDiscardOpen(false)
    onClose()
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const clientErrors = getInitiativeFormFieldErrors(form, { mode: 'create' })
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors)
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      await initiativesApi.create(buildCreateInitiativeRequest(form))
      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, INITIATIVE_MESSAGES.createError))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <Dialog fullWidth maxWidth="md" onClose={requestClose} open={open}>
        <DialogTitle>{INITIATIVE_MESSAGES.createDialogTitle}</DialogTitle>
        <form noValidate onSubmit={(event) => void handleSubmit(event)}>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {formError ? <Alert severity="error">{formError}</Alert> : null}
              <InitiativeFormFields
                disabled={submitting}
                fieldErrors={fieldErrors}
                minExpiryDate={form.startDate || todayUtcDateInput()}
                minStartDate={todayUtcDateInput()}
                onChange={updateField}
                values={form}
              />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button disabled={submitting} onClick={requestClose} type="button">
              {INITIATIVE_MESSAGES.formCancel}
            </Button>
            <Button disabled={submitting} type="submit" variant="contained">
              {submitting ? <CircularProgress color="inherit" size={24} /> : INITIATIVE_MESSAGES.formCreate}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <Dialog onClose={() => setDiscardOpen(false)} open={discardOpen}>
        <DialogTitle>{INITIATIVE_MESSAGES.formDiscardTitle}</DialogTitle>
        <DialogContent>
          <Typography>{INITIATIVE_MESSAGES.formDiscardBody}</Typography>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={() => setDiscardOpen(false)}>{INITIATIVE_MESSAGES.formKeepEditing}</Button>
          <Button color="error" onClick={confirmDiscard} variant="contained">
            {INITIATIVE_MESSAGES.formDiscardConfirm}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
