import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button } from '@mui/material'
import { useSearchParams } from 'react-router-dom'
import { usersApi } from '../../api/usersApi'
import { useAuth } from '../../auth/useAuth'
import { ConfirmActionDialog } from '../../components/common/ConfirmActionDialog'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { CreateUserDialog } from '../../components/users/CreateUserDialog'
import { BulkImportDialog } from '../../components/users/BulkImportDialog'
import { EditUserDialog } from '../../components/users/EditUserDialog'
import { ResetPasswordDialog } from '../../components/users/ResetPasswordDialog'
import { USER_MANAGEMENT_MESSAGES } from '../../components/users/userManagementMessages'
import type { UserManagementNotification } from '../../components/users/UserManagementSnackbar'
import { UserManagementSnackbar } from '../../components/users/UserManagementSnackbar'
import { UserFilters } from '../../components/users/UserFilters'
import { UserListToolbar } from '../../components/users/UserListToolbar'
import { UserTable } from '../../components/users/UserTable'
import type { PageResponse } from '../../types/api'
import type { UserListQuery, UserImportResponse, UserSummary } from '../../types/users'
import { DEFAULT_USER_LIST_QUERY } from '../../types/users'
import { resolveApiError } from '../../utils/apiErrors'
import { downloadBlob } from '../../utils/downloadBlob'
import {
  buildUserListSearchParams,
  parseUserListQuery,
  toApiParams,
  toggleSort,
} from './userListParams'

const EMPTY_PAGE: PageResponse<UserSummary> = {
  content: [],
  page: 0,
  size: DEFAULT_USER_LIST_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [{ property: 'employeeId', direction: 'ASC' }],
}

type ConfirmActionState =
  | { type: 'activate'; user: UserSummary }
  | { type: 'deactivate'; user: UserSummary }
  | null

export function UserListPage() {
  const { user: currentUser } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseUserListQuery(searchParams), [searchParams])
  const [draftQuery, setDraftQuery] = useState<UserListQuery>(appliedQuery)
  const [pageData, setPageData] = useState<PageResponse<UserSummary>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [importOpen, setImportOpen] = useState(false)
  const [downloadingTemplate, setDownloadingTemplate] = useState(false)
  const [editingUser, setEditingUser] = useState<UserSummary | null>(null)
  const [confirmAction, setConfirmAction] = useState<ConfirmActionState>(null)
  const [resetPasswordUser, setResetPasswordUser] = useState<UserSummary | null>(null)
  const [confirmSubmitting, setConfirmSubmitting] = useState(false)
  const [confirmError, setConfirmError] = useState<string | null>(null)
  const [notification, setNotification] = useState<UserManagementNotification | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)

  useEffect(() => {
    setDraftQuery(appliedQuery)
  }, [appliedQuery])

  const updateQuery = useCallback(
    (nextQuery: UserListQuery) => {
      setSearchParams(buildUserListSearchParams(nextQuery), { replace: true })
    },
    [setSearchParams],
  )

  const refreshUsers = useCallback(() => {
    setRefreshToken((current) => current + 1)
  }, [])

  const showSuccessNotification = useCallback((message: string) => {
    setNotification({ message, severity: 'success' })
  }, [])

  const showErrorNotification = useCallback((message: string) => {
    setNotification({ message, severity: 'error' })
  }, [])

  useEffect(() => {
    let mounted = true

    async function loadUsers() {
      setLoading(true)
      setError(null)
      try {
        const response = await usersApi.list(toApiParams(appliedQuery))
        if (mounted) {
          setPageData(response)
        }
      } catch {
        if (mounted) {
          setError('Unable to load users. Please refresh or try again later.')
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    loadUsers()
    return () => {
      mounted = false
    }
  }, [appliedQuery, refreshToken])

  const showMustChangePasswordColumn = useMemo(
    () => pageData.content.some((user) => user.mustChangePassword !== undefined),
    [pageData.content],
  )

  function handleApplyFilters() {
    updateQuery({ ...draftQuery, page: 0 })
  }

  function handleClearFilters() {
    updateQuery({ ...DEFAULT_USER_LIST_QUERY })
  }

  function handleSort(property: string) {
    updateQuery({
      ...appliedQuery,
      sort: toggleSort(appliedQuery.sort, property),
      page: 0,
    })
  }

  function handlePageChange(page: number) {
    updateQuery({ ...appliedQuery, page })
  }

  function handleSizeChange(size: number) {
    updateQuery({ ...appliedQuery, size, page: 0 })
  }

  function handleCreateSuccess() {
    setCreateOpen(false)
    showSuccessNotification(USER_MANAGEMENT_MESSAGES.createSuccess)
    refreshUsers()
  }

  function handleEditSuccess() {
    setEditingUser(null)
    showSuccessNotification(USER_MANAGEMENT_MESSAGES.updateSuccess)
    refreshUsers()
  }

  function openConfirmAction(type: 'activate' | 'deactivate', user: UserSummary) {
    setConfirmError(null)
    setConfirmAction({ type, user })
  }

  function closeConfirmAction() {
    if (!confirmSubmitting) {
      setConfirmAction(null)
      setConfirmError(null)
    }
  }

  async function handleConfirmAction() {
    if (!confirmAction) {
      return
    }

    setConfirmSubmitting(true)
    setConfirmError(null)

    try {
      if (confirmAction.type === 'activate') {
        await usersApi.activate(confirmAction.user.id)
        setConfirmAction(null)
        showSuccessNotification(USER_MANAGEMENT_MESSAGES.activateSuccess)
      } else {
        await usersApi.deactivate(confirmAction.user.id)
        setConfirmAction(null)
        showSuccessNotification(USER_MANAGEMENT_MESSAGES.deactivateSuccess)
      }
      refreshUsers()
    } catch (error) {
      setConfirmError(
        resolveApiError(
          error,
          `Unable to ${confirmAction.type} user. Please try again.`,
        ),
      )
    } finally {
      setConfirmSubmitting(false)
    }
  }

  function handleResetPasswordSuccess() {
    setResetPasswordUser(null)
    showSuccessNotification(USER_MANAGEMENT_MESSAGES.resetPasswordSuccess)
    refreshUsers()
  }

  async function handleDownloadTemplate() {
    setDownloadingTemplate(true)
    try {
      const blob = await usersApi.downloadImportTemplate()
      downloadBlob(blob, 'user-import-template.csv')
      showSuccessNotification(USER_MANAGEMENT_MESSAGES.templateDownloadSuccess)
    } catch (error) {
      showErrorNotification(resolveApiError(error, 'Unable to download import template. Please try again.'))
    } finally {
      setDownloadingTemplate(false)
    }
  }

  function handleImportComplete(result: UserImportResponse) {
    if (result.imported > 0) {
      showSuccessNotification(USER_MANAGEMENT_MESSAGES.importSuccess(result.imported))
      refreshUsers()
      return
    }
    showErrorNotification(USER_MANAGEMENT_MESSAGES.importNoRows)
  }

  function handleImportDialogClose() {
    setImportOpen(false)
  }

  const hasActiveFilters =
    Boolean(appliedQuery.employeeId) ||
    Boolean(appliedQuery.fullName) ||
    Boolean(appliedQuery.email) ||
    Boolean(appliedQuery.role) ||
    Boolean(appliedQuery.active)

  return (
    <Box>
      <PageHeader
        description="Search, filter, and manage employee accounts."
        title="User Management"
      />
      <UserListToolbar
        downloadingTemplate={downloadingTemplate}
        onCreateUser={() => setCreateOpen(true)}
        onDownloadTemplate={() => void handleDownloadTemplate()}
        onImportUsers={() => setImportOpen(true)}
      />
      {error ? (
        <Alert
          action={
            <Button color="inherit" onClick={refreshUsers} size="small">
              Retry
            </Button>
          }
          severity="error"
          sx={{ mb: 3 }}
        >
          {error}
        </Alert>
      ) : null}
      <UserFilters
        draft={draftQuery}
        onApply={handleApplyFilters}
        onClear={handleClearFilters}
        onDraftChange={setDraftQuery}
      />
      <UserTable
        currentUserId={currentUser?.id}
        hasActiveFilters={hasActiveFilters}
        loading={loading}
        onActivate={(user) => openConfirmAction('activate', user)}
        onDeactivate={(user) => openConfirmAction('deactivate', user)}
        onEdit={setEditingUser}
        onResetPassword={setResetPasswordUser}
        onSort={handleSort}
        showMustChangePasswordColumn={showMustChangePasswordColumn}
        sort={appliedQuery.sort}
        users={pageData.content}
      />
      {!loading && !error && pageData.totalElements > 0 ? (
        <TablePaginationBar
          onPageChange={handlePageChange}
          onSizeChange={handleSizeChange}
          page={pageData.page}
          size={pageData.size}
          totalElements={pageData.totalElements}
        />
      ) : null}
      <CreateUserDialog onClose={() => setCreateOpen(false)} onSuccess={handleCreateSuccess} open={createOpen} />
      <EditUserDialog
        currentUserId={currentUser?.id}
        onClose={() => setEditingUser(null)}
        onSuccess={handleEditSuccess}
        open={Boolean(editingUser)}
        user={editingUser}
      />
      <ConfirmActionDialog
        confirmColor={confirmAction?.type === 'activate' ? 'success' : 'warning'}
        confirmLabel={confirmAction?.type === 'activate' ? 'Activate user' : 'Deactivate user'}
        onClose={closeConfirmAction}
        onConfirm={handleConfirmAction}
        open={Boolean(confirmAction)}
        submitting={confirmSubmitting}
        title={confirmAction?.type === 'activate' ? 'Activate user' : 'Deactivate user'}
        user={confirmAction?.user ?? null}
      >
        {confirmError ? <Alert severity="error">{confirmError}</Alert> : null}
        {confirmAction?.type === 'deactivate' ? (
          <>
            <Alert severity="warning">
              This user will be unable to sign in until reactivated.
            </Alert>
            <Alert severity="info">
              This action does not delete the user. The account can be reactivated later.
            </Alert>
          </>
        ) : null}
      </ConfirmActionDialog>
      <ResetPasswordDialog
        onClose={() => setResetPasswordUser(null)}
        onSuccess={handleResetPasswordSuccess}
        open={Boolean(resetPasswordUser)}
        user={resetPasswordUser}
      />
      <BulkImportDialog
        onClose={handleImportDialogClose}
        onComplete={handleImportComplete}
        onDownloadTemplate={() => void handleDownloadTemplate()}
        open={importOpen}
      />
      <UserManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </Box>
  )
}
