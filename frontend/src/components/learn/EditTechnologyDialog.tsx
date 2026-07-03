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
import type { Technology } from '../../types/learn'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import {
  buildTechnologyUpdateRequest,
  createTechnologyFormBaseline,
  getTechnologyFormFieldErrors,
  isTechnologyFormDirty,
  technologyToFormValues,
  type TechnologyFormFieldName,
  type TechnologyFormValues,
} from './learnFormState'
import { LEARN_MESSAGES } from './learnMessages'
import { TechnologyFormFields } from './TechnologyFormFields'

interface EditTechnologyDialogProps {
  open: boolean
  technology: Technology | null
  onClose: () => void
  onSuccess: () => void
}

export function EditTechnologyDialog({ open, technology, onClose, onSuccess }: EditTechnologyDialogProps) {
  const [form, setForm] = useState<TechnologyFormValues | null>(null)
  const [baseline, setBaseline] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<TechnologyFormFieldName, string>>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [discardOpen, setDiscardOpen] = useState(false)

  useEffect(() => {
    if (!open || !technology) {
      return
    }

    const nextForm = technologyToFormValues(technology)
    setForm(nextForm)
    setBaseline(createTechnologyFormBaseline(nextForm))
    setFieldErrors({})
    setFormError(null)
    setSubmitting(false)
    setDiscardOpen(false)
  }, [open, technology])

  const isDirty = useMemo(() => (form ? isTechnologyFormDirty(form, baseline) : false), [baseline, form])

  function updateField<K extends TechnologyFormFieldName>(field: K, value: TechnologyFormValues[K]) {
    setForm((current) => (current ? { ...current, [field]: value } : current))
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
    if (!technology || !form) {
      return
    }

    const clientErrors = getTechnologyFormFieldErrors(form)
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors)
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      const updated = await learnApi.updateTechnology(technology.id, buildTechnologyUpdateRequest(form))
      const existingProjectIds = technology.relatedProjects?.map((project) => project.id) ?? []
      const nextProjectIds = form.linkedProjectIds
      const toAdd = nextProjectIds.filter((projectId) => !existingProjectIds.includes(projectId))
      const toRemove = existingProjectIds.filter((projectId) => !nextProjectIds.includes(projectId))

      for (const projectId of toAdd) {
        await learnApi.addProjectLink(updated.id, projectId)
      }
      for (const projectId of toRemove) {
        await learnApi.removeProjectLink(updated.id, projectId)
      }

      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, LEARN_MESSAGES.updateError))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  if (!form) {
    return null
  }

  return (
    <>
      <Dialog fullWidth maxWidth="md" onClose={requestClose} open={open}>
        <DialogTitle>{LEARN_MESSAGES.editDialogTitle}</DialogTitle>
        <form noValidate onSubmit={(event) => void handleSubmit(event)}>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {formError ? <Alert severity="error">{formError}</Alert> : null}
              <TechnologyFormFields
                fieldErrors={fieldErrors}
                form={form}
                onChange={updateField}
                showFeatured
                showProjectLinks
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button disabled={submitting} onClick={requestClose}>
              {LEARN_MESSAGES.formCancel}
            </Button>
            <Button disabled={submitting} startIcon={submitting ? <CircularProgress size={18} /> : null} type="submit" variant="contained">
              {LEARN_MESSAGES.formSave}
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
