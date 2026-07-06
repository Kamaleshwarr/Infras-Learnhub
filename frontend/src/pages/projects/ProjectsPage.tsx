import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Skeleton, Stack, useMediaQuery, useTheme } from '@mui/material'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { projectsApi } from '../../api/projectsApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { CreateProjectDialog } from '../../components/projects/CreateProjectDialog'
import { ProjectListToolbar } from '../../components/projects/ProjectListToolbar'
import { ProjectCardGrid } from '../../components/projects/ProjectCard'
import { PROJECT_MESSAGES } from '../../components/projects/projectMessages'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import type { PageResponse } from '../../types/api'
import type { ProjectSummary } from '../../types/projects'
import { DEFAULT_PROJECT_LIST_QUERY } from '../../types/projects'
import { resolveApiError } from '../../utils/apiErrors'
import {
  buildProjectListSearchParams,
  parseProjectListQuery,
  toProjectApiParams,
} from './projectListParams'
import { ProjectListFilters } from '../../components/projects/ProjectListFilters'
import { ProjectSearchBar } from '../../components/projects/ProjectSearchBar'

const SEARCH_DEBOUNCE_MS = 300

const EMPTY_PAGE: PageResponse<ProjectSummary> = {
  content: [],
  page: 0,
  size: DEFAULT_PROJECT_LIST_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [],
}

export function ProjectsPage() {
  const { isAdmin } = useAuth()
  const navigate = useNavigate()
  const theme = useTheme()
  useMediaQuery(theme.breakpoints.down('sm'))
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseProjectListQuery(searchParams), [searchParams])
  const [draftSearch, setDraftSearch] = useState(appliedQuery.search)
  const [pageData, setPageData] = useState<PageResponse<ProjectSummary>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)
  const [createOpen, setCreateOpen] = useState(false)
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)

  const updateQuery = useCallback(
    (nextQuery: typeof appliedQuery) => {
      setSearchParams(buildProjectListSearchParams(nextQuery), { replace: true })
    },
    [setSearchParams],
  )

  useEffect(() => {
    setDraftSearch(appliedQuery.search)
  }, [appliedQuery.search])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (draftSearch !== appliedQuery.search) {
        updateQuery({ ...appliedQuery, page: 0, search: draftSearch })
      }
    }, SEARCH_DEBOUNCE_MS)

    return () => window.clearTimeout(timer)
  }, [appliedQuery, draftSearch, updateQuery])

  useEffect(() => {
    let mounted = true

    async function loadProjects() {
      setLoading(true)
      setError(null)
      try {
        const response = await projectsApi.list(toProjectApiParams(appliedQuery))
        if (mounted) {
          setPageData(response)
        }
      } catch (loadError) {
        if (mounted) {
          setError(resolveApiError(loadError, PROJECT_MESSAGES.loadError))
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void loadProjects()
    return () => {
      mounted = false
    }
  }, [appliedQuery, refreshToken])

  return (
    <>
      <PageHeader description={PROJECT_MESSAGES.listDescription} title={PROJECT_MESSAGES.listTitle} />
      {isAdmin ? <ProjectListToolbar onCreateProject={() => setCreateOpen(true)} /> : null}
      <Stack spacing={2}>
        <ProjectSearchBar onChange={setDraftSearch} value={draftSearch} />
        <ProjectListFilters
          isAdmin={isAdmin}
          onChange={updateQuery}
          query={appliedQuery}
        />
        {error ? <Alert severity="error">{error}</Alert> : null}
        {loading ? (
          <Stack sx={{ alignItems: 'center', py: 6 }}>
            <Skeleton height={180} variant="rounded" width="100%" />
          </Stack>
        ) : null}
        {!loading && pageData.content.length === 0 ? (
          <Alert severity="info">
            <Box>
              <strong>{PROJECT_MESSAGES.emptyTitle}</strong>
              <Box sx={{ mt: 0.5 }}>{PROJECT_MESSAGES.emptyDescription}</Box>
            </Box>
          </Alert>
        ) : !loading ? (
          <ProjectCardGrid projects={pageData.content} />
        ) : null}
        <TablePaginationBar
          onPageChange={(page) => updateQuery({ ...appliedQuery, page })}
          onSizeChange={(size) => updateQuery({ ...appliedQuery, page: 0, size })}
          page={pageData.page}
          size={pageData.size}
          totalElements={pageData.totalElements}
        />
      </Stack>
      <CreateProjectDialog
        onClose={() => setCreateOpen(false)}
        onSuccess={(projectId) => {
          setCreateOpen(false)
          setNotification({ message: PROJECT_MESSAGES.createSuccess, severity: 'success' })
          setRefreshToken((current) => current + 1)
          navigate(`/projects/${projectId}`)
        }}
        open={createOpen}
      />
      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}
