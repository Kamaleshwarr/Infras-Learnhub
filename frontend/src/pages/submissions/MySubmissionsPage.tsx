import { useCallback, useEffect, useMemo, useState } from 'react'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import { Alert, Box, Button, Paper, Tab, Tabs } from '@mui/material'
import { Link as RouterLink, useLocation, useSearchParams } from 'react-router-dom'
import { submissionsApi } from '../../api/submissionsApi'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { MySubmissionsTable } from '../../components/submissions/MySubmissionsTable'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import type { UserManagementNotification } from '../../components/users/UserManagementSnackbar'
import { UserManagementSnackbar } from '../../components/users/UserManagementSnackbar'
import type { PageResponse } from '../../types/api'
import type { CertificateSubmission } from '../../types/submissions'
import { resolveApiError } from '../../utils/apiErrors'
import type { SubmissionRouteNotification } from './SubmitCertificatePage'
import {
  buildMySubmissionsSearchParams,
  DEFAULT_MY_SUBMISSIONS_QUERY,
  parseMySubmissionsListQuery,
  toMySubmissionsApiParams,
  type SubmissionStatusFilter,
} from './mySubmissionsListParams'

const EMPTY_PAGE: PageResponse<CertificateSubmission> = {
  content: [],
  page: 0,
  size: DEFAULT_MY_SUBMISSIONS_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [{ property: 'submittedAtUtc', direction: 'DESC' }],
}

const STATUS_TABS: Array<{ label: string; value: SubmissionStatusFilter }> = [
  { label: 'All', value: 'ALL' },
  { label: 'Submitted', value: 'SUBMITTED' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'Rejected', value: 'REJECTED' },
]

export function MySubmissionsPage() {
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseMySubmissionsListQuery(searchParams), [searchParams])
  const [pageData, setPageData] = useState<PageResponse<CertificateSubmission>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notification, setNotification] = useState<UserManagementNotification | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)

  const updateQuery = useCallback(
    (nextQuery: typeof appliedQuery) => {
      setSearchParams(buildMySubmissionsSearchParams(nextQuery), { replace: true })
    },
    [setSearchParams],
  )

  useEffect(() => {
    const routeNotification = (location.state as { submissionNotification?: SubmissionRouteNotification } | null)
      ?.submissionNotification

    if (routeNotification) {
      setNotification(routeNotification)
      window.history.replaceState({}, document.title)
    }
  }, [location.state])

  useEffect(() => {
    let mounted = true

    async function loadSubmissions() {
      setLoading(true)
      setError(null)

      try {
        const response = await submissionsApi.listMine(toMySubmissionsApiParams(appliedQuery))
        if (mounted) {
          setPageData(response)
        }
      } catch (loadError) {
        if (mounted) {
          setError(resolveApiError(loadError, SUBMISSION_MESSAGES.loadError))
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void loadSubmissions()

    return () => {
      mounted = false
    }
  }, [appliedQuery, refreshToken])

  const emptyMessage = useMemo(() => {
    if (appliedQuery.status === 'ALL') {
      return 'No certificate submissions yet.'
    }

    return `No ${appliedQuery.status.toLowerCase()} certificate submissions.`
  }, [appliedQuery.status])

  return (
    <>
      <PageHeader description="Track submitted, approved, and rejected certificates." title="My Submissions" />

      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2, gap: 2, flexWrap: 'wrap' }}>
        <Tabs
          onChange={(_event, value: SubmissionStatusFilter) => {
            updateQuery({ ...appliedQuery, status: value, page: 0 })
          }}
          value={appliedQuery.status}
        >
          {STATUS_TABS.map((tab) => (
            <Tab key={tab.value} label={tab.label} value={tab.value} />
          ))}
        </Tabs>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button component={RouterLink} startIcon={<UploadFileOutlinedIcon />} to="/submissions/new" variant="contained">
            Submit certificate
          </Button>
          <Button disabled={loading} onClick={() => setRefreshToken((current) => current + 1)} variant="outlined">
            Refresh
          </Button>
        </Box>
      </Box>

      {error ? (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      ) : null}

      <Paper variant="outlined">
        <MySubmissionsTable emptyMessage={emptyMessage} loading={loading} submissions={pageData.content} />
        {!loading && pageData.totalElements > 0 ? (
          <TablePaginationBar
            onPageChange={(page) => updateQuery({ ...appliedQuery, page })}
            onSizeChange={(size) => updateQuery({ ...appliedQuery, size, page: 0 })}
            page={pageData.page}
            size={pageData.size}
            totalElements={pageData.totalElements}
          />
        ) : null}
      </Paper>

      <UserManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}
