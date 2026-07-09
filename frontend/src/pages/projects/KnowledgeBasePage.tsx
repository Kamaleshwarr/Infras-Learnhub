import { useCallback, useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
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
import { Link as RouterLink, useParams, useSearchParams } from 'react-router-dom'
import { projectKnowledgeApi } from '../../api/projectKnowledgeApi'
import { projectsApi } from '../../api/projectsApi'
import { useAuth } from '../../auth/useAuth'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import { KnowledgeBreadcrumbs } from '../../components/project-knowledge/KnowledgeBreadcrumbs'
import { KnowledgeFolderCard } from '../../components/project-knowledge/KnowledgeFolderCard'
import { KnowledgeFolderDialog } from '../../components/project-knowledge/KnowledgeFolderDialog'
import { KnowledgeResourceCard } from '../../components/project-knowledge/KnowledgeResourceCard'
import { KnowledgeResourceDialog } from '../../components/project-knowledge/KnowledgeResourceDialog'
import { KNOWLEDGE_MESSAGES } from '../../components/project-knowledge/knowledgeMessages'
import {
  buildFolderBreadcrumbs,
  canCreateSubfolderAtDepth,
  getFolderDepthFromBreadcrumbs,
  loadAllFoldersForSelect,
} from '../../components/project-knowledge/knowledgeUtils'
import type { ProjectDetail } from '../../types/projects'
import type { ProjectKnowledgeFolder, ProjectKnowledgeItem } from '../../types/projectKnowledge'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

const SEARCH_DEBOUNCE_MS = 300

export function KnowledgeBasePage() {
  const { projectId, folderId } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()
  const { isAdmin } = useAuth()

  const [project, setProject] = useState<ProjectDetail | null>(null)
  const [currentFolder, setCurrentFolder] = useState<ProjectKnowledgeFolder | null>(null)
  const [breadcrumbs, setBreadcrumbs] = useState<Awaited<ReturnType<typeof buildFolderBreadcrumbs>>>([])
  const [folders, setFolders] = useState<ProjectKnowledgeFolder[]>([])
  const [items, setItems] = useState<ProjectKnowledgeItem[]>([])
  const [folderOptions, setFolderOptions] = useState<ProjectKnowledgeFolder[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searchInput, setSearchInput] = useState(searchParams.get('search') ?? '')
  const [appliedSearch, setAppliedSearch] = useState(searchParams.get('search') ?? '')
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)

  const [folderDialogOpen, setFolderDialogOpen] = useState(false)
  const [folderDialogMode, setFolderDialogMode] = useState<'create' | 'edit'>('create')
  const [editingFolder, setEditingFolder] = useState<ProjectKnowledgeFolder | null>(null)
  const [resourceDialogOpen, setResourceDialogOpen] = useState(false)
  const [resourceDialogMode, setResourceDialogMode] = useState<'create' | 'edit'>('create')
  const [editingItem, setEditingItem] = useState<ProjectKnowledgeItem | null>(null)
  const [deleteFolderTarget, setDeleteFolderTarget] = useState<ProjectKnowledgeFolder | null>(null)
  const [deleteItemTarget, setDeleteItemTarget] = useState<ProjectKnowledgeItem | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const canManage =
    isAdmin || project?.currentMemberRole === 'OWNER' || project?.currentMemberRole === 'CONTRIBUTOR'
  const canDeleteFolder = isAdmin || project?.currentMemberRole === 'OWNER'
  const canDeleteResource = canDeleteFolder
  const isSearchMode = appliedSearch.trim().length > 0
  const currentFolderDepth = getFolderDepthFromBreadcrumbs(breadcrumbs)
  const allowSubfolderCreate = canCreateSubfolderAtDepth(currentFolderDepth)

  const loadKnowledge = useCallback(async () => {
    if (!projectId) {
      setNotFound(true)
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)
    setNotFound(false)

    const searchActive = appliedSearch.trim().length > 0

    try {
      const projectResponse = await projectsApi.get(projectId)
      setProject(projectResponse)

      const folderResponse = folderId ? await projectKnowledgeApi.getFolder(projectId, folderId) : null
      setCurrentFolder(folderResponse)
      setBreadcrumbs(await buildFolderBreadcrumbs(projectId, projectResponse.name, folderId))

      const [foldersResponse, itemsResponse, selectableFolders] = await Promise.all([
        projectKnowledgeApi.listFolders(projectId, {
          parentId: folderId,
          search: searchActive ? appliedSearch : undefined,
          size: 100,
          sort: 'name,asc',
        }),
        projectKnowledgeApi.listItems(projectId, {
          folderId: searchActive ? undefined : folderId,
          search: appliedSearch || undefined,
          sourceType: 'LINK',
          size: 100,
          sort: 'title,asc',
        }),
        loadAllFoldersForSelect(projectId),
      ])

      setFolders(foldersResponse.content)
      setItems(itemsResponse.content)
      setFolderOptions(selectableFolders)
    } catch (loadError) {
      if (isNotFoundError(loadError)) {
        setNotFound(true)
      } else {
        setError(resolveApiError(loadError, KNOWLEDGE_MESSAGES.loadError))
      }
    } finally {
      setLoading(false)
    }
  }, [appliedSearch, folderId, projectId])

  useEffect(() => {
    void loadKnowledge()
  }, [loadKnowledge])

  useEffect(() => {
    const handle = window.setTimeout(() => {
      setAppliedSearch(searchInput.trim())
      const next = new URLSearchParams(searchParams)
      if (searchInput.trim()) {
        next.set('search', searchInput.trim())
      } else {
        next.delete('search')
      }
      setSearchParams(next, { replace: true })
    }, SEARCH_DEBOUNCE_MS)
    return () => window.clearTimeout(handle)
  }, [searchInput, searchParams, setSearchParams])

  const pageTitle = useMemo(() => {
    if (isSearchMode) {
      return `Search: ${appliedSearch}`
    }
    return currentFolder?.name ?? KNOWLEDGE_MESSAGES.title
  }, [appliedSearch, currentFolder?.name, isSearchMode])

  async function handleCreateFolder(values: { name: string; description?: string }) {
    if (!projectId) {
      return
    }
    setSubmitting(true)
    try {
      await projectKnowledgeApi.createFolder(projectId, {
        name: values.name,
        description: values.description,
        parentId: folderId ?? null,
      })
      setFolderDialogOpen(false)
      setNotification({ message: KNOWLEDGE_MESSAGES.createFolderSuccess, severity: 'success' })
      await loadKnowledge()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, KNOWLEDGE_MESSAGES.loadError), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleUpdateFolder(values: { name: string; description?: string }) {
    if (!projectId || !editingFolder) {
      return
    }
    setSubmitting(true)
    try {
      await projectKnowledgeApi.updateFolder(projectId, editingFolder.id, {
        name: values.name,
        description: values.description,
        parentId: editingFolder.parentId ?? null,
      })
      setFolderDialogOpen(false)
      setEditingFolder(null)
      setNotification({ message: KNOWLEDGE_MESSAGES.updateFolderSuccess, severity: 'success' })
      await loadKnowledge()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, KNOWLEDGE_MESSAGES.loadError), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeleteFolder() {
    if (!projectId || !deleteFolderTarget) {
      return
    }
    setSubmitting(true)
    try {
      await projectKnowledgeApi.deleteFolder(projectId, deleteFolderTarget.id)
      setDeleteFolderTarget(null)
      setNotification({ message: KNOWLEDGE_MESSAGES.deleteFolderSuccess, severity: 'success' })
      await loadKnowledge()
    } catch (submitError) {
      setNotification({
        message: resolveApiError(submitError, KNOWLEDGE_MESSAGES.folderDeleteBlocked),
        severity: 'error',
      })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleSaveResource(values: {
    title: string
    description?: string
    category: ProjectKnowledgeItem['category']
    externalUrl: string
    folderId?: string | null
  }) {
    if (!projectId) {
      return
    }
    setSubmitting(true)
    try {
      if (resourceDialogMode === 'create') {
        await projectKnowledgeApi.createLink(projectId, values)
        setNotification({ message: KNOWLEDGE_MESSAGES.createResourceSuccess, severity: 'success' })
      } else if (editingItem) {
        await projectKnowledgeApi.updateItem(projectId, editingItem.id, values)
        setNotification({ message: KNOWLEDGE_MESSAGES.updateResourceSuccess, severity: 'success' })
      }
      setResourceDialogOpen(false)
      setEditingItem(null)
      await loadKnowledge()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, KNOWLEDGE_MESSAGES.loadError), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeleteItem() {
    if (!projectId || !deleteItemTarget) {
      return
    }
    setSubmitting(true)
    try {
      await projectKnowledgeApi.deleteItem(projectId, deleteItemTarget.id)
      setDeleteItemTarget(null)
      setNotification({ message: KNOWLEDGE_MESSAGES.deleteResourceSuccess, severity: 'success' })
      await loadKnowledge()
    } catch (submitError) {
      setNotification({ message: resolveApiError(submitError, KNOWLEDGE_MESSAGES.loadError), severity: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleOpenResource(item: ProjectKnowledgeItem) {
    if (!projectId) {
      return
    }
    try {
      if (item.sourceType === 'LINK') {
        const response = await projectKnowledgeApi.accessLink(projectId, item.id)
        window.open(response.externalUrl, '_blank', 'noopener,noreferrer')
        return
      }
      if (item.externalUrl) {
        window.open(item.externalUrl, '_blank', 'noopener,noreferrer')
      }
    } catch (openError) {
      setNotification({ message: resolveApiError(openError, KNOWLEDGE_MESSAGES.loadError), severity: 'error' })
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
    return <Alert severity="warning">{KNOWLEDGE_MESSAGES.notFound}</Alert>
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
          {KNOWLEDGE_MESSAGES.backToProject}
        </Button>
        <KnowledgeBreadcrumbs items={breadcrumbs} />
        <Stack
          direction={{ xs: 'column', md: 'row' }}
          spacing={2}
          sx={{ alignItems: { md: 'center' }, justifyContent: 'space-between' }}
        >
          <Box>
            <Typography variant="h4">{pageTitle}</Typography>
            <Typography color="text.secondary" variant="body1">
              {currentFolder?.description ?? KNOWLEDGE_MESSAGES.description}
            </Typography>
          </Box>
          {canManage ? (
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
              {allowSubfolderCreate ? (
                <Button
                  onClick={() => {
                    setFolderDialogMode('create')
                    setEditingFolder(null)
                    setFolderDialogOpen(true)
                  }}
                  startIcon={<AddIcon />}
                  variant="outlined"
                >
                  {folderId ? KNOWLEDGE_MESSAGES.addSubfolder : KNOWLEDGE_MESSAGES.addFolder}
                </Button>
              ) : null}
              <Button
                onClick={() => {
                  setResourceDialogMode('create')
                  setEditingItem(null)
                  setResourceDialogOpen(true)
                }}
                startIcon={<AddIcon />}
                variant="contained"
              >
                {KNOWLEDGE_MESSAGES.addResource}
              </Button>
            </Stack>
          ) : null}
        </Stack>
        <TextField
          fullWidth
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder={KNOWLEDGE_MESSAGES.searchPlaceholder}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            },
          }}
          value={searchInput}
        />
      </Stack>

      {currentFolder && canManage ? (
        <Stack direction="row" spacing={1}>
          <Button
            onClick={() => {
              setFolderDialogMode('edit')
              setEditingFolder(currentFolder)
              setFolderDialogOpen(true)
            }}
            startIcon={<EditOutlinedIcon />}
            variant="outlined"
          >
            {KNOWLEDGE_MESSAGES.editFolder}
          </Button>
          {canDeleteFolder ? (
            <Button
              color="error"
              onClick={() => setDeleteFolderTarget(currentFolder)}
              startIcon={<DeleteOutlinedIcon />}
              variant="outlined"
            >
              {KNOWLEDGE_MESSAGES.deleteFolder}
            </Button>
          ) : null}
        </Stack>
      ) : null}

      {!allowSubfolderCreate && canManage && !isSearchMode ? (
        <Alert severity="info">{KNOWLEDGE_MESSAGES.depthLimitHint}</Alert>
      ) : null}

      {!isSearchMode && folders.length > 0 ? (
        <Stack spacing={1.5}>
          <Typography variant="h6">{folderId ? KNOWLEDGE_MESSAGES.subfoldersTitle : KNOWLEDGE_MESSAGES.foldersTitle}</Typography>
          <Grid container spacing={2}>
            {folders.map((folder) => (
              <Grid key={folder.id} size={{ xs: 12, sm: 6, lg: 4 }} sx={{ display: 'flex' }}>
                <KnowledgeFolderCard
                  folder={folder}
                  href={`/projects/${projectId}/knowledge/folders/${folder.id}`}
                />
              </Grid>
            ))}
          </Grid>
        </Stack>
      ) : null}

      <Stack spacing={1.5}>
        <Typography variant="h6">{KNOWLEDGE_MESSAGES.resourcesTitle}</Typography>
        {items.length === 0 ? (
          <Alert severity={isSearchMode ? 'info' : 'info'}>
            {isSearchMode ? KNOWLEDGE_MESSAGES.emptySearchDescription : KNOWLEDGE_MESSAGES.emptyFolderDescription}
          </Alert>
        ) : (
          <Grid container spacing={2}>
            {items.map((item) => (
              <Grid key={item.id} size={{ xs: 12, md: 6 }} sx={{ display: 'flex' }}>
                <KnowledgeResourceCard
                  canManage={canManage}
                  item={item}
                  onDelete={canDeleteResource ? (target) => setDeleteItemTarget(target) : undefined}
                  onEdit={
                    canManage
                      ? (target) => {
                          setResourceDialogMode('edit')
                          setEditingItem(target)
                          setResourceDialogOpen(true)
                        }
                      : undefined
                  }
                  onOpen={(target) => void handleOpenResource(target)}
                />
              </Grid>
            ))}
          </Grid>
        )}
      </Stack>

      {!isSearchMode && folders.length === 0 && items.length === 0 ? (
        <Alert severity="info">
          {folderId ? KNOWLEDGE_MESSAGES.emptyFolderDescription : KNOWLEDGE_MESSAGES.emptyFoldersDescription}
        </Alert>
      ) : null}

      <KnowledgeFolderDialog
        initialFolder={editingFolder}
        mode={folderDialogMode}
        onClose={() => {
          setFolderDialogOpen(false)
          setEditingFolder(null)
        }}
        onSubmit={(values) => void (folderDialogMode === 'create' ? handleCreateFolder(values) : handleUpdateFolder(values))}
        open={folderDialogOpen}
        submitting={submitting}
      />

      <KnowledgeResourceDialog
        defaultFolderId={folderId ?? null}
        folders={folderOptions}
        initialItem={editingItem}
        mode={resourceDialogMode}
        onClose={() => {
          setResourceDialogOpen(false)
          setEditingItem(null)
        }}
        onSubmit={(values) => void handleSaveResource(values)}
        open={resourceDialogOpen}
        submitting={submitting}
      />

      <ConfirmDialog
        confirmLabel="Delete"
        message={
          deleteFolderTarget
            ? KNOWLEDGE_MESSAGES.deleteFolderConfirm
            : KNOWLEDGE_MESSAGES.deleteResourceConfirm
        }
        onClose={() => {
          setDeleteFolderTarget(null)
          setDeleteItemTarget(null)
        }}
        onConfirm={() => void (deleteFolderTarget ? handleDeleteFolder() : handleDeleteItem())}
        open={Boolean(deleteFolderTarget || deleteItemTarget)}
        title={deleteFolderTarget ? KNOWLEDGE_MESSAGES.deleteFolder : KNOWLEDGE_MESSAGES.deleteResource}
      />

      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </Stack>
  )
}
