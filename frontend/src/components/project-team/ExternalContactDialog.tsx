import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from '@mui/material'
import { projectTeamApi } from '../../api/projectTeamApi'
import type { ExternalContactType, ProjectExternalContact } from '../../types/projectTeam'
import { EXTERNAL_CONTACT_TYPE_LABELS } from '../../types/projectTeam'
import { resolveApiError } from '../../utils/apiErrors'
import { TEAM_MESSAGES } from './teamMessages'

interface ExternalContactDialogProps {
  open: boolean
  projectId: string | null
  contact?: ProjectExternalContact | null
  onClose: () => void
  onSuccess: () => void
}

export function ExternalContactDialog({
  contact,
  onClose,
  onSuccess,
  open,
  projectId,
}: ExternalContactDialogProps) {
  const isEdit = Boolean(contact)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [name, setName] = useState('')
  const [contactType, setContactType] = useState<ExternalContactType>('BUSINESS')
  const [roleTitle, setRoleTitle] = useState('')
  const [organization, setOrganization] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [contactUrl, setContactUrl] = useState('')
  const [notes, setNotes] = useState('')
  const [primaryContact, setPrimaryContact] = useState(false)

  useEffect(() => {
    if (!open) {
      return
    }
    setError(null)
    setName(contact?.name ?? '')
    setContactType(contact?.contactType ?? 'BUSINESS')
    setRoleTitle(contact?.roleTitle ?? '')
    setOrganization(contact?.organization ?? '')
    setEmail(contact?.email ?? '')
    setPhone(contact?.phone ?? '')
    setContactUrl(contact?.contactUrl ?? '')
    setNotes(contact?.notes ?? '')
    setPrimaryContact(contact?.primaryContact ?? false)
  }, [contact, open])

  async function handleSubmit() {
    if (!projectId || !name.trim()) {
      return
    }
    setSubmitting(true)
    setError(null)
    const payload = {
      name: name.trim(),
      contactType,
      roleTitle: roleTitle.trim() || undefined,
      organization: organization.trim() || undefined,
      email: email.trim() || undefined,
      phone: phone.trim() || undefined,
      contactUrl: contactUrl.trim() || undefined,
      notes: notes.trim() || undefined,
      primaryContact,
      active: contact?.active ?? true,
    }
    try {
      if (isEdit) {
        await projectTeamApi.updateExternalContact(projectId, contact!.id, payload)
      } else {
        await projectTeamApi.createExternalContact(projectId, payload)
      }
      onSuccess()
      onClose()
    } catch (submitError) {
      setError(resolveApiError(submitError, 'Unable to save external contact.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={() => !submitting && onClose()} open={open}>
      <DialogTitle>{isEdit ? TEAM_MESSAGES.editExternalContact : TEAM_MESSAGES.addExternalContact}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {error ? <Alert severity="error">{error}</Alert> : null}
          <TextField fullWidth label="Name" onChange={(event) => setName(event.target.value)} required value={name} />
          <FormControl fullWidth>
            <InputLabel id="external-contact-type-label">Contact type</InputLabel>
            <Select
              label="Contact type"
              labelId="external-contact-type-label"
              onChange={(event) => setContactType(event.target.value as ExternalContactType)}
              value={contactType}
            >
              {Object.entries(EXTERNAL_CONTACT_TYPE_LABELS).map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField fullWidth label="Role / title" onChange={(event) => setRoleTitle(event.target.value)} value={roleTitle} />
          <TextField
            fullWidth
            label="Organization"
            onChange={(event) => setOrganization(event.target.value)}
            value={organization}
          />
          <TextField fullWidth label="Email" onChange={(event) => setEmail(event.target.value)} type="email" value={email} />
          <TextField fullWidth label="Phone" onChange={(event) => setPhone(event.target.value)} value={phone} />
          <TextField fullWidth label="Contact URL" onChange={(event) => setContactUrl(event.target.value)} value={contactUrl} />
          <TextField fullWidth label="Notes" minRows={2} multiline onChange={(event) => setNotes(event.target.value)} value={notes} />
          <FormControlLabel
            control={
              <Checkbox
                checked={primaryContact}
                onChange={(event) => setPrimaryContact(event.target.checked)}
              />
            }
            label={TEAM_MESSAGES.primaryContact}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button disabled={submitting} onClick={onClose}>
          Cancel
        </Button>
        <Button
          disabled={submitting || !name.trim()}
          onClick={() => void handleSubmit()}
          startIcon={submitting ? <CircularProgress size={16} /> : undefined}
          variant="contained"
        >
          Save
        </Button>
      </DialogActions>
    </Dialog>
  )
}
