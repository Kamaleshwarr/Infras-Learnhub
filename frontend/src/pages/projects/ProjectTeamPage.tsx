import { useCallback, useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { projectsApi } from '../../api/projectsApi'
import { projectTeamApi } from '../../api/projectTeamApi'
import { useAuth } from '../../auth/useAuth'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import { ExternalContactDialog } from '../../components/project-team/ExternalContactDialog'
import { ExternalContactRow } from '../../components/project-team/ExternalContactRow'
import { TeamMemberDialog } from '../../components/project-team/TeamMemberDialog'
import { TeamMemberRow } from '../../components/project-team/TeamMemberRow'
import { TEAM_MESSAGES } from '../../components/project-team/teamMessages'
import {
  buildProjectOperationalBreadcrumbs,
  ProjectOperationalBreadcrumbs,
} from '../../components/project-operational/ProjectOperationalBreadcrumbs'
import type { ProjectDetail, ProjectMember } from '../../types/projects'
import type { ProjectExternalContact } from '../../types/projectTeam'
import { PROJECT_FUNCTIONAL_ROLE_LABELS } from '../../types/projectTeam'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function ProjectTeamPage() {
  const { projectId } = useParams()
  const { isAdmin } = useAuth()
  const [project, setProject] = useState<ProjectDetail | null>(null)
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [externalContacts, setExternalContacts] = useState<ProjectExternalContact[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)
  const [memberDialogOpen, setMemberDialogOpen] = useState(false)
  const [editingMember, setEditingMember] = useState<ProjectMember | null>(null)
  const [externalDialogOpen, setExternalDialogOpen] = useState(false)
  const [editingExternalContact, setEditingExternalContact] = useState<ProjectExternalContact | null>(null)
  const [deleteMemberTarget, setDeleteMemberTarget] = useState<ProjectMember | null>(null)
  const [deleteExternalTarget, setDeleteExternalTarget] = useState<ProjectExternalContact | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const canManage = isAdmin || project?.currentMemberRole === 'OWNER'

  const breadcrumbs = useMemo(
    () =>
      project
        ? buildProjectOperationalBreadcrumbs(
            project.id,
            project.name,
            TEAM_MESSAGES.pageTitle,
            `/projects/${project.id}/team`,
          )
        : [],
    [project],
  )

  const primaryMembers = useMemo(() => members.filter((member) => member.primaryContact), [members])
  const primaryExternalContacts = useMemo(
    () => externalContacts.filter((contact) => contact.primaryContact),
    [externalContacts],
  )
  const primaryContactCount = primaryMembers.length + primaryExternalContacts.length
  const functionalGroups = useMemo(
    () => new Set(members.map((member) => member.functionalRole)).size,
    [members],
  )

  const groupedMembers = useMemo(() => {
    const groups = new Map<string, ProjectMember[]>()
    for (const member of members) {
      const key = member.functionalRole
      const existing = groups.get(key) ?? []
      existing.push(member)
      groups.set(key, existing)
    }
    return Array.from(groups.entries()).sort(([left], [right]) =>
      PROJECT_FUNCTIONAL_ROLE_LABELS[left as keyof typeof PROJECT_FUNCTIONAL_ROLE_LABELS].localeCompare(
        PROJECT_FUNCTIONAL_ROLE_LABELS[right as keyof typeof PROJECT_FUNCTIONAL_ROLE_LABELS],
      ),
    )
  }, [members])

  const loadData = useCallback(async () => {
    if (!projectId) {
      setNotFound(true)
      setLoading(false)
      return
    }
    setLoading(true)
    setError(null)
    setNotFound(false)
    try {
      const [projectResponse, membersResponse, contactsResponse] = await Promise.all([
        projectsApi.get(projectId),
        projectsApi.listMembers(projectId),
        projectTeamApi.listExternalContacts(projectId).catch(() => []),
      ])
      setProject(projectResponse)
      setMembers(membersResponse)
      setExternalContacts(contactsResponse)
    } catch (loadError) {
      if (isNotFoundError(loadError)) {
        setNotFound(true)
        setProject(null)
      } else {
        setError(resolveApiError(loadError, TEAM_MESSAGES.loadError))
      }
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    void loadData()
  }, [loadData])

  async function handleRemoveMember() {
    if (!projectId || !deleteMemberTarget) {
      return
    }
    setSubmitting(true)
    try {
      await projectsApi.removeMember(projectId, deleteMemberTarget.user.id)
      setDeleteMemberTarget(null)
      setNotification({ message: TEAM_MESSAGES.memberRemoved, severity: 'success' })
      await loadData()
    } catch (removeError) {
      setNotification({
        message: resolveApiError(removeError, 'Unable to remove team member.'),
        severity: 'error',
      })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRemoveExternalContact() {
    if (!projectId || !deleteExternalTarget) {
      return
    }
    setSubmitting(true)
    try {
      await projectTeamApi.deleteExternalContact(projectId, deleteExternalTarget.id)
      setDeleteExternalTarget(null)
      setNotification({ message: TEAM_MESSAGES.externalContactRemoved, severity: 'success' })
      await loadData()
    } catch (removeError) {
      setNotification({
        message: resolveApiError(removeError, 'Unable to remove external contact.'),
        severity: 'error',
      })
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <Stack sx={{ alignItems: 'center', py: 8 }}>
        <CircularProgress />
      </Stack>
    )
  }

  if (notFound) {
    return <Alert severity="warning">Project not found or you do not have access.</Alert>
  }

  if (error || !project) {
    return <Alert severity="error">{error ?? TEAM_MESSAGES.loadError}</Alert>
  }

  return (
    <>
      <Stack spacing={3} sx={{ minWidth: 0, width: '100%' }}>
        <Button component={RouterLink} startIcon={<ArrowBackIcon />} sx={{ alignSelf: 'flex-start' }} to={`/projects/${project.id}`} variant="text">
          Back to project
        </Button>

        <Stack spacing={1}>
          <ProjectOperationalBreadcrumbs items={breadcrumbs} />
          <Typography variant="h4">{TEAM_MESSAGES.pageTitle}</Typography>
          <Typography color="text.secondary" variant="body2">
            {TEAM_MESSAGES.pageDescription}
          </Typography>
        </Stack>

        <Card sx={{ width: '100%' }} variant="outlined">
          <CardContent>
            <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
              <Chip label={`${members.length} ${TEAM_MESSAGES.summaryMembers}`} variant="outlined" />
              <Chip label={`${primaryContactCount} ${TEAM_MESSAGES.summaryPrimaryContacts}`} variant="outlined" />
              <Chip label={`${functionalGroups} ${TEAM_MESSAGES.summaryFunctionalGroups}`} variant="outlined" />
            </Stack>
          </CardContent>
        </Card>

        <Card sx={{ width: '100%' }} variant="outlined">
          <CardContent>
            <Stack spacing={1.5}>
              <Typography variant="h6">{TEAM_MESSAGES.primaryContactsTitle}</Typography>
              {primaryMembers.length === 0 && primaryExternalContacts.length === 0 ? (
                <Alert severity="info">{TEAM_MESSAGES.primaryContactsEmpty}</Alert>
              ) : (
                <Box sx={{ minWidth: 0, width: '100%' }}>
                  {primaryMembers.map((member) => (
                    <TeamMemberRow
                      canManage={canManage}
                      key={member.id}
                      member={member}
                      onEdit={(target) => {
                        setEditingMember(target)
                        setMemberDialogOpen(true)
                      }}
                      onRemove={setDeleteMemberTarget}
                    />
                  ))}
                  {primaryExternalContacts.map((contact) => (
                    <ExternalContactRow
                      canManage={canManage}
                      contact={contact}
                      key={contact.id}
                      onEdit={(target) => {
                        setEditingExternalContact(target)
                        setExternalDialogOpen(true)
                      }}
                      onRemove={setDeleteExternalTarget}
                    />
                  ))}
                </Box>
              )}
            </Stack>
          </CardContent>
        </Card>

        <Card sx={{ width: '100%' }} variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1}
                sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
              >
                <Typography variant="h6">{TEAM_MESSAGES.projectTeamTitle}</Typography>
                {canManage ? (
                  <Button
                    onClick={() => {
                      setEditingMember(null)
                      setMemberDialogOpen(true)
                    }}
                    startIcon={<AddIcon />}
                    variant="outlined"
                  >
                    {TEAM_MESSAGES.addMember}
                  </Button>
                ) : null}
              </Stack>
              {members.length === 0 ? (
                <Alert severity="info">{TEAM_MESSAGES.emptyTeamDescription}</Alert>
              ) : (
                groupedMembers.map(([role, roleMembers]) => (
                  <Stack key={role} spacing={1}>
                    <Typography color="text.secondary" variant="overline">
                      {PROJECT_FUNCTIONAL_ROLE_LABELS[role as keyof typeof PROJECT_FUNCTIONAL_ROLE_LABELS]}
                    </Typography>
                    <Box sx={{ minWidth: 0, width: '100%' }}>
                      {roleMembers.map((member) => (
                        <TeamMemberRow
                          canManage={canManage}
                          key={member.id}
                          member={member}
                          onEdit={(target) => {
                            setEditingMember(target)
                            setMemberDialogOpen(true)
                          }}
                          onRemove={setDeleteMemberTarget}
                        />
                      ))}
                    </Box>
                  </Stack>
                ))
              )}
            </Stack>
          </CardContent>
        </Card>

        <Card sx={{ width: '100%' }} variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1}
                sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
              >
                <Typography variant="h6">{TEAM_MESSAGES.externalContactsTitle}</Typography>
                {canManage ? (
                  <Button
                    onClick={() => {
                      setEditingExternalContact(null)
                      setExternalDialogOpen(true)
                    }}
                    startIcon={<AddIcon />}
                    variant="outlined"
                  >
                    {TEAM_MESSAGES.addExternalContact}
                  </Button>
                ) : null}
              </Stack>
              {externalContacts.length === 0 ? (
                <Alert severity="info">{TEAM_MESSAGES.externalContactsEmpty}</Alert>
              ) : (
                <Box sx={{ minWidth: 0, width: '100%' }}>
                  {externalContacts.map((contact) => (
                    <ExternalContactRow
                      canManage={canManage}
                      contact={contact}
                      key={contact.id}
                      onEdit={(target) => {
                        setEditingExternalContact(target)
                        setExternalDialogOpen(true)
                      }}
                      onRemove={setDeleteExternalTarget}
                    />
                  ))}
                </Box>
              )}
            </Stack>
          </CardContent>
        </Card>
      </Stack>

      <TeamMemberDialog
        member={editingMember}
        onClose={() => {
          setMemberDialogOpen(false)
          setEditingMember(null)
        }}
        onSuccess={() => {
          setNotification({ message: TEAM_MESSAGES.memberSaved, severity: 'success' })
          void loadData()
        }}
        open={memberDialogOpen}
        projectId={project.id}
      />
      <ExternalContactDialog
        contact={editingExternalContact}
        onClose={() => {
          setExternalDialogOpen(false)
          setEditingExternalContact(null)
        }}
        onSuccess={() => {
          setNotification({ message: TEAM_MESSAGES.externalContactSaved, severity: 'success' })
          void loadData()
        }}
        open={externalDialogOpen}
        projectId={project.id}
      />
      <ConfirmDialog
        confirmLabel="Remove"
        submitting={submitting}
        message={
          deleteMemberTarget
            ? `Remove ${deleteMemberTarget.user.fullName} from this project?`
            : deleteExternalTarget
              ? `Remove ${deleteExternalTarget.name}?`
              : ''
        }
        onClose={() => {
          setDeleteMemberTarget(null)
          setDeleteExternalTarget(null)
        }}
        onConfirm={() => {
          if (deleteMemberTarget) {
            void handleRemoveMember()
            return
          }
          void handleRemoveExternalContact()
        }}
        open={Boolean(deleteMemberTarget || deleteExternalTarget)}
        title={deleteMemberTarget ? TEAM_MESSAGES.removeMember : TEAM_MESSAGES.removeExternalContact}
      />
      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}
