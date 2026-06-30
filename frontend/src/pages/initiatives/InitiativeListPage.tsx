import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, useMediaQuery, useTheme } from '@mui/material'
import { useSearchParams } from 'react-router-dom'
import { initiativesApi } from '../../api/initiativesApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { InitiativeCardList, InitiativeTable } from '../../components/initiatives/InitiativeListViews'
import { InitiativeListToolbar } from '../../components/initiatives/InitiativeListToolbar'
import { CreateInitiativeDialog } from '../../components/initiatives/CreateInitiativeDialog'
import { EditInitiativeDialog } from '../../components/initiatives/EditInitiativeDialog'
import {
  InitiativeManagementSnackbar,
  type InitiativeManagementNotification,
} from '../../components/initiatives/InitiativeManagementSnackbar'
import { InitiativeSearchBar } from '../../components/initiatives/InitiativeSearchBar'
import { InitiativeStatusFilterTabs } from '../../components/initiatives/InitiativeStatusFilterTabs'
import { INITIATIVE_MESSAGES } from '../../components/initiatives/initiativeMessages'
import type { PageResponse } from '../../types/api'
import type { Initiative, InitiativeLifecycleAction } from '../../types/initiatives'
import { DEFAULT_INITIATIVE_LIST_QUERY } from '../../types/initiatives'
import { resolveApiError } from '../../utils/apiErrors'
import {
  buildInitiativeListSearchParams,
  parseInitiativeListQuery,
  toInitiativeApiParams,
  toggleSort,
} from './initiativeListParams'

const SEARCH_DEBOUNCE_MS = 300

const EMPTY_PAGE: PageResponse<Initiative> = {
  content: [],
  page: 0,
  size: DEFAULT_INITIATIVE_LIST_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [],
}

export function InitiativeListPage() {
  const { isAdmin } = useAuth()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseInitiativeListQuery(searchParams), [searchParams])
  const [draftSearch, setDraftSearch] = useState(appliedQuery.search)
  const [pageData, setPageData] = useState<PageResponse<Initiative>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)
  const [createOpen, setCreateOpen] = useState(false)
  const [editingInitiative, setEditingInitiative] = useState<Initiative | null>(null)
  const [notification, setNotification] = useState<InitiativeManagementNotification | null>(null)

  const refreshInitiatives = useCallback(() => {
    setRefreshToken((current) => current + 1)
  }, [])

  const showSuccessNotification = useCallback((message: string) => {
    setNotification({ message, severity: 'success' })
  }, [])

  const updateQuery = useCallback(
    (nextQuery: typeof appliedQuery) => {
      setSearchParams(buildInitiativeListSearchParams(nextQuery), { replace: true })
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

    async function loadInitiatives() {
      setLoading(true)
      setError(null)

      try {
        const response = await initiativesApi.list(toInitiativeApiParams(appliedQuery, { isAdmin }))
        if (mounted) {
          setPageData(response)
        }
      } catch (loadError) {
        if (mounted) {
          setError(resolveApiError(loadError, INITIATIVE_MESSAGES.listLoadError))
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void loadInitiatives()

    return () => {
      mounted = false
    }
  }, [appliedQuery, isAdmin, refreshToken])

  const emptyMessage = useMemo(() => {
    if (appliedQuery.search) {
      return INITIATIVE_MESSAGES.listSearchEmpty
    }

    if (isAdmin && appliedQuery.status) {
      return `No ${appliedQuery.status.toLowerCase()} initiatives found.`
    }

    return INITIATIVE_MESSAGES.listEmpty
  }, [appliedQuery.search, appliedQuery.status, isAdmin])

  const showEmptyState = !loading && !error && pageData.content.length === 0
  const showList = !error && (loading || pageData.content.length > 0)

  function handleCreateSuccess() {
    setCreateOpen(false)
    showSuccessNotification(INITIATIVE_MESSAGES.createSuccess)
    refreshInitiatives()
  }

  function handleEditSuccess() {
    setEditingInitiative(null)
    showSuccessNotification(INITIATIVE_MESSAGES.updateSuccess)
    refreshInitiatives()
  }

  function handleLifecycleSuccess(action: InitiativeLifecycleAction) {
    const message = {
      publish: INITIATIVE_MESSAGES.publishSuccess,
      returnToDraft: INITIATIVE_MESSAGES.returnToDraftSuccess,
      markExpired: INITIATIVE_MESSAGES.markExpiredSuccess,
      reactivate: INITIATIVE_MESSAGES.reactivateSuccess,
    }[action]
    showSuccessNotification(message)
    refreshInitiatives()
  }

  return (
    <>
      <PageHeader
        description={
          isAdmin
            ? 'Browse and filter learning initiatives across all statuses.'
            : 'Browse active certification programs available to you.'
        }
        title="Learning Initiatives"
      />

      {isAdmin ? <InitiativeListToolbar onCreateInitiative={() => setCreateOpen(true)} /> : null}

      <CreateInitiativeDialog
        onClose={() => setCreateOpen(false)}
        onSuccess={handleCreateSuccess}
        open={createOpen}
      />

      <EditInitiativeDialog
        initiative={editingInitiative}
        onClose={() => setEditingInitiative(null)}
        onSuccess={handleEditSuccess}
        open={Boolean(editingInitiative)}
      />

      <InitiativeManagementSnackbar notification={notification} onClose={() => setNotification(null)} />

      <Box sx={{ mb: 3 }}>
        <InitiativeSearchBar disabled={Boolean(error) && pageData.content.length === 0} onChange={setDraftSearch} value={draftSearch} />
      </Box>

      {isAdmin ? (
        <InitiativeStatusFilterTabs
          onChange={(status) => updateQuery({ ...appliedQuery, page: 0, status })}
          value={appliedQuery.status}
        />
      ) : null}

      {error ? (
        <Alert
          action={
            <Button color="inherit" onClick={() => setRefreshToken((current) => current + 1)} size="small">
              {INITIATIVE_MESSAGES.retry}
            </Button>
          }
          severity="error"
          sx={{ mb: 2 }}
        >
          {error}
        </Alert>
      ) : null}

      {showEmptyState ? (
        <Alert
          action={
            appliedQuery.search ? (
              <Button
                color="inherit"
                onClick={() => {
                  setDraftSearch('')
                  updateQuery({ ...appliedQuery, page: 0, search: '' })
                }}
                size="small"
              >
                {INITIATIVE_MESSAGES.clearSearch}
              </Button>
            ) : undefined
          }
          severity="info"
        >
          {emptyMessage}
        </Alert>
      ) : null}

      {showList ? (
        <>
          {isMobile ? (
            <InitiativeCardList
              initiatives={pageData.content}
              loading={loading}
              onEdit={isAdmin ? setEditingInitiative : undefined}
              onLifecycleSuccess={isAdmin ? handleLifecycleSuccess : undefined}
              showStatusColumn={isAdmin}
            />
          ) : (
            <InitiativeTable
              initiatives={pageData.content}
              loading={loading}
              onEdit={isAdmin ? setEditingInitiative : undefined}
              onLifecycleSuccess={isAdmin ? handleLifecycleSuccess : undefined}
              onSort={(property) => updateQuery({ ...appliedQuery, page: 0, sort: toggleSort(appliedQuery.sort, property) })}
              showStatusColumn={isAdmin}
              sort={appliedQuery.sort}
            />
          )}

          {!loading && pageData.totalElements > 0 ? (
            <TablePaginationBar
              onPageChange={(page) => updateQuery({ ...appliedQuery, page })}
              onSizeChange={(size) => updateQuery({ ...appliedQuery, page: 0, size })}
              page={pageData.page}
              size={pageData.size}
              totalElements={pageData.totalElements}
            />
          ) : null}
        </>
      ) : null}
    </>
  )
}
