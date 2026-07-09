import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from '@mui/material'
import type { ProjectEnvironment } from '../../types/projectOperational'
import { OPERATIONAL_MESSAGES } from './operationalMessages'

interface EnvironmentDialogProps {
  open: boolean
  mode: 'create' | 'edit'
  initial?: ProjectEnvironment | null
  submitting?: boolean
  onClose: () => void
  onSubmit: (values: { name: string; description?: string }) => void
}

export function EnvironmentDialog({
  initial,
  mode,
  onClose,
  onSubmit,
  open,
  submitting = false,
}: EnvironmentDialogProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  useEffect(() => {
    if (!open) return
    setName(initial?.name ?? '')
    setDescription(initial?.description ?? '')
  }, [initial, open])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    onSubmit({ name: name.trim(), description: description.trim() || undefined })
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={open}>
      <form onSubmit={handleSubmit}>
        <DialogTitle>{mode === 'create' ? OPERATIONAL_MESSAGES.addEnvironment : OPERATIONAL_MESSAGES.editEnvironment}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          <TextField
            autoFocus
            fullWidth
            label={OPERATIONAL_MESSAGES.environmentName}
            onChange={(event) => setName(event.target.value)}
            required
            value={name}
          />
          <TextField
            fullWidth
            label={OPERATIONAL_MESSAGES.description}
            minRows={3}
            multiline
            onChange={(event) => setDescription(event.target.value)}
            value={description}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button disabled={submitting || !name.trim()} type="submit" variant="contained">
            {mode === 'create' ? 'Create' : 'Save'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
