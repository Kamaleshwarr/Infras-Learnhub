import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, Paper } from '@mui/material'
import { useSearchParams } from 'react-router-dom'
import { submissionsApi } from '../../api/submissionsApi'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { AdminReviewTable } from '../../components/submissions/AdminReviewTable'
import { ApproveSubmissionDialog } from '../../components/submissions/ApproveSubmissionDialog'
import { RejectSubmissionDialog } from '../../components/submissions/RejectSubmissionDialog'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import type { UserManagementNotification } from '../../components/users/UserManagementSnackbar'
import { UserManagementSnackbar } from '../../components/users/UserManagementSnackbar'
import type { PageResponse } from '../../types/api'
import type { CertificateSubmission } from '../../types/submissions'
import { resolveApiError } from '../../utils/apiErrors'
import {
  buildAdminReviewSearchParams,
  DEFAULT_ADMIN_REVIEW_QUERY,
  parseAdminReviewListQuery,
  toAdminReviewApiParams,
} from './adminReviewListParams'

const EMPTY_PAGE: PageResponse<CertificateSubmission> = {
  content: [],
  page: 0,
  size: DEFAULT_ADMIN_REVIEW_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [{ property: 'submittedAtUtc', direction: 'DESC' }],
}

const EMPTY_MESSAGE = 'No certificate submissions awaiting review.'

export function AdminReviewPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseAdminReviewListQuery(searchParams), [searchParams])
  const [pageData, setPageData] = useState<PageResponse<CertificateSubmission>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)
  const [notification, setNotification] = useState<UserManagementNotification | null>(null)
  const [approveTarget, setApproveTarget] = useState<CertificateSubmission | null>(null)
  const [rejectTarget, setRejectTarget] = useState<CertificateSubmission | null>(null)
  const [actionSubmitting, setActionSubmitting] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  const updateQuery = useCallback(
    (nextQuery: typeof appliedQuery) => {
      setSearchParams(buildAdminReviewSearchParams(nextQuery), { replace: true })
    },
    [setSearchParams],
  )

  useEffect(() => {
    let mounted = true

    async function loadPendingSubmissions() {
      setLoading(true)
      setError(null)

      try {
        const response = await submissionsApi.listAll(toAdminReviewApiParams(appliedQuery))
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

    void loadPendingSubmissions()

    return () => {
      mounted = false
    }
  }, [appliedQuery, refreshToken])

  function closeApproveDialog() {
    if (actionSubmitting) {
      return
    }
    setApproveTarget(null)
    setActionError(null)
  }

  function closeRejectDialog() {
    if (actionSubmitting) {
      return
    }
    setRejectTarget(null)
    setActionError(null)
  }

  async function handleApprove() {
    if (!approveTarget) {
      return
    }

    setActionSubmitting(true)
    setActionError(null)

    try {
      await submissionsApi.approve(approveTarget.id)
      setApproveTarget(null)
      setNotification({
        message: SUBMISSION_MESSAGES.approveSuccess,
        severity: 'success',
      })
      setRefreshToken((current) => current + 1)
    } catch (approveError) {
      setActionError(resolveApiError(approveError, SUBMISSION_MESSAGES.approveError))
      setRefreshToken((current) => current + 1)
    } finally {
      setActionSubmitting(false)
    }
  }

  async function handleReject(rejectionReason: string) {
    if (!rejectTarget) {
      return
    }

    setActionSubmitting(true)
    setActionError(null)

    try {
      await submissionsApi.reject(rejectTarget.id, { rejectionReason })
      setRejectTarget(null)
      setNotification({
        message: SUBMISSION_MESSAGES.rejectSuccess,
        severity: 'success',
      })
      setRefreshToken((current) => current + 1)
    } catch (rejectError) {
      setActionError(resolveApiError(rejectError, SUBMISSION_MESSAGES.rejectError))
      setRefreshToken((current) => current + 1)
    } finally {
      setActionSubmitting(false)
    }
  }

  return (
    <>
      <PageHeader
        description="Review pending certificate submissions and approve or reject them."
        title="Certificate Review"
      />

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button disabled={loading || actionSubmitting} onClick={() => setRefreshToken((current) => current + 1)} variant="outlined">
          Refresh
        </Button>
      </Box>

      {error ? (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      ) : null}

      <Paper variant="outlined">
        <AdminReviewTable
          actionDisabled={actionSubmitting}
          emptyMessage={EMPTY_MESSAGE}
          loading={loading}
          onApprove={setApproveTarget}
          onReject={setRejectTarget}
          submissions={pageData.content}
        />
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

      <ApproveSubmissionDialog
        error={actionError}
        onClose={closeApproveDialog}
        onConfirm={() => void handleApprove()}
        open={Boolean(approveTarget)}
        submission={approveTarget}
        submitting={actionSubmitting}
      />
      <RejectSubmissionDialog
        error={actionError}
        onClose={closeRejectDialog}
        onSubmit={(rejectionReason) => void handleReject(rejectionReason)}
        open={Boolean(rejectTarget)}
        submission={rejectTarget}
        submitting={actionSubmitting}
      />
      <UserManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}
