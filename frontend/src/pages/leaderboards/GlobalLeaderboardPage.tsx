import { useCallback, useEffect, useState } from 'react'
import { Alert, Box, Button } from '@mui/material'
import { leaderboardsApi } from '../../api/leaderboardsApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { CurrentUserSummaryStrip } from '../../components/leaderboards/CurrentUserSummaryStrip'
import { GlobalLeaderboardTable } from '../../components/leaderboards/GlobalLeaderboardTable'
import { LEADERBOARD_MESSAGES } from '../../components/leaderboards/leaderboardMessages'
import { LeaderboardTabs } from '../../components/leaderboards/LeaderboardTabs'
import { TopPerformersSection } from '../../components/leaderboards/TopPerformersSection'
import type { PageResponse } from '../../types/api'
import type { GlobalLeaderboardEntry, PersonalLeaderboard } from '../../types/leaderboards'
import { DEFAULT_LEADERBOARD_PAGE_SIZE } from '../../types/leaderboards'
import { resolveApiError } from '../../utils/apiErrors'

const EMPTY_PAGE: PageResponse<GlobalLeaderboardEntry> = {
  content: [],
  first: true,
  last: true,
  page: 0,
  size: DEFAULT_LEADERBOARD_PAGE_SIZE,
  sort: [{ direction: 'ASC', property: 'rank' }],
  totalElements: 0,
  totalPages: 0,
}

export function GlobalLeaderboardPage() {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(DEFAULT_LEADERBOARD_PAGE_SIZE)
  const [pageData, setPageData] = useState<PageResponse<GlobalLeaderboardEntry>>(EMPTY_PAGE)
  const [topPerformers, setTopPerformers] = useState<GlobalLeaderboardEntry[]>([])
  const [personal, setPersonal] = useState<PersonalLeaderboard | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)

  const loadLeaderboard = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [globalResult, topResult, personalResult] = await Promise.allSettled([
        leaderboardsApi.global({ page, size, sort: 'rank,asc' }),
        leaderboardsApi.global({ page: 0, size: 3, sort: 'rank,asc' }),
        leaderboardsApi.me(),
      ])

      if (globalResult.status === 'rejected') {
        throw globalResult.reason
      }

      setPageData(globalResult.value)
      setTopPerformers(topResult.status === 'fulfilled' ? topResult.value.content : [])
      setPersonal(personalResult.status === 'fulfilled' ? personalResult.value : null)
    } catch (loadError) {
      setError(resolveApiError(loadError, LEADERBOARD_MESSAGES.errorGlobal))
      setPageData(EMPTY_PAGE)
      setTopPerformers([])
      setPersonal(null)
    } finally {
      setLoading(false)
    }
  }, [page, size])

  useEffect(() => {
    void loadLeaderboard()
  }, [loadLeaderboard, refreshToken])

  return (
    <Box>
      <PageHeader description={LEADERBOARD_MESSAGES.globalDescription} title={LEADERBOARD_MESSAGES.globalTitle} />
      <LeaderboardTabs />

      {error ? (
        <Alert
          action={
            <Button color="inherit" onClick={() => setRefreshToken((current) => current + 1)} size="small">
              {LEADERBOARD_MESSAGES.retry}
            </Button>
          }
          severity="error"
          sx={{ mb: 3 }}
        >
          {error}
        </Alert>
      ) : null}

      <TopPerformersSection
        currentUserId={user?.id}
        entries={topPerformers}
        loading={loading}
      />
      <CurrentUserSummaryStrip loading={loading} personal={personal} />
      <GlobalLeaderboardTable
        currentUserId={user?.id}
        entries={pageData.content}
        loading={loading}
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
    </Box>
  )
}
