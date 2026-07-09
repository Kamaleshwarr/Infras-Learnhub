import { useCallback, useEffect, useState } from 'react'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  Stack,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { projectsApi } from '../../api/projectsApi'
import { useAuth } from '../../auth/useAuth'
import { WrappingText } from '../../components/common/WrappingText'
import { RelatedTechnologiesCard } from '../../components/learn/RelatedTechnologiesCard'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import { EditProjectDialog } from '../../components/projects/EditProjectDialog'
import { ProjectAccessChip } from '../../components/projects/ProjectAccessChip'
import { ProjectAreasPanel } from '../../components/projects/ProjectAreasPanel'
import { PROJECT_MESSAGES } from '../../components/projects/projectMessages'
import { ProjectStatusChip } from '../../components/projects/ProjectStatusChip'
import type { ProjectDetail, ProjectMember } from '../../types/projects'
import { PROJECT_ROLE_LABELS } from '../../types/projects'
import { PROJECT_FUNCTIONAL_ROLE_LABELS } from '../../types/projectTeam'
import { TEAM_MESSAGES } from '../../components/project-team/teamMessages'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

function formatDate(value?: string) {
  if (!value) {
    return '—'
  }
  return new Date(value).toLocaleString()
}

export function ProjectDetailPage() {
  const { projectId } = useParams()
  const { isAdmin, user } = useAuth()
  const [project, setProject] = useState<ProjectDetail | null>(null)
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editOpen, setEditOpen] = useState(false)
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)

  const canEditMetadata = isAdmin || project?.currentMemberRole === 'OWNER'

  const loadProject = useCallback(async () => {
    if (!projectId) {
      setNotFound(true)
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)
    setNotFound(false)

    try {
      const [projectResponse, membersResponse] = await Promise.all([
        projectsApi.get(projectId),
        projectsApi.listMembers(projectId).catch(() => []),
      ])
      setProject(projectResponse)
      setMembers(membersResponse)
    } catch (loadError) {
      if (isNotFoundError(loadError)) {
        setNotFound(true)
        setProject(null)
      } else {
        setError(resolveApiError(loadError, PROJECT_MESSAGES.detailLoadError))
      }
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    void loadProject()
  }, [loadProject])

  if (loading) {
    return (
      <Stack sx={{ alignItems: 'center', py: 8 }}>
        <CircularProgress />
      </Stack>
    )
  }

  if (notFound) {
    return <Alert severity="warning">{PROJECT_MESSAGES.notFound}</Alert>
  }

  if (error || !project) {
    return <Alert severity="error">{error ?? PROJECT_MESSAGES.detailLoadError}</Alert>
  }

  return (
    <>
      <Stack spacing={3}>
        <Button component={RouterLink} startIcon={<ArrowBackIcon />} sx={{ alignSelf: 'flex-start' }} to="/projects" variant="text">
          Back to Projects
        </Button>

        <Card variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Stack
                direction={{ xs: 'column', md: 'row' }}
                spacing={2}
                sx={{ alignItems: { md: 'flex-start' }, justifyContent: 'space-between' }}
              >
                <Stack spacing={1.5} sx={{ flex: 1, minWidth: 0 }}>
                  <Typography variant="h4">{project.name}</Typography>
                  <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                    <ProjectStatusChip status={project.status} />
                    <ProjectAccessChip accessType={project.accessType} />
                    {project.currentMemberRole ? (
                      <Chip label={`${PROJECT_MESSAGES.yourRole}: ${PROJECT_ROLE_LABELS[project.currentMemberRole]}`} size="small" variant="outlined" />
                    ) : null}
                  </Stack>
                  {project.description ? (
                    <Typography color="text.secondary" component="div" variant="body1">
                      <WrappingText>{project.description}</WrappingText>
                    </Typography>
                  ) : null}
                </Stack>
                {canEditMetadata ? (
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                    <Button onClick={() => setEditOpen(true)} startIcon={<EditOutlinedIcon />} variant="outlined">
                      {PROJECT_MESSAGES.editProject}
                    </Button>
                  </Stack>
                ) : null}
              </Stack>
              <Divider />
              <Box
                sx={{
                  display: 'grid',
                  gap: 2,
                  gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                }}
              >
                <MetadataItem label={PROJECT_MESSAGES.owner} value={project.owner?.fullName ?? 'Not assigned'} />
                <MetadataItem label={PROJECT_MESSAGES.members} value={String(project.memberCount ?? members.length)} />
                <MetadataItem label={PROJECT_MESSAGES.createdBy} value={project.createdBy.fullName} />
                <MetadataItem label={PROJECT_MESSAGES.lastUpdated} value={formatDate(project.updatedAtUtc)} />
              </Box>
            </Stack>
          </CardContent>
        </Card>

        <Box
          sx={{
            display: 'grid',
            gap: 3,
            gridTemplateColumns: { xs: '1fr', lg: '2fr 1fr' },
          }}
        >
          <Stack spacing={3}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={1.5}>
                  <Typography variant="h6">{PROJECT_MESSAGES.purposeTitle}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    {project.description ?? 'This project does not have a description yet.'}
                  </Typography>
                </Stack>
              </CardContent>
            </Card>
            <RelatedTechnologiesCard technologies={project.relatedTechnologies ?? []} />
            <ProjectAreasPanel
              environmentCount={project.environmentCount}
              memberCount={project.memberCount ?? members.length}
              primaryContactCount={project.primaryContactCount ?? members.filter((member) => member.primaryContact).length}
              projectId={project.id}
              repositoryCount={project.repositoryCount}
            />
          </Stack>

          <Stack spacing={3}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={1.5}>
                  <Typography variant="h6">{PROJECT_MESSAGES.teamSummaryTitle}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    {PROJECT_MESSAGES.teamSummaryDescription}
                  </Typography>
                  {members.length === 0 ? (
                    <Alert severity="info">{PROJECT_MESSAGES.teamSummaryEmpty}</Alert>
                  ) : (
                    <Stack spacing={1}>
                      <Typography color="text.secondary" variant="body2">
                        {members.length} team member{members.length === 1 ? '' : 's'}
                        {(project.primaryContactCount ?? members.filter((member) => member.primaryContact).length) > 0
                          ? ` · ${project.primaryContactCount ?? members.filter((member) => member.primaryContact).length} primary contact${
                              (project.primaryContactCount ?? members.filter((member) => member.primaryContact).length) === 1 ? '' : 's'
                            }`
                          : ''}
                      </Typography>
                      <Stack divider={<Divider flexItem />} spacing={1}>
                        {members
                          .filter((member) => member.primaryContact)
                          .slice(0, 3)
                          .concat(
                            members.filter((member) => !member.primaryContact).slice(0, Math.max(0, 3 - members.filter((member) => member.primaryContact).length)),
                          )
                          .slice(0, 3)
                          .map((member) => (
                            <Stack key={member.id} spacing={0.25}>
                              <Typography variant="body2">{member.user.fullName}</Typography>
                              <Typography color="text.secondary" variant="caption">
                                {PROJECT_FUNCTIONAL_ROLE_LABELS[member.functionalRole]} · {PROJECT_ROLE_LABELS[member.projectRole]}
                              </Typography>
                            </Stack>
                          ))}
                      </Stack>
                    </Stack>
                  )}
                  <Button
                    component={RouterLink}
                    endIcon={<GroupsOutlinedIcon />}
                    sx={{ alignSelf: 'flex-start' }}
                    to={`/projects/${project.id}/team`}
                    variant="text"
                  >
                    {TEAM_MESSAGES.viewTeamContacts}
                  </Button>
                </Stack>
              </CardContent>
            </Card>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={1}>
                  <Typography variant="h6">{PROJECT_MESSAGES.metadataTitle}</Typography>
                  <MetadataItem label="Project ID" value={project.id} />
                  <MetadataItem label={PROJECT_MESSAGES.visibility} value={project.accessType} />
                  <MetadataItem label={PROJECT_MESSAGES.status} value={project.status} />
                  {user ? <MetadataItem label="Signed in as" value={user.fullName} /> : null}
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </Box>
      </Stack>

      <EditProjectDialog
        onClose={() => setEditOpen(false)}
        onSuccess={() => {
          setEditOpen(false)
          setNotification({ message: PROJECT_MESSAGES.updateSuccess, severity: 'success' })
          void loadProject()
        }}
        open={editOpen}
        project={project}
      />
      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}

function MetadataItem({ label, value }: { label: string; value: string }) {
  return (
    <Stack spacing={0.25}>
      <Typography color="text.secondary" variant="caption">
        {label}
      </Typography>
      <Typography variant="body2">{value}</Typography>
    </Stack>
  )
}
