import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, Stack, useMediaQuery, useTheme } from '@mui/material'
import { useSearchParams } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { TechnologyFilterBar } from '../../components/learn/TechnologyFilterBar'
import { TechnologyCardList, TechnologyTable } from '../../components/learn/TechnologyListViews'
import { TechnologySearchBar } from '../../components/learn/TechnologySearchBar'
import { TechnologyStatusFilterTabs } from '../../components/learn/TechnologyStatusFilterTabs'
import { TechnologyListToolbar } from '../../components/learn/TechnologyListToolbar'
import { TechnologyCurationPanel } from '../../components/learn/TechnologyCurationPanel'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'
import { LearnPageIntro } from '../../layout/LearnLayout'
import type { PageResponse } from '../../types/api'
import type { CatalogStatus, Technology, TechnologyLifecycleAction } from '../../types/learn'
import { DEFAULT_TECHNOLOGY_LIST_QUERY } from '../../types/learn'
import { resolveApiError } from '../../utils/apiErrors'
import {
  buildTechnologyListSearchParams,
  parseTechnologyListQuery,
  toTechnologyApiParams,
  toggleSort,
} from './learnListParams'

const SEARCH_DEBOUNCE_MS = 300

const EMPTY_PAGE: PageResponse<Technology> = {
  content: [],
  page: 0,
  size: DEFAULT_TECHNOLOGY_LIST_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [],
}

interface TechnologyListPageProps {
  adminMode?: boolean
}

export function TechnologyListPage({ adminMode = false }: TechnologyListPageProps) {
  const { isAdmin } = useAuth()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseTechnologyListQuery(searchParams), [searchParams])
  const [draftSearch, setDraftSearch] = useState(appliedQuery.search)
  const [pageData, setPageData] = useState<PageResponse<Technology>>(EMPTY_PAGE)
  const [catalogStatus, setCatalogStatus] = useState<CatalogStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)
  const [curatingTechnology, setCuratingTechnology] = useState<Technology | null>(null)
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)

  const showAdminControls = adminMode && isAdmin

  const refreshTechnologies = useCallback(() => {
    setRefreshToken((current) => current + 1)
  }, [])

  const showSuccessNotification = useCallback((message: string) => {
    setNotification({ message, severity: 'success' })
  }, [])

  const updateQuery = useCallback(
    (nextQuery: typeof appliedQuery) => {
      setSearchParams(buildTechnologyListSearchParams(nextQuery), { replace: true })
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
    if (!showAdminControls) {
      return
    }

    let mounted = true
    void learnApi.getCatalogStatus().then((status) => {
      if (mounted) {
        setCatalogStatus(status)
      }
    })

    return () => {
      mounted = false
    }
  }, [showAdminControls, refreshToken])

  useEffect(() => {
    let mounted = true

    async function loadTechnologies() {
      setLoading(true)
      setError(null)

      try {
        const params = toTechnologyApiParams(appliedQuery, { isAdmin: showAdminControls })
        const response = showAdminControls
          ? await learnApi.listManageTechnologies(params)
          : await learnApi.listTechnologies(params)
        if (mounted) {
          setPageData(response)
        }
      } catch (loadError) {
        if (mounted) {
          setError(resolveApiError(loadError, LEARN_MESSAGES.listLoadError))
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void loadTechnologies()

    return () => {
      mounted = false
    }
  }, [appliedQuery, refreshToken, showAdminControls])

  const emptyMessage = useMemo(() => {
    if (appliedQuery.search || appliedQuery.category || appliedQuery.difficulty) {
      return LEARN_MESSAGES.listSearchEmpty
    }
    if (showAdminControls && appliedQuery.status) {
      return `No ${appliedQuery.status.toLowerCase()} technologies found.`
    }
    return LEARN_MESSAGES.listEmpty
  }, [appliedQuery.category, appliedQuery.difficulty, appliedQuery.search, appliedQuery.status, showAdminControls])

  const showEmptyState = !loading && !error && pageData.content.length === 0
  const showList = !error && (loading || pageData.content.length > 0)

  function handleCurationSuccess() {
    setCuratingTechnology(null)
    showSuccessNotification(LEARN_MESSAGES.curationSuccess)
    refreshTechnologies()
  }

  function handleLifecycleSuccess(action: TechnologyLifecycleAction) {
    const message =
      action === 'publish'
        ? LEARN_MESSAGES.publishSuccess
        : action === 'hide'
          ? LEARN_MESSAGES.hideSuccess
          : LEARN_MESSAGES.archiveSuccess
    showSuccessNotification(message)
    refreshTechnologies()
  }

  return (
    <>
      <PageHeader
        description={showAdminControls ? LEARN_MESSAGES.manageTechnologiesDescription : LEARN_MESSAGES.technologiesDescription}
        title={showAdminControls ? LEARN_MESSAGES.manageTechnologiesTitle : LEARN_MESSAGES.technologiesTitle}
      />
      {!showAdminControls ? <LearnPageIntro /> : null}

      {showAdminControls ? <TechnologyListToolbar catalogStatus={catalogStatus} /> : null}

      <Stack spacing={2} sx={{ mb: 2 }}>
        <TechnologySearchBar onChange={setDraftSearch} value={draftSearch} />
        <TechnologyFilterBar onChange={updateQuery} query={appliedQuery} />
      </Stack>

      {showAdminControls ? (
        <TechnologyStatusFilterTabs
          onChange={(status) => updateQuery({ ...appliedQuery, page: 0, status })}
          value={appliedQuery.status}
        />
      ) : null}

      {error ? <Alert severity="error">{error}</Alert> : null}
      {showEmptyState ? (
        <Alert
          action={
            appliedQuery.search ? (
              <Button color="inherit" onClick={() => updateQuery({ ...appliedQuery, page: 0, search: '' })} size="small">
                {LEARN_MESSAGES.clearSearch}
              </Button>
            ) : undefined
          }
          severity="info"
        >
          {emptyMessage}
        </Alert>
      ) : null}

      {showList ? (
        <Box>
          {isMobile ? (
            <TechnologyCardList loading={loading} technologies={pageData.content} />
          ) : (
            <TechnologyTable
              loading={loading}
              onCurate={showAdminControls ? setCuratingTechnology : undefined}
              onLifecycleSuccess={showAdminControls ? handleLifecycleSuccess : undefined}
              onSort={(property) => updateQuery({ ...appliedQuery, sort: toggleSort(appliedQuery.sort, property) })}
              showStatusColumn={showAdminControls}
              sort={appliedQuery.sort}
              technologies={pageData.content}
            />
          )}
          <TablePaginationBar
            onPageChange={(page) => updateQuery({ ...appliedQuery, page })}
            onSizeChange={(size) => updateQuery({ ...appliedQuery, page: 0, size })}
            page={pageData.page}
            size={pageData.size}
            totalElements={pageData.totalElements}
          />
        </Box>
      ) : null}

      {showAdminControls ? (
        <TechnologyCurationPanel
          onClose={() => setCuratingTechnology(null)}
          onSuccess={handleCurationSuccess}
          open={Boolean(curatingTechnology)}
          technology={curatingTechnology}
        />
      ) : null}

      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}
