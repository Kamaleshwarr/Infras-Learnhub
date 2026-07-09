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
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from '@mui/material'
import { projectsApi } from '../../api/projectsApi'
import type { ProjectAccessType, ProjectDetail, ProjectStatus } from '../../types/projects'
import { PROJECT_ACCESS_LABELS, PROJECT_STATUS_LABELS } from '../../types/projects'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { PROJECT_MESSAGES } from './projectMessages'

interface EditProjectDialogProps {
  open: boolean
  project: ProjectDetail | null
  onClose: () => void
  onSuccess: () => void
}

export function EditProjectDialog({ onClose, onSuccess, open, project }: EditProjectDialogProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [accessType, setAccessType] = useState<ProjectAccessType>('PUBLIC')
  const [status, setStatus] = useState<ProjectStatus>('ACTIVE')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!open || !project) {
      return
    }
    setName(project.name)
    setDescription(project.description ?? '')
    setAccessType(project.accessType)
    setStatus(project.status)
    setFieldErrors({})
    setFormError(null)
    setSubmitting(false)
  }, [open, project])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!project) {
      return
    }

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
      await projectsApi.update(project.id, {
        name: name.trim(),
        description: description.trim() || undefined,
        accessType,
        status,
      })
      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to update project.'))
      setFieldErrors(getValidationErrors(error) ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={() => !submitting && onClose()} open={open}>
      <DialogTitle>{PROJECT_MESSAGES.editProject}</DialogTitle>
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
              <InputLabel id="edit-project-status-label">{PROJECT_MESSAGES.status}</InputLabel>
              <Select
                label={PROJECT_MESSAGES.status}
                labelId="edit-project-status-label"
                onChange={(event) => setStatus(event.target.value as ProjectStatus)}
                value={status}
              >
                {Object.entries(PROJECT_STATUS_LABELS).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel id="edit-project-access-label">{PROJECT_MESSAGES.visibility}</InputLabel>
              <Select
                label={PROJECT_MESSAGES.visibility}
                labelId="edit-project-access-label"
                onChange={(event) => setAccessType(event.target.value as ProjectAccessType)}
                value={accessType}
              >
                <MenuItem value="PUBLIC">{PROJECT_ACCESS_LABELS.PUBLIC}</MenuItem>
                <MenuItem value="MEMBERS_ONLY">{PROJECT_ACCESS_LABELS.MEMBERS_ONLY}</MenuItem>
              </Select>
            </FormControl>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button disabled={submitting} onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={submitting} startIcon={submitting ? <CircularProgress size={18} /> : null} type="submit" variant="contained">
            Save
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
