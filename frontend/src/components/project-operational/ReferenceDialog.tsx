import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material'
import type { EnvironmentReferenceType, ProjectEnvironmentReference } from '../../types/projectOperational'
import { ENVIRONMENT_REFERENCE_TYPE_LABELS } from '../../types/projectOperational'
import { OPERATIONAL_MESSAGES } from './operationalMessages'

interface ReferenceDialogProps {
  open: boolean
  mode: 'create' | 'edit'
  initial?: ProjectEnvironmentReference | null
  submitting?: boolean
  onClose: () => void
  onSubmit: (values: {
    name: string
    referenceType: EnvironmentReferenceType
    url: string
    description?: string
  }) => void
}

export function ReferenceDialog({
  initial,
  mode,
  onClose,
  onSubmit,
  open,
  submitting = false,
}: ReferenceDialogProps) {
  const [name, setName] = useState('')
  const [referenceType, setReferenceType] = useState<EnvironmentReferenceType>('APPLICATION')
  const [url, setUrl] = useState('')
  const [description, setDescription] = useState('')

  useEffect(() => {
    if (!open) return
    setName(initial?.name ?? '')
    setReferenceType(initial?.referenceType ?? 'APPLICATION')
    setUrl(initial?.url ?? '')
    setDescription(initial?.description ?? '')
  }, [initial, open])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    onSubmit({
      name: name.trim(),
      referenceType,
      url: url.trim(),
      description: description.trim() || undefined,
    })
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={open}>
      <form onSubmit={handleSubmit}>
        <DialogTitle>{mode === 'create' ? OPERATIONAL_MESSAGES.addReference : OPERATIONAL_MESSAGES.editReference}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          <TextField
            autoFocus
            fullWidth
            label={OPERATIONAL_MESSAGES.referenceName}
            onChange={(event) => setName(event.target.value)}
            required
            value={name}
          />
          <FormControl fullWidth>
            <InputLabel id="reference-type-label">{OPERATIONAL_MESSAGES.referenceType}</InputLabel>
            <Select
              label={OPERATIONAL_MESSAGES.referenceType}
              labelId="reference-type-label"
              onChange={(event) => setReferenceType(event.target.value as EnvironmentReferenceType)}
              value={referenceType}
            >
              {Object.entries(ENVIRONMENT_REFERENCE_TYPE_LABELS).map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            helperText={OPERATIONAL_MESSAGES.urlHelper}
            label={OPERATIONAL_MESSAGES.referenceUrl}
            onChange={(event) => setUrl(event.target.value)}
            required
            value={url}
          />
          <TextField
            fullWidth
            label={OPERATIONAL_MESSAGES.description}
            minRows={2}
            multiline
            onChange={(event) => setDescription(event.target.value)}
            value={description}
          />
          <Typography color="text.secondary" variant="caption">
            {OPERATIONAL_MESSAGES.urlHelper}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button disabled={submitting || !name.trim() || !url.trim()} type="submit" variant="contained">
            {mode === 'create' ? 'Add' : 'Save'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
