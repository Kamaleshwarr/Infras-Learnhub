import { useCallback, useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LaunchIcon from '@mui/icons-material/Launch'
import SearchIcon from '@mui/icons-material/Search'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { projectEnvironmentsApi } from '../../api/projectEnvironmentsApi'
import { projectsApi } from '../../api/projectsApi'
import { useAuth } from '../../auth/useAuth'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import { EnvironmentDialog } from '../../components/project-operational/EnvironmentDialog'
import {
  buildProjectOperationalBreadcrumbs,
  ProjectOperationalBreadcrumbs,
} from '../../components/project-operational/ProjectOperationalBreadcrumbs'
import { ReferenceDialog } from '../../components/project-operational/ReferenceDialog'
import { OPERATIONAL_MESSAGES } from '../../components/project-operational/operationalMessages'
import type { ProjectDetail } from '../../types/projects'
import type { ProjectEnvironment, ProjectEnvironmentReference } from '../../types/projectOperational'
import { ENVIRONMENT_REFERENCE_TYPE_LABELS } from '../../types/projectOperational'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function ProjectEnvironmentsPage() {
  const { projectId } = useParams()
  const { isAdmin } = useAuth()
  const [project, setProject] = useState<ProjectDetail | null>(null)
  const [environments, setEnvironments] = useState<ProjectEnvironment[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searchInput, setSearchInput] = useState('')
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [environmentDialogOpen, setEnvironmentDialogOpen] = useState(false)
  const [environmentDialogMode, setEnvironmentDialogMode] = useState<'create' | 'edit'>('create')
  const [editingEnvironment, setEditingEnvironment] = useState<ProjectEnvironment | null>(null)
  const [referenceDialogOpen, setReferenceDialogOpen] = useState(false)
  const [referenceDialogMode, setReferenceDialogMode] = useState<'create' | 'edit'>('create')
  const [referenceEnvironmentId, setReferenceEnvironmentId] = useState<string | null>(null)
  const [editingReference, setEditingReference] = useState<ProjectEnvironmentReference | null>(null)
  const [deleteEnvironmentTarget, setDeleteEnvironmentTarget] = useState<ProjectEnvironment | null>(null)
  const [deleteReferenceTarget, setDeleteReferenceTarget] = useState<{
    environmentId: string
    reference: ProjectEnvironmentReference
  } | null>(null)

  const canManage =
    isAdmin || project?.currentMemberRole === 'OWNER' || project?.currentMemberRole === 'CONTRIBUTOR'
  const canDeleteEnvironment = isAdmin || project?.currentMemberRole === 'OWNER'

  const breadcrumbs = useMemo(
    () =>
      project
        ? buildProjectOperationalBreadcrumbs(
            project.id,
            project.name,
            OPERATIONAL_MESSAGES.environmentsTitle,
            `/projects/${project.id}/environments`,
          )
        : [],
    [project],
  )

  const filteredEnvironments = useMemo(() => {
    const term = searchInput.trim().toLowerCase()
    if (!term) return environments
    return environments
      .map((environment) => {
        const envMatch =
          environment.name.toLowerCase().includes(term) ||
          (environment.description?.toLowerCase().includes(term) ?? false)
        const references = environment.references.filter(
          (reference) =>
            reference.name.toLowerCase().includes(term) ||
            ENVIRONMENT_REFERENCE_TYPE_LABELS[reference.referenceType].toLowerCase().includes(term) ||
            reference.url.toLowerCase().includes(term),
        )
        if (envMatch) return environment
        if (references.length > 0) return { ...environment, references }
        return null
      })
      .filter((environment): environment is ProjectEnvironment => environment !== null)
  }, [environments, searchInput])

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
      const [projectResponse, environmentsResponse] = await Promise.all([
        projectsApi.get(projectId),
        projectEnvironmentsApi.list(projectId),
      ])
      setProject(projectResponse)
      setEnvironments(environmentsResponse)
    } catch (loadError) {
      if (isNotFoundError(loadError)) {
        setNotFound(true)
      } else {
        setError(resolveApiError(loadError, 'Unable to load environments.'))
      }
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    void loadData()
  }, [loadData])

  async function handleCreateEnvironment(values: { name: string; description?: string }) {
    if (!projectId) return
    setSubmitting(true)
    try {
      await projectEnvironmentsApi.create(projectId, values)
      setEnvironmentDialogOpen(false)
      setNotification({ message: OPERATIONAL_MESSAGES.createEnvironmentSuccess, severity: 'success' })
      await loadData()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, 'Unable to create environment.'), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleUpdateEnvironment(values: { name: string; description?: string }) {
    if (!projectId || !editingEnvironment) return
    setSubmitting(true)
    try {
      await projectEnvironmentsApi.update(projectId, editingEnvironment.id, {
        ...values,
        displayOrder: editingEnvironment.displayOrder,
        active: editingEnvironment.active,
      })
      setEnvironmentDialogOpen(false)
      setNotification({ message: OPERATIONAL_MESSAGES.updateEnvironmentSuccess, severity: 'success' })
      await loadData()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, 'Unable to update environment.'), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleSaveReference(values: {
    name: string
    referenceType: ProjectEnvironmentReference['referenceType']
    url: string
    description?: string
  }) {
    if (!projectId || !referenceEnvironmentId) return
    setSubmitting(true)
    try {
      if (referenceDialogMode === 'create') {
        await projectEnvironmentsApi.createReference(projectId, referenceEnvironmentId, values)
        setNotification({ message: OPERATIONAL_MESSAGES.createReferenceSuccess, severity: 'success' })
      } else if (editingReference) {
        await projectEnvironmentsApi.updateReference(projectId, referenceEnvironmentId, editingReference.id, {
          ...values,
          displayOrder: editingReference.displayOrder,
          active: editingReference.active,
        })
        setNotification({ message: OPERATIONAL_MESSAGES.updateReferenceSuccess, severity: 'success' })
      }
      setReferenceDialogOpen(false)
      await loadData()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, 'Unable to save reference.'), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (notFound) {
    return <Alert severity="warning">Project not found or you do not have access.</Alert>
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>
  }

  return (
    <Stack spacing={3}>
      <Stack spacing={1.5}>
        <Button
          component={RouterLink}
          startIcon={<ArrowBackIcon />}
          sx={{ alignSelf: 'flex-start' }}
          to={`/projects/${projectId}`}
          variant="text"
        >
          {OPERATIONAL_MESSAGES.backToProject}
        </Button>
        <ProjectOperationalBreadcrumbs items={breadcrumbs} />
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' }, justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="h4">{OPERATIONAL_MESSAGES.environmentsTitle}</Typography>
            <Typography color="text.secondary" variant="body1">
              {OPERATIONAL_MESSAGES.environmentsDescription}
            </Typography>
          </Box>
          {canManage ? (
            <Button
              onClick={() => {
                setEnvironmentDialogMode('create')
                setEditingEnvironment(null)
                setEnvironmentDialogOpen(true)
              }}
              startIcon={<AddIcon />}
              variant="contained"
            >
              {OPERATIONAL_MESSAGES.addEnvironment}
            </Button>
          ) : null}
        </Stack>
        <TextField
          fullWidth
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder={OPERATIONAL_MESSAGES.searchEnvironments}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            },
          }}
          value={searchInput}
        />
      </Stack>

      {filteredEnvironments.length === 0 ? (
        <Alert severity="info">
          {OPERATIONAL_MESSAGES.emptyEnvironments}
          {canManage ? (
            <Box sx={{ mt: 2 }}>
              <Button
                onClick={() => {
                  setEnvironmentDialogMode('create')
                  setEditingEnvironment(null)
                  setEnvironmentDialogOpen(true)
                }}
                startIcon={<AddIcon />}
                variant="outlined"
              >
                {OPERATIONAL_MESSAGES.addEnvironment}
              </Button>
            </Box>
          ) : null}
        </Alert>
      ) : (
        <Stack spacing={1.5}>
          {filteredEnvironments.map((environment) => (
            <Accordion defaultExpanded key={environment.id} variant="outlined">
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', width: '100%' }}>
                  <Typography sx={{ flex: 1 }} variant="h6">
                    {environment.name}
                  </Typography>
                  <Chip label={`${environment.references.length} references`} size="small" variant="outlined" />
                </Stack>
              </AccordionSummary>
              <AccordionDetails>
                <Stack spacing={2}>
                  {environment.description ? (
                    <Typography color="text.secondary" variant="body2">
                      {environment.description}
                    </Typography>
                  ) : null}
                  {canManage ? (
                    <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                      <Button
                        onClick={() => {
                          setReferenceEnvironmentId(environment.id)
                          setReferenceDialogMode('create')
                          setEditingReference(null)
                          setReferenceDialogOpen(true)
                        }}
                        size="small"
                        startIcon={<AddIcon />}
                        variant="outlined"
                      >
                        {OPERATIONAL_MESSAGES.addReference}
                      </Button>
                      <Button
                        onClick={() => {
                          setEnvironmentDialogMode('edit')
                          setEditingEnvironment(environment)
                          setEnvironmentDialogOpen(true)
                        }}
                        size="small"
                        startIcon={<EditOutlinedIcon />}
                        variant="outlined"
                      >
                        {OPERATIONAL_MESSAGES.editEnvironment}
                      </Button>
                      {canDeleteEnvironment ? (
                        <Button
                          color="error"
                          onClick={() => setDeleteEnvironmentTarget(environment)}
                          size="small"
                          startIcon={<DeleteOutlinedIcon />}
                          variant="outlined"
                        >
                          {OPERATIONAL_MESSAGES.deleteEnvironment}
                        </Button>
                      ) : null}
                    </Stack>
                  ) : null}
                  {environment.references.length === 0 ? (
                    <Typography color="text.secondary" variant="body2">
                      No references configured.
                    </Typography>
                  ) : (
                    <Stack spacing={1}>
                      {environment.references.map((reference) => (
                        <Stack
                          direction={{ xs: 'column', sm: 'row' }}
                          key={reference.id}
                          spacing={1}
                          sx={{
                            alignItems: { xs: 'stretch', sm: 'flex-start' },
                            border: 1,
                            borderColor: 'divider',
                            borderRadius: 1,
                            p: 1.5,
                          }}
                        >
                          <Box sx={{ flex: 1, minWidth: 0 }}>
                            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
                              <Typography sx={{ wordBreak: 'break-word' }} variant="subtitle2">
                                {reference.name}
                              </Typography>
                              <Chip
                                label={ENVIRONMENT_REFERENCE_TYPE_LABELS[reference.referenceType]}
                                size="small"
                                variant="outlined"
                              />
                            </Stack>
                            {reference.description?.trim() ? (
                              <Typography
                                color="text.secondary"
                                sx={{
                                  display: '-webkit-box',
                                  mt: 0.5,
                                  overflow: 'hidden',
                                  overflowWrap: 'anywhere',
                                  WebkitBoxOrient: 'vertical',
                                  WebkitLineClamp: 2,
                                  wordBreak: 'break-word',
                                }}
                                variant="body2"
                              >
                                {reference.description.trim()}
                              </Typography>
                            ) : null}
                          </Box>
                          <Stack
                            direction="row"
                            spacing={0.5}
                            sx={{ alignSelf: { sm: 'center' }, flexShrink: 0, flexWrap: 'wrap' }}
                          >
                            <Button
                              endIcon={<LaunchIcon />}
                              href={reference.url}
                              rel="noopener noreferrer"
                              size="small"
                              target="_blank"
                              variant="outlined"
                            >
                              {OPERATIONAL_MESSAGES.openExternal}
                            </Button>
                            {canManage ? (
                              <>
                                <IconButton
                                  aria-label="Edit reference"
                                  onClick={() => {
                                    setReferenceEnvironmentId(environment.id)
                                    setReferenceDialogMode('edit')
                                    setEditingReference(reference)
                                    setReferenceDialogOpen(true)
                                  }}
                                  size="small"
                                >
                                  <EditOutlinedIcon fontSize="small" />
                                </IconButton>
                                <IconButton
                                  aria-label="Delete reference"
                                  color="error"
                                  onClick={() =>
                                    setDeleteReferenceTarget({ environmentId: environment.id, reference })
                                  }
                                  size="small"
                                >
                                  <DeleteOutlinedIcon fontSize="small" />
                                </IconButton>
                              </>
                            ) : null}
                          </Stack>
                        </Stack>
                      ))}
                    </Stack>
                  )}
                </Stack>
              </AccordionDetails>
            </Accordion>
          ))}
        </Stack>
      )}

      <EnvironmentDialog
        initial={editingEnvironment}
        mode={environmentDialogMode}
        onClose={() => setEnvironmentDialogOpen(false)}
        onSubmit={environmentDialogMode === 'create' ? handleCreateEnvironment : handleUpdateEnvironment}
        open={environmentDialogOpen}
        submitting={submitting}
      />
      <ReferenceDialog
        initial={editingReference}
        mode={referenceDialogMode}
        onClose={() => setReferenceDialogOpen(false)}
        onSubmit={handleSaveReference}
        open={referenceDialogOpen}
        submitting={submitting}
      />
      <ConfirmDialog
        confirmLabel="Delete"
        message={OPERATIONAL_MESSAGES.deleteEnvironmentConfirm}
        onClose={() => setDeleteEnvironmentTarget(null)}
        onConfirm={async () => {
          if (!projectId || !deleteEnvironmentTarget) return
          try {
            await projectEnvironmentsApi.remove(projectId, deleteEnvironmentTarget.id)
            setNotification({ message: OPERATIONAL_MESSAGES.deleteEnvironmentSuccess, severity: 'success' })
            await loadData()
          } catch (deleteError) {
            setNotification({ message: resolveApiError(deleteError, 'Unable to delete environment.'), severity: 'error' })
          } finally {
            setDeleteEnvironmentTarget(null)
          }
        }}
        open={Boolean(deleteEnvironmentTarget)}
        title={OPERATIONAL_MESSAGES.deleteEnvironment}
      />
      <ConfirmDialog
        confirmLabel="Remove"
        message={OPERATIONAL_MESSAGES.deleteReferenceConfirm}
        onClose={() => setDeleteReferenceTarget(null)}
        onConfirm={async () => {
          if (!projectId || !deleteReferenceTarget) return
          try {
            await projectEnvironmentsApi.removeReference(
              projectId,
              deleteReferenceTarget.environmentId,
              deleteReferenceTarget.reference.id,
            )
            setNotification({ message: OPERATIONAL_MESSAGES.deleteReferenceSuccess, severity: 'success' })
            await loadData()
          } catch (deleteError) {
            setNotification({ message: resolveApiError(deleteError, 'Unable to remove reference.'), severity: 'error' })
          } finally {
            setDeleteReferenceTarget(null)
          }
        }}
        open={Boolean(deleteReferenceTarget)}
        title={OPERATIONAL_MESSAGES.deleteReference}
      />
      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </Stack>
  )
}
