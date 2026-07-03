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
import { learnApi } from '../../api/learnApi'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import {
  buildTechnologyCreateRequest,
  createEmptyTechnologyForm,
  createTechnologyFormBaseline,
  getTechnologyFormFieldErrors,
  isTechnologyFormDirty,
  type TechnologyFormFieldName,
  type TechnologyFormValues,
} from './learnFormState'
import { LEARN_MESSAGES } from './learnMessages'
import { TechnologyFormFields } from './TechnologyFormFields'

interface CreateTechnologyDialogProps {
  open: boolean
  onClose: () => void
  onSuccess: () => void
}

export function CreateTechnologyDialog({ open, onClose, onSuccess }: CreateTechnologyDialogProps) {
  const [form, setForm] = useState<TechnologyFormValues>(() => createEmptyTechnologyForm())
  const [baseline, setBaseline] = useState(() => createTechnologyFormBaseline(createEmptyTechnologyForm()))
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<TechnologyFormFieldName, string>>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [discardOpen, setDiscardOpen] = useState(false)

  useEffect(() => {
    if (!open) {
      return
    }

    const emptyForm = createEmptyTechnologyForm()
    setForm(emptyForm)
    setBaseline(createTechnologyFormBaseline(emptyForm))
    setFieldErrors({})
    setFormError(null)
    setSubmitting(false)
    setDiscardOpen(false)
  }, [open])

  const isDirty = useMemo(() => isTechnologyFormDirty(form, baseline), [baseline, form])

  function updateField<K extends TechnologyFormFieldName>(field: K, value: TechnologyFormValues[K]) {
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

    const clientErrors = getTechnologyFormFieldErrors(form)
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors)
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      await learnApi.createTechnology(buildTechnologyCreateRequest(form))
      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, LEARN_MESSAGES.createError))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <Dialog fullWidth maxWidth="md" onClose={requestClose} open={open}>
        <DialogTitle>{LEARN_MESSAGES.createDialogTitle}</DialogTitle>
        <form noValidate onSubmit={(event) => void handleSubmit(event)}>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {formError ? <Alert severity="error">{formError}</Alert> : null}
              <TechnologyFormFields fieldErrors={fieldErrors} form={form} onChange={updateField} />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button disabled={submitting} onClick={requestClose}>
              {LEARN_MESSAGES.formCancel}
            </Button>
            <Button disabled={submitting} startIcon={submitting ? <CircularProgress size={18} /> : null} type="submit" variant="contained">
              {LEARN_MESSAGES.formCreate}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <Dialog onClose={() => setDiscardOpen(false)} open={discardOpen}>
        <DialogTitle>{LEARN_MESSAGES.formDiscardTitle}</DialogTitle>
        <DialogContent>
          <Typography>{LEARN_MESSAGES.formDiscardBody}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDiscardOpen(false)}>{LEARN_MESSAGES.formKeepEditing}</Button>
          <Button color="error" onClick={confirmDiscard}>
            {LEARN_MESSAGES.formDiscardConfirm}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
