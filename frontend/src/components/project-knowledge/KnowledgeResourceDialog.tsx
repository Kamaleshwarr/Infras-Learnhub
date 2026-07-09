import { useEffect, useMemo, useState } from 'react'
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
} from '@mui/material'
import type { KnowledgeCategory, ProjectKnowledgeFolder, ProjectKnowledgeItem } from '../../types/projectKnowledge'
import { KNOWLEDGE_CATEGORY_LABELS } from '../../types/projectKnowledge'
import { KNOWLEDGE_MESSAGES } from './knowledgeMessages'

const CATEGORY_OPTIONS = Object.entries(KNOWLEDGE_CATEGORY_LABELS) as [KnowledgeCategory, string][]

interface KnowledgeResourceDialogProps {
  open: boolean
  mode: 'create' | 'edit'
  folders: ProjectKnowledgeFolder[]
  defaultFolderId?: string | null
  initialItem?: ProjectKnowledgeItem | null
  submitting?: boolean
  onClose: () => void
  onSubmit: (values: {
    title: string
    description?: string
    category: KnowledgeCategory
    externalUrl: string
    folderId?: string | null
  }) => void
}

export function KnowledgeResourceDialog({
  defaultFolderId,
  folders,
  initialItem,
  mode,
  onClose,
  onSubmit,
  open,
  submitting = false,
}: KnowledgeResourceDialogProps) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState<KnowledgeCategory>('EXTERNAL_LINKS')
  const [externalUrl, setExternalUrl] = useState('')
  const [folderId, setFolderId] = useState<string>('')

  const folderOptions = useMemo(
    () => [{ id: '', name: KNOWLEDGE_MESSAGES.rootFolder }, ...folders.map((folder) => ({ id: folder.id, name: folder.name }))],
    [folders],
  )

  useEffect(() => {
    if (!open) {
      return
    }
    setTitle(initialItem?.title ?? '')
    setDescription(initialItem?.description ?? '')
    setCategory(initialItem?.category ?? 'EXTERNAL_LINKS')
    setExternalUrl(initialItem?.externalUrl ?? '')
    setFolderId(initialItem?.folderId ?? defaultFolderId ?? '')
  }, [defaultFolderId, initialItem, open])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    onSubmit({
      title: title.trim(),
      description: description.trim() || undefined,
      category,
      externalUrl: externalUrl.trim(),
      folderId: folderId || null,
    })
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={open}>
      <form onSubmit={handleSubmit}>
        <DialogTitle>{mode === 'create' ? KNOWLEDGE_MESSAGES.addResource : KNOWLEDGE_MESSAGES.editResource}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          <TextField
            autoFocus
            fullWidth
            label={KNOWLEDGE_MESSAGES.resourceTitle}
            onChange={(event) => setTitle(event.target.value)}
            required
            value={title}
          />
          <TextField
            fullWidth
            label={KNOWLEDGE_MESSAGES.resourceDescription}
            minRows={3}
            multiline
            onChange={(event) => setDescription(event.target.value)}
            value={description}
          />
          <FormControl fullWidth>
            <InputLabel id="knowledge-resource-type-label">{KNOWLEDGE_MESSAGES.resourceType}</InputLabel>
            <Select
              label={KNOWLEDGE_MESSAGES.resourceType}
              labelId="knowledge-resource-type-label"
              onChange={(event) => setCategory(event.target.value as KnowledgeCategory)}
              value={category}
            >
              {CATEGORY_OPTIONS.map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label={KNOWLEDGE_MESSAGES.resourceUrl}
            onChange={(event) => setExternalUrl(event.target.value)}
            placeholder="https://"
            required
            type="url"
            value={externalUrl}
          />
          <FormControl fullWidth>
            <InputLabel id="knowledge-parent-folder-label">{KNOWLEDGE_MESSAGES.parentFolder}</InputLabel>
            <Select
              label={KNOWLEDGE_MESSAGES.parentFolder}
              labelId="knowledge-parent-folder-label"
              onChange={(event) => setFolderId(event.target.value)}
              value={folderId}
            >
              {folderOptions.map((folder) => (
                <MenuItem key={folder.id || 'root'} value={folder.id}>
                  {folder.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button disabled={submitting || !title.trim() || !externalUrl.trim()} type="submit" variant="contained">
            {mode === 'create' ? 'Add resource' : 'Save'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
