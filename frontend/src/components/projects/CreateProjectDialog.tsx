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
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from '@mui/material'
import { projectsApi } from '../../api/projectsApi'
import type { ProjectAccessType } from '../../types/projects'
import { PROJECT_ACCESS_LABELS } from '../../types/projects'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { PROJECT_MESSAGES } from './projectMessages'

interface CreateProjectDialogProps {
  open: boolean
  onClose: () => void
  onSuccess: (projectId: string) => void
}

export function CreateProjectDialog({ onClose, onSuccess, open }: CreateProjectDialogProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [accessType, setAccessType] = useState<ProjectAccessType>('PUBLIC')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const isDirty = useMemo(() => name.trim().length > 0 || description.trim().length > 0, [description, name])

  useEffect(() => {
    if (!open) {
      return
    }
    setName('')
    setDescription('')
    setAccessType('PUBLIC')
    setFieldErrors({})
    setFormError(null)
    setSubmitting(false)
  }, [open])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const errors: Record<string, string> = {}
    if (!name.trim()) {
      errors.name = 'Project name is required'
    }
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setSubmitting(true)
    setFormError(null)
    setFieldErrors({})

    try {
      const project = await projectsApi.create({
        name: name.trim(),
        description: description.trim() || undefined,
        accessType,
      })
      onSuccess(project.id)
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to create project.'))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={() => !submitting && onClose()} open={open}>
      <DialogTitle>{PROJECT_MESSAGES.createProject}</DialogTitle>
      <form noValidate onSubmit={(event) => void handleSubmit(event)}>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {formError ? <Alert severity="error">{formError}</Alert> : null}
            <TextField
              error={Boolean(fieldErrors.name)}
              fullWidth
              helperText={fieldErrors.name}
              label="Project name"
              onChange={(event) => setName(event.target.value)}
              required
              value={name}
            />
            <TextField
              fullWidth
              label="Description"
              minRows={3}
              multiline
              onChange={(event) => setDescription(event.target.value)}
              value={description}
            />
            <FormControl fullWidth>
              <InputLabel id="create-project-access-label">{PROJECT_MESSAGES.visibility}</InputLabel>
              <Select
                label={PROJECT_MESSAGES.visibility}
                labelId="create-project-access-label"
                onChange={(event) => setAccessType(event.target.value as ProjectAccessType)}
                value={accessType}
              >
                <MenuItem value="PUBLIC">{PROJECT_ACCESS_LABELS.PUBLIC}</MenuItem>
                <MenuItem value="MEMBERS_ONLY">{PROJECT_ACCESS_LABELS.MEMBERS_ONLY}</MenuItem>
              </Select>
              <FormHelperText>Members-only projects are visible only to assigned members and administrators.</FormHelperText>
            </FormControl>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button disabled={submitting} onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={submitting || !isDirty} startIcon={submitting ? <CircularProgress size={18} /> : null} type="submit" variant="contained">
            Create
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
