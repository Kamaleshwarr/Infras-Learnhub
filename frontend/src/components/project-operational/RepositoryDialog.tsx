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
import type { ProjectLinkedRepository, RepositoryProvider, RepositoryType } from '../../types/projectOperational'
import { REPOSITORY_PROVIDER_LABELS, REPOSITORY_TYPE_LABELS } from '../../types/projectOperational'
import { OPERATIONAL_MESSAGES } from './operationalMessages'

interface RepositoryDialogProps {
  open: boolean
  mode: 'create' | 'edit'
  initial?: ProjectLinkedRepository | null
  submitting?: boolean
  onClose: () => void
  onSubmit: (values: {
    name: string
    description?: string
    repositoryType: RepositoryType
    provider: RepositoryProvider
    repositoryUrl: string
    defaultBranch?: string
  }) => void
}

export function RepositoryDialog({
  initial,
  mode,
  onClose,
  onSubmit,
  open,
  submitting = false,
}: RepositoryDialogProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [repositoryType, setRepositoryType] = useState<RepositoryType>('BACKEND')
  const [provider, setProvider] = useState<RepositoryProvider>('GITHUB')
  const [repositoryUrl, setRepositoryUrl] = useState('')
  const [defaultBranch, setDefaultBranch] = useState('')

  useEffect(() => {
    if (!open) return
    setName(initial?.name ?? '')
    setDescription(initial?.description ?? '')
    setRepositoryType(initial?.repositoryType ?? 'BACKEND')
    setProvider(initial?.provider ?? 'GITHUB')
    setRepositoryUrl(initial?.repositoryUrl ?? '')
    setDefaultBranch(initial?.defaultBranch ?? '')
  }, [initial, open])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    onSubmit({
      name: name.trim(),
      description: description.trim() || undefined,
      repositoryType,
      provider,
      repositoryUrl: repositoryUrl.trim(),
      defaultBranch: defaultBranch.trim() || undefined,
    })
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={open}>
      <form onSubmit={handleSubmit}>
        <DialogTitle>{mode === 'create' ? OPERATIONAL_MESSAGES.addRepository : OPERATIONAL_MESSAGES.editRepository}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          <TextField
            autoFocus
            fullWidth
            label={OPERATIONAL_MESSAGES.repositoryName}
            onChange={(event) => setName(event.target.value)}
            required
            value={name}
          />
          <FormControl fullWidth>
            <InputLabel id="repository-type-label">{OPERATIONAL_MESSAGES.repositoryType}</InputLabel>
            <Select
              label={OPERATIONAL_MESSAGES.repositoryType}
              labelId="repository-type-label"
              onChange={(event) => setRepositoryType(event.target.value as RepositoryType)}
              value={repositoryType}
            >
              {Object.entries(REPOSITORY_TYPE_LABELS).map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id="provider-label">{OPERATIONAL_MESSAGES.provider}</InputLabel>
            <Select
              label={OPERATIONAL_MESSAGES.provider}
              labelId="provider-label"
              onChange={(event) => setProvider(event.target.value as RepositoryProvider)}
              value={provider}
            >
              {Object.entries(REPOSITORY_PROVIDER_LABELS).map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            helperText={OPERATIONAL_MESSAGES.urlHelper}
            label={OPERATIONAL_MESSAGES.repositoryUrl}
            onChange={(event) => setRepositoryUrl(event.target.value)}
            required
            value={repositoryUrl}
          />
          <TextField
            fullWidth
            label={OPERATIONAL_MESSAGES.defaultBranch}
            onChange={(event) => setDefaultBranch(event.target.value)}
            placeholder="main"
            value={defaultBranch}
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
          <Button disabled={submitting || !name.trim() || !repositoryUrl.trim()} type="submit" variant="contained">
            {mode === 'create' ? 'Create' : 'Save'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
