import { useCallback, useEffect, useState } from 'react'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { projectsApi } from '../../api/projectsApi'
import { usersApi } from '../../api/usersApi'
import type { ProjectMember, ProjectRole } from '../../types/projects'
import { PROJECT_ROLE_LABELS } from '../../types/projects'
import type { UserSummary } from '../../types/users'
import { resolveApiError } from '../../utils/apiErrors'
import { PROJECT_MESSAGES } from './projectMessages'

interface ManageProjectMembersDialogProps {
  open: boolean
  projectId: string | null
  onClose: () => void
  onSuccess: () => void
}

export function ManageProjectMembersDialog({ onClose, onSuccess, open, projectId }: ManageProjectMembersDialogProps) {
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [userSearch, setUserSearch] = useState('')
  const [userResults, setUserResults] = useState<UserSummary[]>([])
  const [selectedUserId, setSelectedUserId] = useState('')
  const [selectedRole, setSelectedRole] = useState<ProjectRole>('VIEWER')
  const [submitting, setSubmitting] = useState(false)

  const loadMembers = useCallback(async () => {
    if (!projectId) {
      return
    }
    setLoading(true)
    setError(null)
    try {
      const response = await projectsApi.listMembers(projectId)
      setMembers(response)
    } catch (loadError) {
      setError(resolveApiError(loadError, 'Unable to load project members.'))
      setMembers([])
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    if (!open) {
      return
    }
    void loadMembers()
    setUserSearch('')
    setUserResults([])
    setSelectedUserId('')
    setSelectedRole('VIEWER')
  }, [loadMembers, open])

  useEffect(() => {
    if (!open || !userSearch.trim()) {
      setUserResults([])
      return
    }

    const timer = window.setTimeout(() => {
      void usersApi
        .list({ fullName: userSearch.trim(), size: 10, page: 0 })
        .then((response) => setUserResults(response.content))
        .catch(() => setUserResults([]))
    }, 300)

    return () => window.clearTimeout(timer)
  }, [open, userSearch])

  async function handleAddMember() {
    if (!projectId || !selectedUserId) {
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await projectsApi.addOrUpdateMember(projectId, {
        userId: selectedUserId,
        projectRole: selectedRole,
      })
      setSelectedUserId('')
      setUserSearch('')
      await loadMembers()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, 'Unable to update project member.'))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRemoveMember(userId: string) {
    if (!projectId) {
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await projectsApi.removeMember(projectId, userId)
      await loadMembers()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, 'Unable to remove project member.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="md" onClose={() => !submitting && onClose()} open={open}>
      <DialogTitle>{PROJECT_MESSAGES.manageMembers}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {error ? <Alert severity="error">{error}</Alert> : null}
          <Stack spacing={1.5}>
            <Typography variant="subtitle2">Add member</Typography>
            <TextField
              fullWidth
              label="Search users"
              onChange={(event) => setUserSearch(event.target.value)}
              placeholder="Name, email, or employee ID"
              value={userSearch}
            />
            <FormControl fullWidth>
              <InputLabel id="member-user-label">User</InputLabel>
              <Select
                label="User"
                labelId="member-user-label"
                onChange={(event) => setSelectedUserId(event.target.value)}
                value={selectedUserId}
              >
                {userResults.map((user) => (
                  <MenuItem key={user.id} value={user.id}>
                    {user.fullName} ({user.email})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel id="member-role-label">Role</InputLabel>
              <Select
                label="Role"
                labelId="member-role-label"
                onChange={(event) => setSelectedRole(event.target.value as ProjectRole)}
                value={selectedRole}
              >
                {Object.entries(PROJECT_ROLE_LABELS).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button disabled={!selectedUserId || submitting} onClick={() => void handleAddMember()} variant="outlined">
              Add or update member
            </Button>
          </Stack>
          {loading ? (
            <Stack sx={{ alignItems: 'center', py: 3 }}>
              <CircularProgress size={28} />
            </Stack>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {members.map((member) => (
                  <TableRow key={member.id}>
                    <TableCell>{member.user.fullName}</TableCell>
                    <TableCell>{member.user.email}</TableCell>
                    <TableCell>{PROJECT_ROLE_LABELS[member.projectRole]}</TableCell>
                    <TableCell align="right">
                      <Tooltip title="Remove member">
                        <span>
                          <IconButton
                            aria-label={`Remove ${member.user.fullName}`}
                            disabled={submitting}
                            onClick={() => void handleRemoveMember(member.user.id)}
                            size="small"
                          >
                            <DeleteOutlinedIcon fontSize="small" />
                          </IconButton>
                        </span>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button disabled={submitting} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  )
}
