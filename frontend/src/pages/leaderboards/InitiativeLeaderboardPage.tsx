import { useCallback, useEffect, useState } from 'react'
import { Alert, Box, Button, FormControl, InputLabel, MenuItem, Select, Stack } from '@mui/material'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import { initiativesApi } from '../../api/initiativesApi'
import { leaderboardsApi } from '../../api/leaderboardsApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { InitiativeLeaderboardView } from '../../components/leaderboards/InitiativeLeaderboardView'
import { LEADERBOARD_MESSAGES } from '../../components/leaderboards/leaderboardMessages'
import { LeaderboardTabs } from '../../components/leaderboards/LeaderboardTabs'
import type { PageResponse } from '../../types/api'
import type { Initiative } from '../../types/initiatives'
import type { InitiativeLeaderboardEntry } from '../../types/leaderboards'
import { DEFAULT_LEADERBOARD_PAGE_SIZE } from '../../types/leaderboards'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

const EMPTY_PAGE: PageResponse<InitiativeLeaderboardEntry> = {
  content: [],
  first: true,
  last: true,
  page: 0,
  size: DEFAULT_LEADERBOARD_PAGE_SIZE,
  sort: [{ direction: 'ASC', property: 'rank' }],
  totalElements: 0,
  totalPages: 0,
}

export function InitiativeLeaderboardPage() {
  const { initiativeId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [initiatives, setInitiatives] = useState<Initiative[]>([])
  const [initiativeTitle, setInitiativeTitle] = useState('')
  const [pageData, setPageData] = useState<PageResponse<InitiativeLeaderboardEntry>>(EMPTY_PAGE)
  const [loadingInitiatives, setLoadingInitiatives] = useState(!initiativeId)
  const [loadingLeaderboard, setLoadingLeaderboard] = useState(Boolean(initiativeId))
  const [initiativesError, setInitiativesError] = useState<string | null>(null)
  const [leaderboardError, setLeaderboardError] = useState<string | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(DEFAULT_LEADERBOARD_PAGE_SIZE)
  const [refreshToken, setRefreshToken] = useState(0)

  useEffect(() => {
    if (initiativeId) {
      return
    }

    let mounted = true
    async function loadInitiatives() {
      setLoadingInitiatives(true)
      setInitiativesError(null)
      try {
        const response = await initiativesApi.list({ size: 100, sort: 'title,asc', status: 'ACTIVE' })
        if (mounted) {
          setInitiatives(response.content)
        }
      } catch {
        if (mounted) {
          setInitiativesError(LEADERBOARD_MESSAGES.loadInitiativesError)
          setInitiatives([])
        }
      } finally {
        if (mounted) {
          setLoadingInitiatives(false)
        }
      }
    }

    void loadInitiatives()
    return () => {
      mounted = false
    }
  }, [initiativeId])

  const loadLeaderboard = useCallback(async () => {
    if (!initiativeId) {
      return
    }

    setLoadingLeaderboard(true)
    setLeaderboardError(null)
    setNotFound(false)

    try {
      const [initiativeResult, leaderboardResult] = await Promise.allSettled([
        initiativesApi.get(initiativeId),
        leaderboardsApi.initiative(initiativeId, { page, size, sort: 'rank,asc' }),
      ])

      if (initiativeResult.status === 'rejected') {
        if (isNotFoundError(initiativeResult.reason)) {
          setNotFound(true)
          setPageData(EMPTY_PAGE)
          setInitiativeTitle('')
          return
        }
        throw initiativeResult.reason
      }

      if (leaderboardResult.status === 'rejected') {
        if (isNotFoundError(leaderboardResult.reason)) {
          setNotFound(true)
          setPageData(EMPTY_PAGE)
          setInitiativeTitle(initiativeResult.value.title)
          return
        }
        throw leaderboardResult.reason
      }

      setInitiativeTitle(initiativeResult.value.title)
      setPageData(leaderboardResult.value)
    } catch (loadError) {
      setLeaderboardError(resolveApiError(loadError, LEADERBOARD_MESSAGES.errorInitiative))
      setPageData(EMPTY_PAGE)
    } finally {
      setLoadingLeaderboard(false)
    }
  }, [initiativeId, page, size])

  useEffect(() => {
    void loadLeaderboard()
  }, [loadLeaderboard, refreshToken])

  return (
    <Box>
      <PageHeader
        description={LEADERBOARD_MESSAGES.initiativeDescription}
        title={LEADERBOARD_MESSAGES.initiativeTab + ' Leaderboard'}
      />
      <LeaderboardTabs />

      {!initiativeId ? (
        <Stack spacing={2}>
          {initiativesError ? <Alert severity="error">{initiativesError}</Alert> : null}
          <FormControl fullWidth sx={{ maxWidth: 480 }}>
            <InputLabel id="initiative-leaderboard-select-label">{LEADERBOARD_MESSAGES.initiativeSelectLabel}</InputLabel>
            <Select
              disabled={loadingInitiatives}
              displayEmpty
              id="initiative-leaderboard-select"
              label={LEADERBOARD_MESSAGES.initiativeSelectLabel}
              labelId="initiative-leaderboard-select-label"
              onChange={(event) => {
                if (event.target.value) {
                  navigate(`/leaderboards/initiatives/${event.target.value}`)
                }
              }}
              value=""
            >
              <MenuItem disabled value="">
                {loadingInitiatives ? 'Loading initiatives...' : LEADERBOARD_MESSAGES.initiativeSelectPlaceholder}
              </MenuItem>
              {initiatives.map((initiative) => (
                <MenuItem key={initiative.id} value={initiative.id}>
                  {initiative.title}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Alert severity="info">{LEADERBOARD_MESSAGES.noInitiativeSelected}</Alert>
        </Stack>
      ) : notFound ? (
        <Alert severity="warning">{LEADERBOARD_MESSAGES.notFound}</Alert>
      ) : (
        <>
          <InitiativeLeaderboardView
            currentUserId={user?.id}
            entries={pageData.content}
            error={leaderboardError}
            initiativeTitle={initiativeTitle || pageData.content[0]?.initiativeTitle || 'Initiative'}
            loading={loadingLeaderboard}
            onRetry={() => setRefreshToken((current) => current + 1)}
          />
          <TablePaginationBar
            onPageChange={setPage}
            onSizeChange={(nextSize) => {
              setSize(nextSize)
              setPage(0)
            }}
            page={page}
            size={size}
            totalElements={pageData.totalElements}
          />
          <Button component={RouterLink} sx={{ mt: 2 }} to={`/initiatives/${initiativeId}`} variant="text">
            View Initiative Details
          </Button>
        </>
      )}
    </Box>
  )
}
