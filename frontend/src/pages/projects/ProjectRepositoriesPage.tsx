import { useCallback, useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { RepositoryCard } from '../../components/project-operational/RepositoryCard'
import SearchIcon from '@mui/icons-material/Search'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Grid,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { projectRepositoriesApi } from '../../api/projectRepositoriesApi'
import { projectsApi } from '../../api/projectsApi'
import { useAuth } from '../../auth/useAuth'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import {
  buildProjectOperationalBreadcrumbs,
  ProjectOperationalBreadcrumbs,
} from '../../components/project-operational/ProjectOperationalBreadcrumbs'
import { RepositoryDialog } from '../../components/project-operational/RepositoryDialog'
import { OPERATIONAL_MESSAGES } from '../../components/project-operational/operationalMessages'
import type { ProjectDetail } from '../../types/projects'
import type { ProjectLinkedRepository } from '../../types/projectOperational'
import { REPOSITORY_PROVIDER_LABELS, REPOSITORY_TYPE_LABELS } from '../../types/projectOperational'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function ProjectRepositoriesPage() {
  const { projectId } = useParams()
  const { isAdmin } = useAuth()
  const [project, setProject] = useState<ProjectDetail | null>(null)
  const [repositories, setRepositories] = useState<ProjectLinkedRepository[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searchInput, setSearchInput] = useState('')
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingRepository, setEditingRepository] = useState<ProjectLinkedRepository | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<ProjectLinkedRepository | null>(null)

  const canManage =
    isAdmin || project?.currentMemberRole === 'OWNER' || project?.currentMemberRole === 'CONTRIBUTOR'
  const canDelete = isAdmin || project?.currentMemberRole === 'OWNER'

  const breadcrumbs = useMemo(
    () =>
      project
        ? buildProjectOperationalBreadcrumbs(
            project.id,
            project.name,
            OPERATIONAL_MESSAGES.repositoriesTitle,
            `/projects/${project.id}/repositories`,
          )
        : [],
    [project],
  )

  const filteredRepositories = useMemo(() => {
    const term = searchInput.trim().toLowerCase()
    if (!term) return repositories
    return repositories.filter(
      (repository) =>
        repository.name.toLowerCase().includes(term) ||
        REPOSITORY_TYPE_LABELS[repository.repositoryType].toLowerCase().includes(term) ||
        REPOSITORY_PROVIDER_LABELS[repository.provider].toLowerCase().includes(term) ||
        (repository.defaultBranch?.toLowerCase().includes(term) ?? false) ||
        (repository.description?.toLowerCase().includes(term) ?? false),
    )
  }, [repositories, searchInput])

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
      const [projectResponse, repositoriesResponse] = await Promise.all([
        projectsApi.get(projectId),
        projectRepositoriesApi.list(projectId),
      ])
      setProject(projectResponse)
      setRepositories(repositoriesResponse)
    } catch (loadError) {
      if (isNotFoundError(loadError)) {
        setNotFound(true)
      } else {
        setError(resolveApiError(loadError, 'Unable to load repositories.'))
      }
    } finally {
      setLoading(false)
    }
  }, [projectId])

  useEffect(() => {
    void loadData()
  }, [loadData])

  async function handleSave(values: {
    name: string
    description?: string
    repositoryType: ProjectLinkedRepository['repositoryType']
    provider: ProjectLinkedRepository['provider']
    repositoryUrl: string
    defaultBranch?: string
  }) {
    if (!projectId) return
    setSubmitting(true)
    try {
      if (dialogMode === 'create') {
        await projectRepositoriesApi.create(projectId, values)
        setNotification({ message: OPERATIONAL_MESSAGES.createRepositorySuccess, severity: 'success' })
      } else if (editingRepository) {
        await projectRepositoriesApi.update(projectId, editingRepository.id, {
          ...values,
          displayOrder: editingRepository.displayOrder,
          active: editingRepository.active,
        })
        setNotification({ message: OPERATIONAL_MESSAGES.updateRepositorySuccess, severity: 'success' })
      }
      setDialogOpen(false)
      await loadData()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, 'Unable to save repository.'), severity: 'error' })
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
            <Typography variant="h4">{OPERATIONAL_MESSAGES.repositoriesTitle}</Typography>
            <Typography color="text.secondary" variant="body1">
              {OPERATIONAL_MESSAGES.repositoriesDescription}
            </Typography>
          </Box>
          {canManage ? (
            <Button
              onClick={() => {
                setDialogMode('create')
                setEditingRepository(null)
                setDialogOpen(true)
              }}
              startIcon={<AddIcon />}
              variant="contained"
            >
              {OPERATIONAL_MESSAGES.addRepository}
            </Button>
          ) : null}
        </Stack>
        <TextField
          fullWidth
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder={OPERATIONAL_MESSAGES.searchRepositories}
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

      {filteredRepositories.length === 0 ? (
        <Alert severity="info">
          {OPERATIONAL_MESSAGES.emptyRepositories}
          {canManage ? (
            <Box sx={{ mt: 2 }}>
              <Button
                onClick={() => {
                  setDialogMode('create')
                  setEditingRepository(null)
                  setDialogOpen(true)
                }}
                startIcon={<AddIcon />}
                variant="outlined"
              >
                {OPERATIONAL_MESSAGES.addRepository}
              </Button>
            </Box>
          ) : null}
        </Alert>
      ) : (
        <Grid container spacing={2}>
          {filteredRepositories.map((repository) => (
            <Grid key={repository.id} size={{ xs: 12, md: 6, lg: 4 }} sx={{ display: 'flex' }}>
              <RepositoryCard
                canDelete={canDelete}
                canManage={canManage}
                onDelete={setDeleteTarget}
                onEdit={(target) => {
                  setDialogMode('edit')
                  setEditingRepository(target)
                  setDialogOpen(true)
                }}
                repository={repository}
              />
            </Grid>
          ))}
        </Grid>
      )}

      <RepositoryDialog
        initial={editingRepository}
        mode={dialogMode}
        onClose={() => setDialogOpen(false)}
        onSubmit={handleSave}
        open={dialogOpen}
        submitting={submitting}
      />
      <ConfirmDialog
        confirmLabel="Delete"
        message={OPERATIONAL_MESSAGES.deleteRepositoryConfirm}
        onClose={() => setDeleteTarget(null)}
        onConfirm={async () => {
          if (!projectId || !deleteTarget) return
          try {
            await projectRepositoriesApi.remove(projectId, deleteTarget.id)
            setNotification({ message: OPERATIONAL_MESSAGES.deleteRepositorySuccess, severity: 'success' })
            await loadData()
          } catch (deleteError) {
            setNotification({ message: resolveApiError(deleteError, 'Unable to delete repository.'), severity: 'error' })
          } finally {
            setDeleteTarget(null)
          }
        }}
        open={Boolean(deleteTarget)}
        title={OPERATIONAL_MESSAGES.deleteRepository}
      />
      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </Stack>
  )
}
