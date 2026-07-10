import { useEffect, useState } from 'react'
import {
  Alert,
  Box,
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
import { projectsApi } from '../../api/projectsApi'
import { projectTeamApi } from '../../api/projectTeamApi'
import type { ProjectMember, ProjectRole } from '../../types/projects'
import { PROJECT_ROLE_LABELS } from '../../types/projects'
import type { ProjectFunctionalRole, ProjectMemberCandidate } from '../../types/projectTeam'
import { PROJECT_FUNCTIONAL_ROLE_LABELS } from '../../types/projectTeam'
import { resolveApiError } from '../../utils/apiErrors'
import { useDialogSelectMenuProps } from '../common/useDialogSelectMenuProps'
import { TEAM_MESSAGES } from './teamMessages'

interface TeamMemberDialogProps {
  open: boolean
  projectId: string | null
  member?: ProjectMember | null
  onClose: () => void
  onSuccess: () => void
}

const DEFAULT_FUNCTIONAL_ROLE: ProjectFunctionalRole = 'OTHER'

export function TeamMemberDialog({ member, onClose, onSuccess, open, projectId }: TeamMemberDialogProps) {
  const isEdit = Boolean(member)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [userSearch, setUserSearch] = useState('')
  const [userResults, setUserResults] = useState<ProjectMemberCandidate[]>([])
  const [selectedUserId, setSelectedUserId] = useState('')
  const [projectRole, setProjectRole] = useState<ProjectRole>('CONTRIBUTOR')
  const [functionalRole, setFunctionalRole] = useState<ProjectFunctionalRole | ''>('')
  const [responsibility, setResponsibility] = useState('')
  const [primaryContact, setPrimaryContact] = useState(false)
  const { menuProps: selectMenuProps, onOpen: handleSelectOpen, onClose: handleSelectClose } =
    useDialogSelectMenuProps()

  useEffect(() => {
    if (!open) {
      return
    }
    setError(null)
    setUserSearch('')
    setUserResults([])
    setSelectedUserId(member?.user.id ?? '')
    setProjectRole(member?.projectRole ?? 'CONTRIBUTOR')
    setFunctionalRole(member?.functionalRole ?? (member ? DEFAULT_FUNCTIONAL_ROLE : ''))
    setResponsibility(member?.responsibility ?? '')
    setPrimaryContact(member?.primaryContact ?? false)
  }, [member, open])

  useEffect(() => {
    if (!open || isEdit || !projectId || !userSearch.trim()) {
      setUserResults([])
      return
    }

    const timer = window.setTimeout(() => {
      void projectTeamApi
        .searchMemberCandidates(projectId, userSearch.trim())
        .then(setUserResults)
        .catch(() => setUserResults([]))
    }, 300)

    return () => window.clearTimeout(timer)
  }, [isEdit, open, projectId, userSearch])

  async function handleSubmit() {
    if (!projectId || (!isEdit && !selectedUserId)) {
      return
    }
    if (!functionalRole) {
      setError('Functional role is required.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await projectsApi.addOrUpdateMember(projectId, {
        userId: isEdit ? member!.user.id : selectedUserId,
        projectRole,
        functionalRole,
        responsibility: responsibility.trim() || undefined,
        primaryContact,
      })
      onSuccess()
      onClose()
    } catch (submitError) {
      setError(resolveApiError(submitError, 'Unable to save team member.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="sm" onClose={() => !submitting && onClose()} open={open}>
      <DialogTitle>{isEdit ? TEAM_MESSAGES.editMember : TEAM_MESSAGES.addMember}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {error ? <Alert severity="error">{error}</Alert> : null}
          {isEdit ? (
            <TextField
              disabled
              fullWidth
              label="User"
              value={`${member!.user.fullName} (${member!.user.email})`}
            />
          ) : (
            <>
              <TextField
                fullWidth
                label={TEAM_MESSAGES.searchUsers}
                onChange={(event) => setUserSearch(event.target.value)}
                placeholder="Name, email, or employee ID"
                value={userSearch}
              />
              <FormControl fullWidth>
                <InputLabel id="team-member-user-label">{TEAM_MESSAGES.selectUser}</InputLabel>
                <Select
                  label={TEAM_MESSAGES.selectUser}
                  labelId="team-member-user-label"
                  MenuProps={selectMenuProps}
                  onChange={(event) => setSelectedUserId(event.target.value)}
                  onClose={handleSelectClose}
                  onOpen={handleSelectOpen}
                  value={selectedUserId}
                >
                  {userResults.map((user) => (
                    <MenuItem key={user.id} value={user.id}>
                      {user.fullName} ({user.email})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </>
          )}
          <FormControl fullWidth>
            <InputLabel id="team-access-role-label">{TEAM_MESSAGES.accessRole}</InputLabel>
            <Select
              label={TEAM_MESSAGES.accessRole}
              labelId="team-access-role-label"
              MenuProps={selectMenuProps}
              onChange={(event) => setProjectRole(event.target.value as ProjectRole)}
              onClose={handleSelectClose}
              onOpen={handleSelectOpen}
              value={projectRole}
            >
              {Object.entries(PROJECT_ROLE_LABELS).map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth required={!isEdit}>
            <InputLabel id="team-functional-role-label" shrink>
              {TEAM_MESSAGES.functionalRole}
            </InputLabel>
            <Select
              displayEmpty={!isEdit}
              label={TEAM_MESSAGES.functionalRole}
              labelId="team-functional-role-label"
              MenuProps={selectMenuProps}
              onChange={(event) => setFunctionalRole(event.target.value as ProjectFunctionalRole)}
              onClose={handleSelectClose}
              onOpen={handleSelectOpen}
              renderValue={(value) =>
                value ? (
                  PROJECT_FUNCTIONAL_ROLE_LABELS[value as ProjectFunctionalRole]
                ) : (
                  <Box component="span" sx={{ color: 'text.secondary' }}>
                    {TEAM_MESSAGES.selectFunctionalRole}
                  </Box>
                )
              }
              value={functionalRole}
            >
              {Object.entries(PROJECT_FUNCTIONAL_ROLE_LABELS).map(([value, label]) => (
                <MenuItem key={value} value={value}>
                  {label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label={TEAM_MESSAGES.responsibility}
            minRows={2}
            multiline
            onChange={(event) => setResponsibility(event.target.value)}
            placeholder="Optional summary of responsibilities on this project"
            value={responsibility}
          />
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
          disabled={submitting || (!isEdit && !selectedUserId) || !functionalRole}
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
