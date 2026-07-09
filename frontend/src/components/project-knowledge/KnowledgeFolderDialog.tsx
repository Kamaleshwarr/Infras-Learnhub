import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
} from '@mui/material'
import type { ProjectKnowledgeFolder } from '../../types/projectKnowledge'
import { KNOWLEDGE_MESSAGES } from './knowledgeMessages'

interface KnowledgeFolderDialogProps {
  open: boolean
  mode: 'create' | 'edit'
  initialFolder?: ProjectKnowledgeFolder | null
  submitting?: boolean
  onClose: () => void
  onSubmit: (values: { name: string; description?: string }) => void
}

export function KnowledgeFolderDialog({
  initialFolder,
  mode,
  onClose,
  onSubmit,
  open,
  submitting = false,
}: KnowledgeFolderDialogProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  useEffect(() => {
    if (!open) {
      return
    }
    setName(initialFolder?.name ?? '')
    setDescription(initialFolder?.description ?? '')
  }, [initialFolder, open])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    onSubmit({
      name: name.trim(),
      description: description.trim() || undefined,
    })
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={open}>
      <form onSubmit={handleSubmit}>
        <DialogTitle>{mode === 'create' ? KNOWLEDGE_MESSAGES.addFolder : KNOWLEDGE_MESSAGES.editFolder}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          <TextField
            autoFocus
            fullWidth
            label={KNOWLEDGE_MESSAGES.folderName}
            onChange={(event) => setName(event.target.value)}
            required
            value={name}
          />
          <TextField
            fullWidth
            label={KNOWLEDGE_MESSAGES.folderDescription}
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
